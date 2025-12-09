package com.multimediachat.app.im.plugin;

/** Defines the IDs of branding resources. */
public interface BrandingResourceIDs {

    /** The logo icon of the provider which is displayed in the landing page. */
    int DRAWABLE_LOGO = 100;
    /** The icon of online presence status. */
    int DRAWABLE_PRESENCE_ONLINE = 102;
    /** The icon of busy presence status. */
    int DRAWABLE_PRESENCE_BUSY = 103;
    /** The icon of away presence status. */
    int DRAWABLE_PRESENCE_AWAY = 104;
    /** The icon of invisible presence status. */
    int DRAWABLE_PRESENCE_INVISIBLE = 105;
    /** The icon of offline presence status. */
    int DRAWABLE_PRESENCE_OFFLINE = 106;
    /** The label of the menu to go to the contact list screen. */
    int STRING_MENU_CONTACT_LIST = 107;

    /** The image displayed on the splash screen while logging in. */
    int DRAWABLE_SPLASH_SCREEN = 200;
    /** The icon for blocked contacts. */
    int DRAWABLE_BLOCK = 201;
    /** The water mark background for chat screen. */
    int DRAWABLE_CHAT_WATERMARK = 202;
    /** The icon for the read conversation. */
    int DRAWABLE_READ_CHAT = 203;
    /** The icon for the unread conversation. */
    int DRAWABLE_UNREAD_CHAT = 204;

    /**
     * The title of buddy list screen. It's conjuncted with the current username
     * and should be formatted as a string like
     * "Contact List - &lt;xliff:g id="username"&gt;%1$s&lt;/xliff:g&gt;
     */
    int STRING_BUDDY_LIST_TITLE = 301;

    /** A string array of the smiley names. */
    int STRING_ARRAY_SMILEY_NAMES = 302;
    /** A string array of the smiley texts. */
    int STRING_ARRAY_SMILEY_TEXTS = 303;

    /** The string of available presence status. */
    int STRING_PRESENCE_AVAILABLE = 304;
    /** The string of away presence status. */
    int STRING_PRESENCE_AWAY = 305;
    /** The string of busy presence status. */
    int STRING_PRESENCE_BUSY = 306;
    /** The string of the idle presence status. */
    int STRING_PRESENCE_IDLE = 307;
    /** The string of the invisible presence status. */
    int STRING_PRESENCE_INVISIBLE = 308;
    /** The string of the offline presence status. */
    int STRING_PRESENCE_OFFLINE = 309;

    /** The label of username displayed on the account setup screen. */
    int STRING_LABEL_USERNAME = 310;
    /** The label of the ongoing conversation group. */
    int STRING_ONGOING_CONVERSATION = 311;
    /** The title of add contact screen. */
    int STRING_ADD_CONTACT_TITLE = 312;
    /** The label of the contact input box on the add contact screen. */
    int STRING_LABEL_INPUT_CONTACT = 313;
    /** The label of the add contact button on the add contact screen */
    int STRING_BUTTON_ADD_CONTACT = 314;
    /** The title of the contact info dialog. */
    int STRING_CONTACT_INFO_TITLE = 315;
    /** The label of the menu to add a contact. */
    int STRING_MENU_ADD_CONTACT = 316;
    /** The label of the menu to start a conversation. */
    int STRING_MENU_START_CHAT = 317;
    /** The label of the menu to view contact profile info. */
    int STRING_MENU_VIEW_PROFILE = 318;
    /** The label of the menu to end a conversation. */
    int STRING_MENU_END_CHAT = 319;
    /** The label of the menu to block a contact. */
    int STRING_MENU_BLOCK_CONTACT = 320;
    /** The label of the menu to delete a contact. */
    int STRING_MENU_DELETE_CONTACT = 321;
    /** The label of the menu to insert a smiley. */
    int STRING_MENU_INSERT_SMILEY = 322;
    /** The label of the menu to switch conversations. */
    int STRING_MENU_SWITCH_CHATS = 323;
    /**
     * The string of the toast displayed when auto sign in button on the account
     * setup screen is checked.
     */
    int STRING_TOAST_CHECK_AUTO_SIGN_IN = 324;
    /**
     * The string of the toast displayed when the remember password button on
     * the account setup screen is checked.
     */
    int STRING_TOAST_CHECK_SAVE_PASSWORD = 325;
    /** The label of sign up a new account on the account setup screen. */
    int STRING_LABEL_SIGN_UP = 326;
    /**
     * The term of use message. If provided, a dialog will be shown at the first
     * time login to ask the user if he would accept the term or not.
     */
    int STRING_TOU_MESSAGE = 327;
    /** The title of the term of use dialog. */
    int STRING_TOU_TITLE = 328;
    /** The label of the button to accept the term of use. */
    int STRING_TOU_ACCEPT = 329;
    /** The label of the button to decline the term of use. */
    int STRING_TOU_DECLINE = 330;
}
