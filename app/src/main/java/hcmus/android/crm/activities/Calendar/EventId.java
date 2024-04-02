package hcmus.android.crm.activities.Calendar;


import androidx.annotation.NonNull;

import com.google.firebase.firestore.Exclude;
public class EventId {
    @Exclude
    public String EventId;

    public <T extends EventId> T withId(@NonNull final String id) {
        this.EventId = id;

        return (T) this;
    }
}
