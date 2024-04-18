package hcmus.android.crm.activities.Tags;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.google.firebase.firestore.FirebaseFirestore;

import hcmus.android.crm.activities.DrawerBaseActivity;
import hcmus.android.crm.databinding.ActivityAddNewTagBinding;
import hcmus.android.crm.models.Tag;
import hcmus.android.crm.utilities.Constants;

public class AddNewTagActivity extends DrawerBaseActivity {

    private ActivityAddNewTagBinding binding;
    private Tag newTag;

    private EditText tagTitle;
    private Button newTagSaveButton;
    private ProgressBar progressBar;
    private FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Add new tag");

        binding = ActivityAddNewTagBinding.inflate(getLayoutInflater());
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
        tagTitle = binding.tagTitle;
        progressBar = binding.progressBar;
        newTagSaveButton = binding.buttonSaveTag;

        newTag = getIntent().getParcelableExtra("tag");


        setListeners();
    }


    private void setListeners() {
        newTagSaveButton.setEnabled(false); // Initially disable the button

        // Add text change listeners to required fields
        tagTitle.addTextChangedListener(new AddNewTagActivity.FieldTextWatcher());

        newTagSaveButton.setOnClickListener(v -> {
            loading(true);
            handleAddNewTag();
        });
    }
    private void loading(Boolean isLoading) {
        if (isLoading) {
            newTagSaveButton.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.INVISIBLE);
            newTagSaveButton.setVisibility(View.VISIBLE);
        }
    }

    private boolean isFieldsFilled() {
        // Check if all required fields are filled
        return !tagTitle.getText().toString().trim().isEmpty();
    }

    private void handleAddNewTag() {
        // Get input values
        String title = tagTitle.getText().toString().trim();
        if (!isFieldsFilled()) {
            loading(false);
            showToast("Title field is required", 0);
            return;
        }

        newTag = new Tag(title);
        db.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID))
                .collection(Constants.KEY_COLLECTION_TAGS)
                .add(newTag)
                .addOnSuccessListener(documentReference -> {
                    // Reset field
                    tagTitle.setText("");

                    // Hide loading state
                    loading(false);
                    showToast("New tag added successful", 0);
                    Intent intent = new Intent(this, TagsActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                })
                .addOnFailureListener(e -> {
                    // Handle failure, e.g., show error message
                    loading(false);
                    showToast("Failed to add new tag", 0);
                });
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
            newTagSaveButton.setEnabled(isFieldsFilled());
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }
}