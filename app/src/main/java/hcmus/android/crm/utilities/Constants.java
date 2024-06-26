package hcmus.android.crm.utilities;
import hcmus.android.crm.BuildConfig;

public class Constants {
    public static final String KEY_COLLECTION_USERS = "users";
    public static final String KEY_COLLECTION_LEADS = "leads";
    public static final String KEY_COLLECTION_TAGS = "tags";
    public static final String KEY_COLLECTION_TEMPLATES = "templates";
    public static final String KEY_COLLECTION_CARDS = "cards";
    public static final String KEY_COLLECTION_NOTES = "notes";
    public static final String KEY_COLLECTION_OPPORTUNITIES = "opportunities";
    public static final String KEY_COLLECTION_CONTACTS = "contacts";
    public static final String KEY_COLLECTION_EVENTS = "events";
    public static final String KEY_COLLECTION_CHAT_ROOMS = "chatrooms";
    public static final String KEY_COLLECTION_CHATS = "chats";
    public static final String KEY_COLLECTION_REMINDERS = "reminders";

    public static final String KEY_FCM_TOKEN = "fcmToken";

    public static final String KEY_NAME = "name";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_PHONE_NUMBER = "phone";

    public static final String KEY_PREFERENCE_NAME = "crmAppPreference";
    public static final String KEY_IS_SIGNED_IN = "isSignedIn";
    public static final String KEY_USER_ID = "userId";
    public static final String KEY_IMAGE = "image";
    public static final String URL_FCM_API = "https://fcm.googleapis.com/v1/projects/android-crm-group-10/messages:send";

    public static final String MESSAGING_SCOPE = "https://www.googleapis.com/auth/firebase.messaging";
    public static final String[] SCOPES = { MESSAGING_SCOPE };

    public static final String OPENAI_API_KEY = BuildConfig.OPENAI_API_KEY;
    public static final String END_POINT = "https://api.openai.com/v1/chat/completions";

    public static final String ASSISTANT_PROMPT = "You are a helpful AI assistant, your name is HyperCRM. You are responsible for helping users with their request. " +
            "You are given full information of user's leads, upcoming events and potential opportunities. " +
            "Try your best to assist user and do not hallucinate any information. Only get it from the provided context. " +
            "Summarize important and necessary information, DO NOT answers the whole content. Wrap it up and make it short but informative. " +
            "If no information found, reply with `INFORMATION NOT FOUND`";
    public static final String EMAIL_PROMPT = "You are a helpful AI assistant, your name is HyperCRM." +
            "You are responsible for creating email template based on user requirements. Make sure to follow strictly to user requirement.";

}