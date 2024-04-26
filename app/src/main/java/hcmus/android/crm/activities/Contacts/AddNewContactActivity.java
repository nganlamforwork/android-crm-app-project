package hcmus.android.crm.activities.Contacts;

import static hcmus.android.crm.utilities.Utils.encodeImage;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.firestore.FirebaseFirestore;
import com.makeramen.roundedimageview.RoundedImageView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;

import hcmus.android.crm.activities.Contacts.ContactActivity;
import hcmus.android.crm.activities.DrawerBaseActivity;
import hcmus.android.crm.databinding.ActivityAddNewContactBinding;
import hcmus.android.crm.databinding.ActivityAddNewLeadBinding;
import hcmus.android.crm.models.Contact;
import hcmus.android.crm.models.Lead;
import hcmus.android.crm.utilities.Constants;
import hcmus.android.crm.utilities.Utils;


public class AddNewContactActivity extends DrawerBaseActivity {
    private static final int REQUEST_LOCATION = 1;
    private ActivityAddNewContactBinding binding;
    private Contact newContact;
    private EditText contactName, contactEmail, contactPhone, contactNotes;
    private TextView contactLocation;
    private Button newContactSaveButton;
    private ProgressBar progressBar;
    private RoundedImageView contactImage;
    private FrameLayout layoutImage;
    private String encodedImage;
    private SwitchMaterial getContactLocation;
    private boolean isEditMode = false;
    private String contactId;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Add new contact");

        binding = ActivityAddNewContactBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();


        Toolbar toolbar = binding.appBar.toolbar;
        setSupportActionBar(toolbar);

        // Enable the back button in the action bar or toolbar
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }
        // Get element id
        contactName = binding.contactName;
        contactEmail = binding.contactEmail;
        contactPhone = binding.contactPhone;
        contactNotes = binding.contactNotes;
        contactImage = binding.contactImage;
        layoutImage = binding.layoutImage;

        progressBar = binding.progressBar;
        newContactSaveButton = binding.buttonSaveContact;

        contactId = getIntent().getStringExtra("contactId");
        newContact = getIntent().getParcelableExtra("contact");

        if (contactId != null) {
            isEditMode = true;
            setTitle("Edit contact");
            populateEventData();
        }

        setListeners();
    }

    private void populateEventData() {
        contactName.setText(newContact.getName());
        contactEmail.setText(newContact.getEmail());
        contactPhone.setText(newContact.getPhone());
        contactNotes.setText(newContact.getNotes());

        // Decode base64 encoded image string and set it to ImageView
        String imageString = newContact.getImage();
        if (imageString != null && !imageString.isEmpty()) {
            byte[] bytes = Base64.decode(imageString, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            encodedImage = encodeImage(bitmap);
            contactImage.setImageBitmap(bitmap);
        }
    }

    private void setListeners() {
        newContactSaveButton.setEnabled(false); // Initially disable the button

        // Add text change listeners to required fields
        contactName.addTextChangedListener(new FieldTextWatcher());
        contactEmail.addTextChangedListener(new FieldTextWatcher());
        contactPhone.addTextChangedListener(new FieldTextWatcher());

        layoutImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });

        if (isEditMode) {
            newContactSaveButton.setText("Update Contact");
            newContactSaveButton.setOnClickListener(v -> {
                loading(true);
                handleUpdateContact();
            });
        } else {
            newContactSaveButton.setText("Save Contact");
            newContactSaveButton.setOnClickListener(v -> {
                loading(true);
                handleAddNewContact();
            });
        }
    }

    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    if (result.getData() != null) {
                        // Get the URI of the selected image
                        Uri imageUri = result.getData().getData();

                        // Process the selected image (in your case, you're encoding it and updating Firestore)
                        try {
                            // Open an input stream for the selected image URI
                            assert imageUri != null;
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);

                            // Decode the input stream into a Bitmap
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                            // Encode the bitmap to a base64 string (if needed)
                            encodedImage = encodeImage(bitmap);

                            // Update the ImageView with the selected image
                            contactImage.setImageBitmap(bitmap);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
    );

    private void loading(Boolean isLoading) {
        if (isLoading) {
            newContactSaveButton.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.INVISIBLE);
            newContactSaveButton.setVisibility(View.VISIBLE);
        }
    }

    private boolean isFieldsFilled() {
        // Check if all required fields are filled
        return !contactName.getText().toString().trim().isEmpty() &&
                !contactEmail.getText().toString().trim().isEmpty() &&
                !contactPhone.getText().toString().trim().isEmpty();
    }

    private void handleUpdateContact() {
        // Get input values
        String name = contactName.getText().toString().trim();
        String email = contactEmail.getText().toString().trim();
        String phone = contactPhone.getText().toString().trim();
        String notes = contactNotes.getText().toString().trim();

        if (!isFieldsFilled()) {
            loading(false);
            showToast("All field is required", 0);
            return;
        }

        Contact updatedContact = new Contact();
        updatedContact.setName(name);
        updatedContact.setEmail(email);
        updatedContact.setPhone(phone);
        updatedContact.setNotes(notes);
        updatedContact.setImage(encodedImage);


        db.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID))
                .collection(Constants.KEY_COLLECTION_CONTACTS)
                .document(contactId)
                .set(updatedContact)
                .addOnSuccessListener(documentReference -> {
                    // Reset fields
                    resetFields();

                    // Hide loading state
                    loading(false);
                    showToast("Contact updated successful", 0);

                    // Send back the new contact data to ContactDetailActivity
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("updatedContact", updatedContact);
                    setResult(Activity.RESULT_OK, resultIntent);

                    finish();
                })
                .addOnFailureListener(e -> {
                    // Handle failure, e.g., show error message
                    loading(false);
                    showToast("Failed to update contact", 0);
                });
    }

    private void handleAddNewContact() {
        // Get input values
        String name = contactName.getText().toString().trim();
        String email = contactEmail.getText().toString().trim();
        String phone = contactPhone.getText().toString().trim();
        String notes = contactNotes.getText().toString().trim();

        if (!isFieldsFilled()) {
            loading(false);
            showToast("All field is required", 0);
            return;
        }

        newContact = new Contact();
        newContact.setName(name);
        newContact.setEmail(email);
        newContact.setPhone(phone);
        newContact.setNotes(notes);
        newContact.setImage(encodedImage);


        db.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID))
                .collection(Constants.KEY_COLLECTION_CONTACTS)
                .add(newContact)
                .addOnSuccessListener(documentReference -> {
                    // Reset fields
                    resetFields();

                    // Hide loading state
                    loading(false);
                    showToast("New contact added successful", 0);
                    Intent intent = new Intent(this, ContactActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                })
                .addOnFailureListener(e -> {
                    // Handle failure, e.g., show error message
                    loading(false);
                    showToast("Failed to add new contact", 0);
                });
    }


    private void resetFields() {
        // Reset input fields
        contactName.setText("");
        contactEmail.setText("");
        contactPhone.setText("");
        contactNotes.setText("");
        // Reset the image view
        contactImage.setImageResource(android.R.color.transparent);

        // Clear the encoded image
        encodedImage = "";

        // Disable save button
        newContactSaveButton.setEnabled(false);
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
            newContactSaveButton.setEnabled(isFieldsFilled());
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }

}
