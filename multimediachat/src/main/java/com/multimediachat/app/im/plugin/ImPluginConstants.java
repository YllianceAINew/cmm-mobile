package com.multimediachat.app.im.plugin;

public class ImPluginConstants {

    /** The intent action name for the plugin service. */
    public static final String PLUGIN_ACTION_NAME = "com.multimediachat.app.im.plugin";

    /**
     * The name of the provider. It should match the values defined in
     * {@link com.multimediachat.app.im.provider.Imps.ProviderNames}.
     */
    public static final String METADATA_PROVIDER_NAME = "com.multimediachat.app.im.provider_name";

    /** The full name of the provider. */
    public static final String METADATA_PROVIDER_FULL_NAME = "com.multimediachat.app.im.provider_full_name";

    /** The url where the user can register a new account for the provider. */
    public static final String METADATA_SIGN_UP_URL = "com.multimediachat.app.im.signup_url";

    /**
     * Presence status OFFLINE. Should match the value defined in the IM engine.
     */
    public static final int PRESENCE_OFFLINE = 0;

    /**
     * Presence status DO_NOT_DISTURB. Should match the value defined in the IM
     * engine.
     */
    public static final int PRESENCE_DO_NOT_DISTURB = 1;

    /** Presence status AWAY. Should match the value defined in the IM engine. */
    public static final int PRESENCE_AWAY = 2;

    /** Presence status IDLE. Should match the value defined in the IM engine. */
    public static final int PRESENCE_IDLE = 3;

    /**
     * Presence status AVAILABLE. Should match the value defined in the IM
     * engine.
     */
    public static final int PRESENCE_AVAILABLE = 4;

    public static final String PA_AVAILABLE = "AVAILABLE";
    public static final String PA_NOT_AVAILABLE = "NOT_AVAILABLE";
    public static final String PA_DISCREET = "DISCREET";
}
