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
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;
import hcmus.android.crm.R;
import hcmus.android.crm.activities.DrawerBaseActivity;
import hcmus.android.crm.databinding.ActivityAddNewBussinessCardAcitivityBinding;
import hcmus.android.crm.models.BusinessCard;
import hcmus.android.crm.utilities.Constants;

public class AddNewBusinessCardActivity extends DrawerBaseActivity {
    private ActivityAddNewBussinessCardAcitivityBinding binding;
    private EditText fullName, aboutMe, company, jobTitle, email, phone, note, cardName;
    private MenuItem createMenuItem;
    private FrameLayout logoImage;

    private FirebaseFirestore db;
    private QRGEncoder qrgEncoder;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle("Add Business Card");

        binding = ActivityAddNewBussinessCardAcitivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = binding.appBar.toolbar;
        setSupportActionBar(toolbar);

        getElementsById();

        db = FirebaseFirestore.getInstance();
        // Enable the back button in the action bar or toolbar
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setTitle("Add Business Card");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }
        setListeners();

    }

    private void setListeners() {
        // Add text change listeners to required fields
        fullName.addTextChangedListener(new AddNewBusinessCardActivity.FieldTextWatcher());
        email.addTextChangedListener(new AddNewBusinessCardActivity.FieldTextWatcher());
        phone.addTextChangedListener(new AddNewBusinessCardActivity.FieldTextWatcher());
        jobTitle.addTextChangedListener(new AddNewBusinessCardActivity.FieldTextWatcher());
        company.addTextChangedListener(new AddNewBusinessCardActivity.FieldTextWatcher());
        cardName.addTextChangedListener(new AddNewBusinessCardActivity.FieldTextWatcher());
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
        getMenuInflater().inflate(R.menu.create, menu);
        createMenuItem = menu.findItem(R.id.action_create);
        createMenuItem.setEnabled(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_create) {
            addBusinessCardToFirestore();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    public static String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }
    private void addBusinessCardToFirestore() {
        String name = fullName.getText().toString().trim();
        String aboutMe = this.aboutMe.getText().toString().trim();
        String email = this.email.getText().toString().trim();
        String phoneNumber = phone.getText().toString().trim();
        String job = jobTitle.getText().toString().trim();
        String companyName = company.getText().toString().trim();
        String note = this.note.getText().toString().trim();
        String cardName = this.cardName.getText().toString().trim();

        BusinessCard newCard = new BusinessCard(name, aboutMe, companyName, job, email, phoneNumber, note, cardName);

        // Object to Json
        Gson gson = new Gson();
        String json = gson.toJson(newCard);

        // Get QR code for card information
        QRGEncoder qrgEncoder = new QRGEncoder(json, null, QRGContents.Type.TEXT, 400);
        try{
            Bitmap bitmap = qrgEncoder.getBitmap(0);
            if (bitmap != null){
                String base64String = bitmapToBase64(bitmap);
                newCard.setQrcode(base64String);
            }
            else
                Log.d("Bitmap QR", "null");
        }catch (Exception ignored){
        }

        // Add card to Firestore
        db.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID))
                .collection(Constants.KEY_COLLECTION_CARDS)
                .add(newCard)
                .addOnSuccessListener(documentReference -> {
                    // Reset fields
                    resetFields();

                    // Hide loading state
                    loading(false);
                    showToast("New business card added successful", 0);
                    Intent intent = new Intent(this, BusinessCardActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    // Handle failure, e.g., show error message
                    loading(false);
                    showToast("Failed to add new business card", 0);
                });
    }

    private void resetFields() {
        // Reset input fields
        fullName.setText("");
        aboutMe.setText("");
        company.setText("");
        jobTitle.setText("");
        email.setText("");
        phone.setText("");
        note.setText("");
        cardName.setText("");
    }


    private void loading(Boolean isLoading) {
        if (isLoading) {
            createMenuItem.setEnabled(false); // Disable createButton
        } else {
            createMenuItem.setEnabled(true); // Enable createButton
        }
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
            createMenuItem.setEnabled(isFieldsFilled());
        }
    }
}