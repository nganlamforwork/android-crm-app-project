package hcmus.android.crm.activities.Opportunity;


import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.Spinner;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

import hcmus.android.crm.R;
import hcmus.android.crm.activities.DrawerBaseActivity;
import hcmus.android.crm.databinding.ActivityCreateOpportunityBinding;
import hcmus.android.crm.models.Opportunity;
import hcmus.android.crm.utilities.Constants;

public class CreateOpportunityActivity extends DrawerBaseActivity {
    private ActivityCreateOpportunityBinding binding;
    private FirebaseFirestore db;
    private boolean isEditMode = false;

    private String opportunityId;
    private Opportunity opportunity;

    private ArrayList<String> statusOptions = new ArrayList<>(
            Arrays.asList("In Prospect", "Negotiation", "Closed Won", "Closed Lost")
    );

    private ArrayAdapter<String> statusAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setTitle("Create Opportunity");

        binding = ActivityCreateOpportunityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        db = FirebaseFirestore.getInstance();

        Toolbar toolbar = binding.appBar.toolbar;
        setSupportActionBar(toolbar);

        // Enable the back button in the action bar or toolbar
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        opportunityId = getIntent().getStringExtra("opportunityId");
        opportunity = getIntent().getParcelableExtra("opportunity");
        setupSpinnerStatus();
        setupFilterPossibility();
        if (opportunityId != null) {
            isEditMode = true;
            setTitle("Edit opportunity");
            populateEventData();
        }
        setListeners();

    }

    private void setupFilterPossibility() {
        InputFilter maxInputFilter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                // Concatenate the current input and the new input
                String input = dest.subSequence(0, dstart).toString() + source.subSequence(start, end) + dest.subSequence(dend, dest.length());
                // Parse the input as a double
                double value;
                try {
                    value = Double.parseDouble(input);
                } catch (NumberFormatException e) {
                    // Return null if the input cannot be parsed as a double
                    return null;
                }
                // Return the input as-is if it is less than or equal to 100, else return an empty string to reject the new input
                return (value <= 100) ? null : "";
            }
        };

        binding.opportunityPossibility.setFilters(new InputFilter[]{maxInputFilter});
    }

    private void populateEventData() {
        binding.opportunityName.setText(opportunity.getName());
        binding.opportunityStatus.setSelection(statusAdapter.getPosition(opportunity.getStatus()));
        binding.opportunityPrice.setText(String.valueOf(opportunity.getPrice()));
        binding.opportunityPossibility.setText(String.valueOf(opportunity.getPossibility()));
        binding.expectedDate.setText(opportunity.getExpectedDate());
    }

    private void setupSpinnerStatus() {
        Spinner statusSpinner = binding.opportunityStatus;
        statusAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, statusOptions);

        // Specify the layout to use when the list of choices appears
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        statusSpinner.setAdapter(statusAdapter);
    }

    private void handleUpdateOpportunity() {
        String name = binding.opportunityName.getText().toString().trim();
        String status = binding.opportunityStatus.getSelectedItem().toString().trim();
        Double price = Double.valueOf(binding.opportunityPrice.getText().toString().trim());
        Double possibility = Double.valueOf(binding.opportunityPossibility.getText().toString().trim());
        String expectedDate = binding.expectedDate.getText().toString().trim();

        if (!isFieldsFilled()) {
            loading(false);
            showToast("All field is required", 0);
            return;
        }

        Opportunity updatedOpportunity = new Opportunity(name, status, price, possibility, expectedDate);

        db.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID))
                .collection(Constants.KEY_COLLECTION_OPPORTUNITIES)
                .document(opportunityId)
                .set(updatedOpportunity)
                .addOnSuccessListener(documentReference -> {
                    resetFields();

                    loading(false);
                    showToast("Opportunity updated successful", 0);
                    Intent intent = new Intent(this, OpportunityDetailActivity.class);
                    intent.putExtra("opportunityDetails", updatedOpportunity);
                    intent.putExtra("opportunityId", opportunityId);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                })
                .addOnFailureListener(e -> {
                    loading(false);
                    showToast("Failed to update opportunity", 0);
                });
    }

    private void handleSaveOpportunity() {
        String name = binding.opportunityName.getText().toString().trim();
        String status = binding.opportunityStatus.getSelectedItem().toString().trim();
        Double price = Double.valueOf(binding.opportunityPrice.getText().toString().trim());
        Double possibility = Double.valueOf(binding.opportunityPossibility.getText().toString().trim());
        String expectedDate = binding.expectedDate.getText().toString().trim();

        if (!isFieldsFilled()) {
            loading(false);
            showToast("All field is required", 0);
            return;
        }

        Opportunity newOpportunity = new Opportunity(name, status, price, possibility, expectedDate);

        db.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID))
                .collection(Constants.KEY_COLLECTION_OPPORTUNITIES)
                .add(newOpportunity)
                .addOnSuccessListener(documentReference -> {
                    resetFields();

                    loading(false);
                    showToast("New opportunity added successful", 0);
                    String opportunityId = documentReference.getId();

                    Intent intent = new Intent(this, OpportunityDetailActivity.class);
                    intent.putExtra("opportunityDetails", newOpportunity);
                    intent.putExtra("opportunityId", opportunityId);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    loading(false);
                    showToast("Failed to add new opportunity", 0);
                });
    }

    private boolean isFieldsFilled() {
        // Check if all required fields are filled
        return !binding.opportunityName.getText().toString().trim().isEmpty() &&
                !binding.opportunityStatus.getSelectedItem().toString().trim().isEmpty() &&
                !binding.opportunityPrice.getText().toString().trim().isEmpty() &&
                !binding.opportunityPossibility.getText().toString().trim().isEmpty() &&
                !binding.expectedDate.getText().toString().trim().isEmpty();
    }

    private void resetFields() {
        binding.opportunityName.setText("");
        binding.opportunityPossibility.setText("");
        binding.opportunityPrice.setText("");
        binding.expectedDate.setText("");
        binding.opportunityStatus.setSelection(0);
    }

    private void setListeners() {
        binding.expectedDate.setOnClickListener(v -> {
            showDatePicker();
        });

        if (isEditMode) {
            // Edit mode: Set button text to "Update Event"
            binding.buttonSaveOpportunity.setText("Update");
            binding.buttonSaveOpportunity.setOnClickListener(v -> {
                loading(true);
                handleUpdateOpportunity();
            });
        } else {
            // Add mode: Set button text to "Save Event"
            binding.buttonSaveOpportunity.setText("Save");
            binding.buttonSaveOpportunity.setOnClickListener(v -> {
                loading(true);
                handleSaveOpportunity();
            });
        }
    }

    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.buttonSaveOpportunity.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.buttonSaveOpportunity.setVisibility(View.VISIBLE);
        }
    }

    public void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int day) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(year, month, day);
                        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                        String selectedDate = dateFormat.format(calendar.getTime());

                        binding.expectedDate.setText(selectedDate);
                    }
                }, year, month, dayOfMonth);

        datePickerDialog.show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }
}