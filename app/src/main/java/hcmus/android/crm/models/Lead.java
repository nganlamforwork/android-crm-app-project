package hcmus.android.crm.models;

import com.google.firebase.firestore.PropertyName;

import hcmus.android.crm.activities.Leads.LeadId;

public class Lead extends LeadId {

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

    public Lead() {
        // Empty constructor needed for Firestore
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
}
