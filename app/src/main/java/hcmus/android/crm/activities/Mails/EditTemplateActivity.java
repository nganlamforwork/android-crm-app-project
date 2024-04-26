package hcmus.android.crm.activities.Mails;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;
import hcmus.android.crm.R;
import hcmus.android.crm.activities.BusinessCard.EditBusinessCardActivity;
import hcmus.android.crm.activities.DrawerBaseActivity;
import hcmus.android.crm.databinding.ActivityEditBusinessCardBinding;
import hcmus.android.crm.databinding.ActivityEditTemplateBinding;
import hcmus.android.crm.models.BusinessCard;
import hcmus.android.crm.models.Template;
import hcmus.android.crm.utilities.Constants;

public class EditTemplateActivity extends DrawerBaseActivity {
    private ActivityEditTemplateBinding binding;
    private EditText templateName, templateSubject, templateBody;
    private MenuItem saveMenuItem;

    private FirebaseFirestore db;
    private String templateId;
    private Template template;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle("Edit Template");

        binding = ActivityEditTemplateBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = binding.appBar.toolbar;
        setSupportActionBar(toolbar);

        getElementsById();

        db = FirebaseFirestore.getInstance();
        // Enable the back button in the action bar or toolbar
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setTitle("Edit Template");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        Intent intent = this.getIntent();
        if (intent != null) {
            templateId = intent.getStringExtra("templateId");
            template = intent.getParcelableExtra("template");

            displayDataOnUI(template);
        }

        setListeners();
    }
    private void setListeners() {
        // Add text change listeners to required fields
        templateName.addTextChangedListener(new EditTemplateActivity.FieldTextWatcher());
        templateBody.addTextChangedListener(new EditTemplateActivity.FieldTextWatcher());
        templateSubject.addTextChangedListener(new EditTemplateActivity.FieldTextWatcher());
    }
    private void getElementsById() {
        // Basic fields
        templateName = binding.templateName;
        templateBody = binding.templateBody;
        templateSubject = binding.templateSubject;
    }
    private void displayDataOnUI(Template template) {
        if (template != null) {
            templateName.setText(template.getName());
            templateBody.setText(template.getBody());
            templateSubject.setText(template.getSubject());
        }
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
                updateTemplate();
                return true;
            } else {
                showToast("Please fill in all required fields", 0);
                return false;
            }
        }

        return super.onOptionsItemSelected(item);
    }
    private void updateTemplate() {
        String templateName = this.templateName.getText().toString().trim();
        String templateSubject = this.templateSubject.getText().toString().trim();
        String templateBody = this.templateBody.getText().toString().trim();

        template.setName(templateName);
        template.setSubject(templateSubject);
        template.setBody(templateBody);

        db.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID))
                .collection(Constants.KEY_COLLECTION_TEMPLATES)
                .document(templateId)
                .set(template)
                .addOnSuccessListener(documentReference -> {
                    showToast("Template updated successfully!", 0);
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("updatedTemplate", template);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    showToast("Failed to update template!",0);
                });
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
            saveMenuItem.setEnabled(isFieldsFilled());
        }
    }

}