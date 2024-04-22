package hcmus.android.crm.models;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.PropertyName;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class Opportunity implements Parcelable {

    @PropertyName("name")
    private String name;

    @PropertyName("status")
    private String status;

    @PropertyName("price")
    private Double price;

    @PropertyName("possibility")
    private Double possibility;

    @PropertyName("expectedDate")
    private String expectedDate;


    @PropertyName("createdAt")
    @ServerTimestamp
    private Date createdAt;

    // Constructors, getters, and setters

    // Empty constructor needed for Firestore
    public Opportunity() {
    }

    public Opportunity(String name, String status, Double price, Double possibility, String expectedDate) {
        this.name = name;
        this.status = status;
        this.price = price;
        this.possibility = possibility;
        this.expectedDate = expectedDate;
    }

    // Parcelable implementation
    @SuppressLint("NewApi")
    protected Opportunity(Parcel in) {
        name = in.readString();
        status = in.readString();
        price = in.readDouble();
        possibility = in.readDouble();
        expectedDate = in.readString();
        long tmpCreatedAt = in.readLong();
        createdAt = tmpCreatedAt == -1 ? null : new Date(tmpCreatedAt);
    }

    public static final Creator<Opportunity> CREATOR = new Creator<Opportunity>() {
        @Override
        public Opportunity createFromParcel(Parcel in) {
            return new Opportunity(in);
        }

        @Override
        public Opportunity[] newArray(int size) {
            return new Opportunity[size];
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Double getPossibility() {
        return possibility;
    }

    public void setPossibility(Double possibility) {
        this.possibility = possibility;
    }

    public String getExpectedDate() {
        return expectedDate;
    }

    public void setExpectedDate(String expectedDate) {
        this.expectedDate = expectedDate;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @SuppressLint("NewApi")
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(status);
        dest.writeDouble(price);
        dest.writeDouble(possibility);
        dest.writeString(expectedDate);
        dest.writeLong(createdAt != null ? createdAt.getTime() : -1);
    }
}

