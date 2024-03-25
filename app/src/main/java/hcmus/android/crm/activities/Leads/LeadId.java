package hcmus.android.crm.activities.Leads;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.Exclude;

public class LeadId {
    @Exclude
    public String LeadId;

    public <T extends LeadId> T withId(@NonNull final String id) {
        this.LeadId = id;

        return (T) this;
    }
}
