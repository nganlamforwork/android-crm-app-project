package hcmus.android.crm.models;


import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.firebase.database.PropertyName;

import java.util.Date;

import hcmus.android.crm.activities.Contacts.ContactId;
import hcmus.android.crm.activities.Reminder.ReminderId;

public class Reminder implements Parcelable {

    @PropertyName("reminderTitle")
    String reminderTitle;
    @PropertyName("date")
    String date;
    @PropertyName("reminderDescription")
    String reminderDescrption;
    @PropertyName("isComplete")
    boolean isComplete;
    @PropertyName("timeAlarm")
    String timeAlarm;
    @PropertyName("event")
    String event;

    public Reminder() {

    }

    public Reminder(String reminderTitle, String date, String reminderDescrption, boolean isComplete, String firstAlarmTime, String secondAlarmTime, String lastAlarm, String event) {
        this.reminderTitle = reminderTitle;
        this.date = date;
        this.reminderDescrption = reminderDescrption;
        this.isComplete = isComplete;
        this.timeAlarm = lastAlarm;
        this.event = event;
    }


    protected Reminder(Parcel in) {
        reminderTitle = in.readString();
        date = in.readString();
        reminderDescrption = in.readString();
        isComplete = in.readByte() != 0;
        timeAlarm = in.readString();
        event = in.readString();
    }

    public static final Creator<Reminder> CREATOR = new Creator<Reminder>() {
        @Override
        public Reminder createFromParcel(Parcel in) {
            return new Reminder(in);
        }

        @Override
        public Reminder[] newArray(int size) {
            return new Reminder[size];
        }
    };

    public boolean isComplete() {
        return isComplete;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public void setComplete(boolean complete) {
        isComplete = complete;
    }

    public String getTimeAlarm() {
        return timeAlarm;
    }

    public void setTimeAlarm(String lastAlarm) {
        this.timeAlarm = lastAlarm;
    }

    public String getReminderTitle() {
        return reminderTitle;
    }

    public void setReminderTitle(String reminderTitle) {
        this.reminderTitle = reminderTitle;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getReminderDescrption() {
        return reminderDescrption;
    }

    public void setReminderDescrption(String reminderDescrption) {
        this.reminderDescrption = reminderDescrption;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(reminderTitle);
        dest.writeString(date);
        dest.writeString(reminderDescrption);
        dest.writeString(String.valueOf(isComplete));
        dest.writeString(timeAlarm);
        dest.writeString(event);
    }
}