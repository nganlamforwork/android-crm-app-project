package hcmus.android.crm.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.PropertyName;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

import hcmus.android.crm.activities.Contacts.ContactId;

public class Contact extends ContactId implements Parcelable {

    @PropertyName("name")
    private String name;

    @PropertyName("image")
    private String image;

    @PropertyName("email")
    private String email;

    @PropertyName("phone")
    private String phone;

    @PropertyName("company")
    private String company;

    @PropertyName("notes")
    private String notes;
    @PropertyName("createdAt")
    @ServerTimestamp
    private Date createdAt;

    // Constructors, getters, and setters

    // Empty constructor needed for Firestore
    public Contact() {
    }

    public Contact(String name, String email, String phone, String company, String notes, String image) {
        this.name = name;
        this.image = image;
        this.email = email;
        this.phone = phone;
        this.company = company;
        this.notes = notes;
    }

    // Parcelable implementation
    protected Contact(Parcel in) {
        name = in.readString();
        image = in.readString();
        email = in.readString();
        phone = in.readString();
        company = in.readString();
        notes = in.readString();
        long tmpCreatedAt = in.readLong();
        createdAt = tmpCreatedAt == -1 ? null : new Date(tmpCreatedAt);
    }

    public static final Creator<Contact> CREATOR = new Creator<Contact>() {
        @Override
        public Contact createFromParcel(Parcel in) {
            return new Contact(in);
        }

        @Override
        public Contact[] newArray(int size) {
            return new Contact[size];
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

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
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
        dest.writeString(image);
        dest.writeString(email);
        dest.writeString(phone);
        dest.writeString(company);
        dest.writeString(notes);
        dest.writeLong(createdAt != null ? createdAt.getTime() : -1);
    }

    @NonNull
    @Override
    public String toString() {
        return name + "|" + phone + "|" + image;
    }
}
