package at.naurandir.discord.clem.bot.service.client;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

/**
 *
 * @author Naurandir
 */
@Slf4j
@Service
public class WarframeClient {
    
    private final Gson gson = new Gson();
    
    public String getDataRaw(String url, Map<String, String> headers) throws IOException {
        try (CloseableHttpClient httpClient = getClient()) {
            HttpGet httpGet = new HttpGet(url);
            for (Map.Entry<String, String> header : headers.entrySet()) {
                httpGet.addHeader(header.getKey(), header.getValue());
            }
            
            StopWatch watch = new StopWatch();
            watch.start();
            
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                log.info("getDataRaw: received http status [{}] for url [{}]", response.getStatusLine(), url);
                watch.stop();
                log.debug("getDataRaw: needed [{}]ms for getting raw data", watch.getTotalTimeMillis());
                if (response.getStatusLine().getStatusCode() >= 400) {
                    return null;
                }
                return getHttpContent(response);
            }
        }
    }
    
    public <T> T getData(String url, Map<String, String> headers, Class<T> clazz) throws IOException {
        try (CloseableHttpClient httpClient = getClient()) {
            HttpGet httpGet = new HttpGet(url);
            for (Map.Entry<String, String> header : headers.entrySet()) {
                httpGet.addHeader(header.getKey(), header.getValue());
            }
            
            StopWatch watch = new StopWatch();
            watch.start();
            
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                log.info("getData: received http status [{}] for url [{}]", response.getStatusLine(), url);
                String jsonString = getHttpContent(response);
                if (log.isDebugEnabled()) {
                    log.debug("getData: received payload: [{}]", jsonString);
                }
                
                watch.stop();
                log.debug("getData: needed [{}]ms for getting [{}]", watch.getTotalTimeMillis(), clazz);
                
                return gson.fromJson(jsonString, clazz);
            }
        }
    }
    
    public <T, R> T getDataByPost(String url, Map<String, String> headers, R body, Class<T> clazz) throws IOException {
        try (CloseableHttpClient httpClient = getClient()) {
            HttpPost httpPost = new HttpPost(url);
            for (Map.Entry<String, String> header : headers.entrySet()) {
                httpPost.addHeader(header.getKey(), header.getValue());
            }
            
            String stringBody = gson.toJson(body);
            StringEntity entity = new StringEntity(stringBody);
            httpPost.setEntity(entity);
            
            StopWatch watch = new StopWatch();
            watch.start();
            
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                log.info("getDataByPost: received http status [{}] for url [{}]", response.getStatusLine(), url);
                String jsonString = getHttpContent(response);
                
                watch.stop();
                log.debug("getDataByPost: needed [{}]ms for getting [{}]", watch.getTotalTimeMillis(), clazz);
                
                return gson.fromJson(jsonString, clazz);
            }
        }
    }
    
    public <T> List<T> getListData(String url, Map<String, String> headers, Class<T> clazz) throws IOException {
        try (CloseableHttpClient httpClient = getClient()) {
            HttpGet httpGet = new HttpGet(url);
            for (Map.Entry<String, String> header : headers.entrySet()) {
                httpGet.addHeader(header.getKey(), header.getValue());
            }
            
            StopWatch watch = new StopWatch();
            watch.start();
            
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                log.info("getData: received http status [{}] for url [{}]", response.getStatusLine(), url);
                String jsonString = getHttpContent(response);
                
                Type clazzType = TypeToken.getParameterized(List.class, clazz).getType();
                
                watch.stop();
                log.debug("getListData: needed [{}]ms for getting list of [{}]", watch.getTotalTimeMillis(), clazz);
                
                return gson.fromJson(jsonString, clazzType);
            }
        }
    }
    
    CloseableHttpClient getClient() {
        return HttpClients.createDefault();
    }
    
    String getHttpContent(CloseableHttpResponse response) throws IOException {
        return EntityUtils.toString(response.getEntity(), Charset.forName("UTF-8"));
    }
}
