package hcmus.android.crm.activities.Contacts;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.Exclude;

public class ContactId {
    @Exclude
    public String ContactId;

    public <T extends ContactId> T withId(@NonNull final String id) {
        this.ContactId = id;

        return (T) this;
    }
}
