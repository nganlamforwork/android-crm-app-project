package hcmus.android.crm.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.PropertyName;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

import hcmus.android.crm.activities.Leads.LeadId;

public class Lead extends LeadId implements Parcelable {

    @PropertyName("name")
    private String name;

    @PropertyName("image")
    private String image;

    @PropertyName("email")
    private String email;

    @PropertyName("phone")
    private String phone;

    @PropertyName("address")
    private String address;

    @PropertyName("job")
    private String job;

    @PropertyName("company")
    private String company;

    @PropertyName("notes")
    private String notes;

    @PropertyName("latitude")
    private String latitude;

    @PropertyName("longitude")
    private String longitude;

    @PropertyName("createdAt")
    @ServerTimestamp
    private Date createdAt;

    // Constructors, getters, and setters

    public Lead() {
    }

    public Lead(String name, String email, String phone, String address, String job, String company, String notes, String image, String latitude, String longitude) {
        this.name = name;
        this.image = image;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.job = job;
        this.company = company;
        this.notes = notes;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Parcelable implementation
    protected Lead(Parcel in) {
        name = in.readString();
        image = in.readString();
        email = in.readString();
        phone = in.readString();
        address = in.readString();
        job = in.readString();
        company = in.readString();
        notes = in.readString();
        latitude = in.readString();
        longitude = in.readString();
        long tmpCreatedAt = in.readLong();
        createdAt = tmpCreatedAt == -1 ? null : new Date(tmpCreatedAt);
    }

    public static final Creator<Lead> CREATOR = new Creator<Lead>() {
        @Override
        public Lead createFromParcel(Parcel in) {
            return new Lead(in);
        }

        @Override
        public Lead[] newArray(int size) {
            return new Lead[size];
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getJob() {
        return job;
    }

    public void setJob(String job) {
        this.job = job;
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

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
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
        dest.writeString(address);
        dest.writeString(job);
        dest.writeString(company);
        dest.writeString(notes);
        dest.writeString(latitude);
        dest.writeString(longitude);
        dest.writeLong(createdAt != null ? createdAt.getTime() : -1);
    }
}
