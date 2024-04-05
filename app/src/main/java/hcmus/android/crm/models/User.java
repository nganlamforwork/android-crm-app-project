package hcmus.android.crm.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.PropertyName;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class User implements Parcelable {

    @PropertyName("name")
    private String name;

    @PropertyName("image")
    private String image;

    @PropertyName("email")
    private String email;

    @PropertyName("phone")
    private String phone;

    @PropertyName("userId")
    private String userId;
    @PropertyName("createdAt")
    @ServerTimestamp
    private Date createdAt;
    @PropertyName("fcmToken")

    private String fcmToken;

    public User() {
    }

    public User(String name, String image, String email, String phone, String userId) {
        this.name = name;
        this.image = image;
        this.email = email;
        this.phone = phone;
        this.userId = userId;
    }

    // Parcelable implementation
    protected User(Parcel in) {
        userId = in.readString();
        name = in.readString();
        image = in.readString();
        email = in.readString();
        phone = in.readString();
        long tmpCreatedAt = in.readLong();
        createdAt = tmpCreatedAt == -1 ? null : new Date(tmpCreatedAt);
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }


    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(userId);
        dest.writeString(name);
        dest.writeString(image);
        dest.writeString(email);
        dest.writeString(phone);
        dest.writeLong(createdAt != null ? createdAt.getTime() : -1);
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }
}
