package at.naurandir.discord.clem.bot.controller;

/**
 *
 * @author Naurandir
 */
public class ControllerEndpoint {
    
    public static final String REFRESH = "/refresh";
    public static final String REFRESH_VOID_TRADER = REFRESH + "/void_trader";
    public static final String REFRESH_VOID_RELICS = REFRESH + "/void_relics";
    public static final String REFRESH_ALERTS = REFRESH + "/alerts";
    
    private ControllerEndpoint() {
        //
    }
}
