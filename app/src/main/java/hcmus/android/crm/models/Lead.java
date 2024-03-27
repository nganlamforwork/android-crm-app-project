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

    @PropertyName("createdAt")
    @ServerTimestamp
    private Date createdAt;

    // Constructors, getters, and setters

    // Empty constructor needed for Firestore
    public Lead() {
    }

    public Lead(String name, String email, String phone, String address, String job, String company, String notes, String image) {
        this.name = name;
        this.image = image;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.job = job;
        this.company = company;
        this.notes = notes;
    }

    // Getters and setters for fields

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }

    public String getJob() {
        return job;
    }

    public String getCompany() {
        return company;
    }

    public String getNotes() {
        return notes;
    }

    public String getImage() {
        return image;
    }

    public Date getCreatedAt() {
        return createdAt;
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
        dest.writeLong(createdAt != null ? createdAt.getTime() : -1);
    }
}
