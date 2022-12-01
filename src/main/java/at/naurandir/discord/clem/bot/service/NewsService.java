package at.naurandir.discord.clem.bot.service;

import at.naurandir.discord.clem.bot.model.news.News;
import at.naurandir.discord.clem.bot.model.news.NewsMapper;
import at.naurandir.discord.clem.bot.repository.NewsRepository;
import at.naurandir.discord.clem.bot.service.client.WarframeClient;
import at.naurandir.discord.clem.bot.service.client.dto.worldstate.NewsDTO;
import at.naurandir.discord.clem.bot.utils.LocalDateTimeUtil;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 *
 * @author Naurandir
 */
@Slf4j
@Service
public class NewsService extends SyncService {
    
    @Autowired
    private NewsRepository newsRepository;
    
    @Autowired
    private NewsMapper newsMapper;
    
    @Autowired
    private WarframeClient warframeClient;
    
    @Autowired
    private UpdatePipelineService updatePipelineService;
    
    @Value( "${discord.clem.news.url}" )
    private String apiUrl;
    
    @Value("#{${discord.clem.news.headers}}")
    private Map<String, String> apiHeaders;
    
    private final long daysUntilExpired = 30;

    @Override
    @Scheduled(cron = "${discord.clem.news.scheduler.cron}")
    public void sync() {
        try {
            doSync();
        } catch (Exception ex) {
            log.error("sync: throwed error: ", ex);
        }
    }

    @Override
    public void doSync() throws IOException {
        List<NewsDTO> news = warframeClient.getListData(apiUrl, apiHeaders, NewsDTO.class);
        List<News> dbNews = newsRepository.findByEndDateIsNull();
        
        // add new news
        for (NewsDTO newsDto : news) {
            boolean isInDatabase = dbNews.stream()
                    .anyMatch(dbNewsItem -> dbNewsItem.getExternalId().equals(newsDto.getId()));
            
            if (isInDatabase) {
                log.debug("doSync: news [{}] already exists in database", newsDto.getId());
                continue; // nothing to do
            }
            
            Duration diffTime = LocalDateTimeUtil.getDiffTime(newsDto.getDate(), LocalDateTime.now());
            if (diffTime.toDays() >= daysUntilExpired) {
                log.debug("doSync: news [{}] is older than 10 days", newsDto.getId());
                continue; // nothing to do
            }
            
            News newNews = newsMapper.newsDtoToNews(newsDto);
            newNews = newsRepository.save(newNews);
            
            log.info("doSync: added new news [{} - {}]", newNews.getId(), newNews.getExternalId());
            
            updatePipelineService.addNewsNotify(newNews);
        }
        
        // inactivate old news
        for (News newsItem : dbNews) {
            LocalDateTime now = LocalDateTime.now();
            Duration diffTime = LocalDateTimeUtil.getDiffTime(newsItem.getStartDate(), now);
            
            if (diffTime.toDays() < daysUntilExpired) {
                continue;
            }
            
            newsItem.setModifyDate(now);
            newsItem.setEndDate(now);
            newsRepository.save(newsItem);
            log.info("doSync: news [{}] expired, set end date to now.", newsItem.getId());
        }
    }

    @Override
    boolean isFirstTimeStartup() {
        return newsRepository.findByEndDateIsNull().isEmpty();
    }
    
}
