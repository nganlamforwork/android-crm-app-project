package hcmus.android.crm.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.PropertyName;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

import hcmus.android.crm.activities.BusinessCard.BusinessCardId;

public class BusinessCard extends BusinessCardId implements Parcelable {
    @PropertyName("fullname")
    private String fullname;

    @PropertyName("aboutme")
    private String aboutme;

    @PropertyName("company")
    private String company;

    @PropertyName("jobtitle")
    private String jobtitle;

    @PropertyName("email")
    private String email;

    @PropertyName("phone")
    private String phone;

    @PropertyName("note")
    private String note;

    @PropertyName("cardname")
    private String cardname;

    @PropertyName("createdAt")
    @ServerTimestamp
    private Date createdAt;

    // Constructors, getters, and setters

    // Empty constructor needed for Firestore
    public BusinessCard() {
    }

    public BusinessCard(String fullname, String aboutme, String company, String jobtitle, String email, String phone, String note, String cardname) {
        this.fullname = fullname;
        this.aboutme = aboutme;
        this.company = company;
        this.jobtitle = jobtitle;
        this.email = email;
        this.phone = phone;
        this.note = note;
        this.cardname = cardname;
    }

    // Parcelable implementation
    protected BusinessCard(Parcel in) {
        fullname = in.readString();
        aboutme = in.readString();
        company = in.readString();
        jobtitle = in.readString();
        email = in.readString();
        phone = in.readString();
        note = in.readString();
        cardname = in.readString();
        long tmpCreatedAt = in.readLong();
        createdAt = tmpCreatedAt == -1 ? null : new Date(tmpCreatedAt);
    }

    public static final Creator<BusinessCard> CREATOR = new Creator<BusinessCard>() {
        @Override
        public BusinessCard createFromParcel(Parcel in) {
            return new BusinessCard(in);
        }

        @Override
        public BusinessCard[] newArray(int size) {
            return new BusinessCard[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getAboutme() {
        return aboutme;
    }

    public void setAboutme(String aboutme) {
        this.aboutme = aboutme;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getJobtitle() {
        return jobtitle;
    }

    public void setJobtitle(String jobtitle) {
        this.jobtitle = jobtitle;
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

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getCardname() {
        return cardname;
    }

    public void setCardname(String cardname) {
        this.cardname = cardname;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(fullname);
        dest.writeString(aboutme);
        dest.writeString(company);
        dest.writeString(jobtitle);
        dest.writeString(email);
        dest.writeString(phone);
        dest.writeString(note);
        dest.writeString(cardname);
        dest.writeLong(createdAt != null ? createdAt.getTime() : -1);
    }
}
