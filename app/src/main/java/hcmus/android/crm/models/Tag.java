package hcmus.android.crm.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.PropertyName;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class Tag implements Parcelable {
    @Exclude // Exclude from Firestore serialization/deserialization
    private String id;
    @PropertyName("title")
    private String title;
    @PropertyName("createdAt")
    @ServerTimestamp
    private Date createdAt;

    public Tag() {
    }

    public Tag(String title) {
        this.title = title;
    }

    // Parcelable implementation
    protected Tag(Parcel in) {
        id = in.readString();
        title = in.readString();
        long tmpCreatedAt = in.readLong();
        createdAt = tmpCreatedAt == -1 ? null : new Date(tmpCreatedAt);
    }

    public static final Creator<Tag> CREATOR = new Creator<Tag>() {
        @Override
        public Tag createFromParcel(Parcel in) {
            return new Tag(in);
        }

        @Override
        public Tag[] newArray(int size) {
            return new Tag[size];
        }
    };

    @Exclude
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(title);
        dest.writeLong(createdAt != null ? createdAt.getTime() : -1);
    }
}