package hcmus.android.crm.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.PropertyName;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;


public class Template implements Parcelable {

    @PropertyName("name")
    private String name;

    @PropertyName("subject")
    private String subject;

    @PropertyName("body")
    private String body;
    @PropertyName("createdAt")
    @ServerTimestamp
    private Date createdAt;

    // Constructors, getters, and setters

    public Template() {
    }

    public Template(String name, String body, String phone, String address, String job, String company, String notes, String subject, String latitude, String longitude) {
        this.name = name;
        this.subject = subject;
        this.body = body;
    }

    public Template(String name, String subject, String body) {
        this.name = name;
        this.subject = subject;
        this.body = body;
    }

    // Parcelable implementation
    protected Template(Parcel in) {
        name = in.readString();
        subject = in.readString();
        body = in.readString();
        long tmpCreatedAt = in.readLong();
        createdAt = tmpCreatedAt == -1 ? null : new Date(tmpCreatedAt);
    }

    public static final Creator<Template> CREATOR = new Creator<Template>() {
        @Override
        public Template createFromParcel(Parcel in) {
            return new Template(in);
        }

        @Override
        public Template[] newArray(int size) {
            return new Template[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(subject);
        dest.writeString(body);
        dest.writeLong(createdAt != null ? createdAt.getTime() : -1);
    }
}