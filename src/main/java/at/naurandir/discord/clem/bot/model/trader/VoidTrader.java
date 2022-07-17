package at.naurandir.discord.clem.bot.model.trader;

import at.naurandir.discord.clem.bot.model.DbEntity;
import java.time.LocalDateTime;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author Naurandir
 */
@Getter
@Setter
@Entity
@Table(name = "void_trader")
public class VoidTrader extends DbEntity {

    @Column(nullable = false)
    private LocalDateTime activation;
    
    @Column(nullable = false)
    private LocalDateTime expiry;
    
    @Column(nullable = false)
    private String location;
    
    @OneToMany(mappedBy="voidTrader", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<VoidTraderItem> inventory;
}
