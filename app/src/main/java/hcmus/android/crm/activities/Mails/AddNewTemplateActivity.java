package hcmus.android.crm.activities.Mails;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;
import hcmus.android.crm.R;
import hcmus.android.crm.activities.BusinessCard.AddNewBusinessCardActivity;
import hcmus.android.crm.activities.BusinessCard.BusinessCardActivity;
import hcmus.android.crm.activities.DrawerBaseActivity;
import hcmus.android.crm.databinding.ActivityAddNewBussinessCardAcitivityBinding;
import hcmus.android.crm.databinding.ActivityAddNewTemplateBinding;
import hcmus.android.crm.models.BusinessCard;
import hcmus.android.crm.models.Template;
import hcmus.android.crm.utilities.Constants;

public class AddNewTemplateActivity extends DrawerBaseActivity {
    private static final int GENERATE_AI_TEMPLATE_REQUEST_CODE = 101;
    private ActivityAddNewTemplateBinding binding;
    private EditText templateName, templateSubject, templateBody;
    private Button createButton;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle("Add New Template");

        binding = ActivityAddNewTemplateBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = binding.appBar.toolbar;
        setSupportActionBar(toolbar);

        getElementsById();

        db = FirebaseFirestore.getInstance();
        // Enable the back button in the action bar or toolbar
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setTitle("Add New Template");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }
        setListeners();
    }
    private void getElementsById() {
        // Basic fields
        templateName = binding.templateName;
        templateSubject = binding.templateSubject;
        templateBody = binding.templateBody;

        // Create Button
        createButton = binding.createButton;
        createButton.setEnabled(false);
    }

    private void setListeners() {
        // Add text change listeners to required fields
        templateName.addTextChangedListener(new AddNewTemplateActivity.FieldTextWatcher());
        templateSubject.addTextChangedListener(new AddNewTemplateActivity.FieldTextWatcher());
        templateBody.addTextChangedListener(new AddNewTemplateActivity.FieldTextWatcher());

        // Set onClick listener to createButton
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addTemplateToFirestore();
            }
        });
    }

    private void addTemplateToFirestore() {
        String templateName = this.templateName.getText().toString().trim();
        String templateSubject = this.templateSubject.getText().toString().trim();
        String templateBody = this.templateBody.getText().toString().trim();

        Template newTemplate = new Template(templateName, templateSubject, templateBody);

        // Add card to Firestore
        db.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID))
                .collection(Constants.KEY_COLLECTION_TEMPLATES)
                .add(newTemplate)
                .addOnSuccessListener(documentReference -> {
                    // Reset fields
                    resetFields();

                    // Hide loading state
                    loading(false);
                    showToast("New template added successful", 0);
                    Intent intent = new Intent(this, TemplatesActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    // Handle failure, e.g., show error message
                    loading(false);
                    showToast("Failed to add new template", 0);
                });
    }
    private void resetFields() {
        // Reset input fields
        templateName.setText("");
        templateSubject.setText("");
        templateBody.setText("");
    }

    private void loading(Boolean isLoading) {
        if (isLoading) {
            createButton.setEnabled(false); // Disable createButton
        } else {
            createButton.setEnabled(true); // Enable createButton
        }
    }
    private boolean isFieldsFilled() {
        // Check if all required fields are filled
        return !templateName.getText().toString().trim().isEmpty() &&
                !templateSubject.getText().toString().trim().isEmpty() &&
                !templateBody.getText().toString().trim().isEmpty();
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
            createButton.setEnabled(isFieldsFilled());
        }
    }
    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GENERATE_AI_TEMPLATE_REQUEST_CODE) {
            if (resultCode == RESULT_OK && data != null) {
                String generatedSubject = data.getStringExtra("generatedSubject");
                String generatedBody = data.getStringExtra("generatedBody");
                // Use the generated subject and body here
                templateSubject.setText(generatedSubject);
                templateBody.setText(generatedBody);
            }
        }
    }
    public void genAIButton(View view) {
        Intent intent = new Intent(AddNewTemplateActivity.this, GenerateAITemplateActivity.class);
        startActivityForResult(intent, GENERATE_AI_TEMPLATE_REQUEST_CODE);
    }
}