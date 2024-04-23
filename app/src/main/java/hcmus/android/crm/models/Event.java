package hcmus.android.crm.models;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.PropertyName;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class Event implements Parcelable {

    @PropertyName("name")
    private String name;

    @PropertyName("description")
    private String description;

    @PropertyName("location")
    private String location;

    @PropertyName("date")
    private String date;

    @PropertyName("time")
    private String time;

    @PropertyName("isPassed")
    private boolean isPassed;

    @PropertyName("createdAt")
    @ServerTimestamp
    private Date createdAt;

    @PropertyName("reminderTime")
    private String reminderTime;


    // Constructors, getters, and setters

    // Empty constructor needed for Firestore
    public Event() {
    }

    public Event(String name, String description, String location, String date, String time, boolean isPassed) {
        this.name = name;
        this.description = description;
        this.location = location;
        this.date = date;
        this.time = time;
        this.isPassed = isPassed;
    }

    // Parcelable implementation
    @SuppressLint("NewApi")
    protected Event(Parcel in) {
        name = in.readString();
        description = in.readString();
        location = in.readString();
        long tmpDate = in.readLong();
        date = in.readString();
        time = in.readString();
        isPassed = in.readBoolean();
        long tmpCreatedAt = in.readLong();
        createdAt = tmpCreatedAt == -1 ? null : new Date(tmpCreatedAt);
        reminderTime = in.readString();
    }

    public static final Creator<Event> CREATOR = new Creator<Event>() {
        @Override
        public Event createFromParcel(Parcel in) {
            return new Event(in);
        }

        @Override
        public Event[] newArray(int size) {
            return new Event[size];
        }
    };


    public boolean isPassed() {
        return isPassed;
    }

    public void setPassed(boolean passed) {
        isPassed = passed;
    }

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getReminderTime() {
        return reminderTime;
    }

    public void setReminderTime(String reminderTime) {
        this.reminderTime = reminderTime;
    }


    @SuppressLint("NewApi")
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(description);
        dest.writeString(location);
        dest.writeString(date);
        dest.writeString(time);
        dest.writeBoolean(isPassed);
        dest.writeLong(createdAt != null ? createdAt.getTime() : -1);
        dest.writeString(reminderTime);

    }
}
