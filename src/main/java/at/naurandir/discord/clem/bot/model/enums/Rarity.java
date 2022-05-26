package at.naurandir.discord.clem.bot.model.enums;

import lombok.AllArgsConstructor;

/**
 *
 * @author Naurandir
 */
@AllArgsConstructor
public enum Rarity {
    COMMON("Common"),
    UNCOMMON("Uncommon"),
    RARE("Rare");
    
    private String value;
    
    public Rarity getRarityByEnum(String input) {
        for (Rarity rarity : Rarity.values()) {
            if (rarity.value.equals(input)) {
                return rarity;
            }
        }
        
        throw new IllegalArgumentException("Given input ["+input+"] is not a valid enum!");
    }
}
