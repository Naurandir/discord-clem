package at.naurandir.discord.clem.bot.service;

import at.naurandir.discord.clem.bot.service.client.WarframeClient;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import org.mockito.Mock;
import static org.mockito.Mockito.when;
/**
 *
 * @author Naurandir
 */
public abstract class BaseServiceTest {
    
    @Mock
    private WarframeClient warframeClient;
    
    private final Gson gson = new Gson();
    
    @BeforeEach
    public void setup() throws IOException, URISyntaxException {
        //
    }
    
    void setWarframeClientGetDataMock(String responseFilePath, Class clazz) throws IOException {
        Path resourceDirectory = Paths.get("src","test","resources", responseFilePath);
        String json = Files.readString(resourceDirectory, Charset.forName("utf-8"));
        
        when(warframeClient.getData(any(), any(), eq(clazz)))
                .thenReturn(gson.fromJson(json, clazz));
    }
    
    void setWarframeClientGetListMock(String responseFilePath, Class clazz) throws IOException {
        Path resourceDirectory = Paths.get("src","test","resources", responseFilePath);
        String json = Files.readString(resourceDirectory, Charset.forName("utf-8"));
        Type clazzType = TypeToken.getParameterized(List.class, clazz).getType();
        
        when(warframeClient.getListData(any(), any(), eq(clazz)))
                .thenReturn(gson.fromJson(json, clazzType));
    }
    
    void setWarframeClientGetRawMock(String responseFilePath) throws IOException, URISyntaxException {
        Path resourceDirectory = Paths.get("src","test","resources", responseFilePath);
        String json = Files.readString(resourceDirectory, Charset.forName("utf-8"));
        
        when(warframeClient.getDataRaw(any(), any()))
                .thenReturn(json);
    }
    

}
