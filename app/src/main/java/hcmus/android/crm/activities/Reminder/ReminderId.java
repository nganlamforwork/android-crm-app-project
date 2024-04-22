package hcmus.android.crm.activities.Reminder;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.Exclude;

public class ReminderId {
    @Exclude
    public String ReminderId;

    public <T extends ReminderId> T withId(@NonNull final String id) {
        this.ReminderId = id;

        return (T) this;
    }
}
