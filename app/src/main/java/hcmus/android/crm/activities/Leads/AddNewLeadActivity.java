package hcmus.android.crm.activities.Leads;

import static hcmus.android.crm.utilities.Utils.encodeImage;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.makeramen.roundedimageview.RoundedImageView;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Objects;

import hcmus.android.crm.R;
import hcmus.android.crm.databinding.ActivityAddNewLeadBinding;
import hcmus.android.crm.models.Lead;
import hcmus.android.crm.utilities.Constants;
import hcmus.android.crm.utilities.Utils;


public class AddNewLeadActivity extends AppCompatActivity {
    private ActivityAddNewLeadBinding binding;
    private Lead newLead;
    private EditText leadName, leadEmail, leadPhone, leadAddress, leadJob, leadCompany, leadNotes;
    private Button newLeadSaveButton;
    private ProgressBar progressBar;
    private RoundedImageView leadImage;
    private FrameLayout layoutImage;
    private String encodedImage;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Add new lead");

        binding = ActivityAddNewLeadBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Get element id
        leadName = binding.leadName;
        leadEmail = binding.leadEmail;
        leadPhone = binding.leadPhone;
        leadAddress = binding.leadAddress;
        leadJob = binding.leadJob;
        leadCompany = binding.leadCompany;
        leadNotes = binding.leadNotes;

        leadImage = binding.leadImage;
        layoutImage = binding.layoutImage;

        progressBar = binding.progressBar;
        newLeadSaveButton = binding.buttonSaveLead;

        // Handling logic here
        db = FirebaseFirestore.getInstance();

        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);

        // Enable the back button in the action bar or toolbar
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setTitle("Add new lead");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        setListeners();
    }

    private void setListeners() {
        newLeadSaveButton.setEnabled(false); // Initially disable the button

        // Add text change listeners to required fields
        leadName.addTextChangedListener(new FieldTextWatcher());
        leadEmail.addTextChangedListener(new FieldTextWatcher());
        leadPhone.addTextChangedListener(new FieldTextWatcher());
        leadAddress.addTextChangedListener(new FieldTextWatcher());

        layoutImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });

        newLeadSaveButton.setOnClickListener(v -> {
            if (isFieldsFilled()) {
                // Show loading state
                loading(true);

                // Get input values
                String name = leadName.getText().toString().trim();
                String email = leadEmail.getText().toString().trim();
                String phone = leadPhone.getText().toString().trim();
                String address = leadAddress.getText().toString().trim();
                String job = leadJob.getText().toString().trim();
                String company = leadCompany.getText().toString().trim();
                String notes = leadNotes.getText().toString().trim();

                // Add lead to Firestore
                addLeadToFirestore(name, email, phone, address, job, company, notes, encodedImage);
            }
        });
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
                            leadImage.setImageBitmap(bitmap);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
    );

    private void loading(Boolean isLoading) {
        if (isLoading) {
            newLeadSaveButton.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.INVISIBLE);
            newLeadSaveButton.setVisibility(View.VISIBLE);
        }
    }

    private boolean isFieldsFilled() {
        // Check if all required fields are filled
        return !leadName.getText().toString().trim().isEmpty() &&
                !leadEmail.getText().toString().trim().isEmpty() &&
                !leadPhone.getText().toString().trim().isEmpty() &&
                !leadAddress.getText().toString().trim().isEmpty() &&
                encodedImage != null && !encodedImage.isEmpty();
    }

    private void showToast(String message, int length) {
        Utils.showToast(getApplicationContext(), message, length);
    }

    private void addLeadToFirestore(String name, String email, String phone, String address, String job, String company, String notes, String image) {
        // Add lead to Firestore
        newLead = new Lead(name, email, phone, address, job, company, notes, image);
        db.collection(Constants.KEY_COLLECTION_LEADS)
                .add(newLead)
                .addOnSuccessListener(documentReference -> {
                    // Reset fields
                    resetFields();

                    // Hide loading state
                    loading(false);
                    showToast("New lead added successful", 0);

                    // Send back the new lead data to LeadActivity
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("newLead", newLead);
                    setResult(Activity.RESULT_OK, resultIntent);

                    finish();
                })
                .addOnFailureListener(e -> {
                    // Handle failure, e.g., show error message
                    loading(false);
                    showToast("Failed to add new lead", 0);
                });
    }

    private void resetFields() {
        // Reset input fields
        leadName.setText("");
        leadEmail.setText("");
        leadPhone.setText("");
        leadAddress.setText("");
        leadJob.setText("");
        leadCompany.setText("");
        leadNotes.setText("");
        // Reset the image view
        leadImage.setImageResource(android.R.color.transparent);

        // Clear the encoded image
        encodedImage = "";

        // Disable save button
        newLeadSaveButton.setEnabled(false);
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
            newLeadSaveButton.setEnabled(isFieldsFilled());
        }
    }
}
