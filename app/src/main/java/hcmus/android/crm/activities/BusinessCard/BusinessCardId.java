package hcmus.android.crm.activities.BusinessCard;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.Exclude;

public class BusinessCardId {
    @Exclude
    public String BusinessCardId;

    public <T extends BusinessCardId> T withId(@NonNull final String id) {
        this.BusinessCardId = id;

        return (T) this;
    }
}
