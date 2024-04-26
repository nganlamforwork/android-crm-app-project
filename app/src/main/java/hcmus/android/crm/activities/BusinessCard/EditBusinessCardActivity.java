package hcmus.android.crm.activities.BusinessCard;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;
import hcmus.android.crm.R;
import hcmus.android.crm.activities.DrawerBaseActivity;
import hcmus.android.crm.databinding.ActivityEditBusinessCardBinding;
import hcmus.android.crm.models.BusinessCard;
import hcmus.android.crm.utilities.Constants;

public class EditBusinessCardActivity extends DrawerBaseActivity {
    private ActivityEditBusinessCardBinding binding;
    private EditText fullName, aboutMe, company, jobTitle, email, phone, note, cardName;
    private MenuItem saveMenuItem;
    private FirebaseFirestore db;

    private String businessCardId;
    private BusinessCard businessCard;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle("Edit Business Card");

        binding = ActivityEditBusinessCardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = binding.appBar.toolbar;
        setSupportActionBar(toolbar);

        getElementsById();

        db = FirebaseFirestore.getInstance();
        // Enable the back button in the action bar or toolbar
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setTitle("Edit Business Card");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        Intent intent = this.getIntent();
        if (intent != null) {
            businessCardId = intent.getStringExtra("businessCardId");
            businessCard = intent.getParcelableExtra("businessCard");

            displayDataOnUI(businessCard);
        }

        setListeners();
    }

    private void setListeners() {
        // Add text change listeners to required fields
        fullName.addTextChangedListener(new EditBusinessCardActivity.FieldTextWatcher());
        email.addTextChangedListener(new EditBusinessCardActivity.FieldTextWatcher());
        phone.addTextChangedListener(new EditBusinessCardActivity.FieldTextWatcher());
        jobTitle.addTextChangedListener(new EditBusinessCardActivity.FieldTextWatcher());
        company.addTextChangedListener(new EditBusinessCardActivity.FieldTextWatcher());
        cardName.addTextChangedListener(new EditBusinessCardActivity.FieldTextWatcher());
    }

    private void getElementsById() {
        // Basic fields
        fullName = binding.fullName;
        aboutMe = binding.aboutMe;
        company = binding.company;
        jobTitle = binding.jobTitle;
        email = binding.email;
        phone = binding.phone;
        note = binding.notes;
        cardName = binding.cardName;
    }

    private void displayDataOnUI(BusinessCard businessCard) {
        if (businessCard != null) {
            fullName.setText(businessCard.getFullname());
            aboutMe.setText(businessCard.getAboutme());
            company.setText(businessCard.getCompany());
            jobTitle.setText(businessCard.getJobTitle());
            email.setText(businessCard.getEmail());
            phone.setText(businessCard.getPhone());
            note.setText(businessCard.getNote());
            cardName.setText(businessCard.getCardname());
        }
    }

    protected void onResume() {
        super.onResume();
        navigationView.setCheckedItem(R.id.nav_business_card);
    }

    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.save, menu);
        saveMenuItem = menu.findItem(R.id.action_save);
        saveMenuItem.setEnabled(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_save) {
            if (isFieldsFilled()) {
                updateBusinessCard();
                return true;
            } else {
                showToast("Please fill in all required fields", 0);
                return false;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateBusinessCard() {
        String name = fullName.getText().toString().trim();
        String aboutMe = this.aboutMe.getText().toString().trim();
        String email = this.email.getText().toString().trim();
        String phoneNumber = phone.getText().toString().trim();
        String job = jobTitle.getText().toString().trim();
        String companyName = company.getText().toString().trim();
        String note = this.note.getText().toString().trim();
        String cardName = this.cardName.getText().toString().trim();

        businessCard.setFullname(name);
        businessCard.setAboutme(aboutMe);
        businessCard.setEmail(email);
        businessCard.setPhone(phoneNumber);
        businessCard.setJobTitle(job);
        businessCard.setCompany(companyName);
        businessCard.setNote(note);
        businessCard.setCardname(cardName);
        businessCard.setQrcode(null);

        // Object to Json
        Gson gson = new Gson();
        String json = gson.toJson(businessCard);

        // Get QR code for card information
        QRGEncoder qrgEncoder = new QRGEncoder(json, null, QRGContents.Type.TEXT, 600);
        try {
            Bitmap bitmap = qrgEncoder.getBitmap(0);
            if (bitmap != null) {
                String base64String = bitmapToBase64(bitmap);
                businessCard.setQrcode(base64String);
            } else
                Log.d("Bitmap QR", "null");
        } catch (Exception ignored) {
        }

        db.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID))
                .collection(Constants.KEY_COLLECTION_CARDS)
                .document(businessCardId)
                .set(businessCard)
                .addOnSuccessListener(documentReference -> {
                    showToast("Business card updated successfully!", 0);
                    onBackPressed();
                })
                .addOnFailureListener(e -> {
                    showToast("Failed to update business card!",0);
                });
    }

    public static String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private boolean isFieldsFilled() {
        // Check if all required fields are filled
        return !fullName.getText().toString().trim().isEmpty() &&
                !email.getText().toString().trim().isEmpty() &&
                !phone.getText().toString().trim().isEmpty() &&
                !jobTitle.getText().toString().trim().isEmpty() &&
                !company.getText().toString().trim().isEmpty() &&
                !cardName.getText().toString().trim().isEmpty();
    }

    private class FieldTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            // Enable/disable the button based on field content
            saveMenuItem.setEnabled(isFieldsFilled());
        }
    }
}