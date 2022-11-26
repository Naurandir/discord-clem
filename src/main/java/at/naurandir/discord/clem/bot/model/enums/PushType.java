package at.naurandir.discord.clem.bot.model.enums;

/**
 *
 * @author Naurandir
 */
public enum PushType {
    
    ALERT_PUSH("alert"),
    VOID_FISSURE_PUSH("fissure"),
    VOID_STORM_PUSH("storm"),
    VOID_TRADER_PUSH("trader"),
    WORLD_CYCLE_PUSH("cycle"),
    UPDATE_PIPELINE("updates");
    
    private final String value;
    
    private PushType(String value) {
        this.value = value;
    }
    
    public static PushType getByValue(String value) {
        for (PushType type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("no valid value found for " + value);
    }
    
    public String getValue() {
        return value;
    }
}
