package hcmus.android.crm.utilities;

import com.google.firebase.auth.FirebaseAuth;

public class FirebaseUtils {

    public static String currentUserId() {
        return FirebaseAuth.getInstance().getUid();
    }

    public static boolean isLoggedIn() {
        if (currentUserId() != null) {
            return true;
        }
        return false;
    }
}
