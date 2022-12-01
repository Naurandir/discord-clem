
package at.naurandir.discord.clem.bot.service;

import at.naurandir.discord.clem.bot.model.news.News;
import at.naurandir.discord.clem.bot.model.news.NewsMapper;
import at.naurandir.discord.clem.bot.repository.NewsRepository;
import at.naurandir.discord.clem.bot.service.client.dto.worldstate.AlertDTO;
import at.naurandir.discord.clem.bot.service.client.dto.worldstate.NewsDTO;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 *
 * @author Naurandir
 */
@ExtendWith(MockitoExtension.class)
public class NewsServiceTest extends BaseServiceTest {
    
    @Spy
    private NewsMapper newsMapper;
    
    @Spy
    private NewsRepository newsRepository;
    
    @Mock
    private UpdatePipelineService upatePipelineService;

    @InjectMocks
    private NewsService newsService;
    
    private News news;
    
    private final int numberOfNews = 2; // 2 News existing with date 9999 and should be handled
    
    @Override
    @BeforeEach
    public void setup() throws IOException {
        news = new News();
        news.setId(815L);
        news.setExternalId("anyId");
        
        when(newsMapper.newsDtoToNews(any())).thenReturn(news);
        
        when(newsRepository.findByEndDateIsNull()).thenReturn(new ArrayList<>());
        when(newsRepository.save(any())).thenReturn(news);
        
        setWarframeClientGetListMock("dto/news.json", NewsDTO.class);
    }
    
    @Test
    public void shouldSyncNewNews() throws IOException {
        newsService.sync();
        
        verify(newsRepository, times(numberOfNews)).save(any());
        verify(newsMapper, times(numberOfNews)).newsDtoToNews(any());
    }
    
    @Test
    public void shouldSyncExistingNewsNothingToDo() throws IOException {
        when(newsRepository.findByEndDateIsNull()).thenReturn(List.of(news));
        news.setExternalId("62d31b87106360aa5703954d");
        news.setStartDate(LocalDateTime.now());
        
        newsService.sync();
        
        verify(newsRepository, times(numberOfNews - 1)).save(any());
        verify(newsMapper, times(numberOfNews - 1)).newsDtoToNews(any());
    }
    
    @Test
    public void shouldSyncExpiredNews() throws IOException {
        when(newsRepository.findByEndDateIsNull()).thenReturn(List.of(news));
        news.setExternalId("oldId");
        news.setStartDate(LocalDateTime.now().minusDays(90));
        
        newsService.sync();
        
        verify(newsRepository, times(numberOfNews + 1)).save(any());
        verify(newsMapper, times(numberOfNews)).newsDtoToNews(any());
        Assertions.assertTrue(() -> news.getEndDate() != null);
    }
    
}
