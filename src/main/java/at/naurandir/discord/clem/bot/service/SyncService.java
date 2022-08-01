package at.naurandir.discord.clem.bot.service;

import java.io.IOException;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;

/**
 *
 * @author Naurandir
 */
public abstract class SyncService {
    
    @Value("${discord.clem.startup.init:false}")
    boolean doInitAtStartup;
    
    @PostConstruct
    public void init() throws IOException {
        if (!doInitAtStartup) {
            return;
        }
        
        if(isFirstTimeStartup()) {
            sync();
        }
    }
    
    public abstract void sync() throws IOException;
    abstract boolean isFirstTimeStartup();
}
