package hcmus.android.crm.activities.Leads;

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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.makeramen.roundedimageview.RoundedImageView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import hcmus.android.crm.R;
import hcmus.android.crm.activities.DrawerBaseActivity;
import hcmus.android.crm.activities.Tags.StringWithTag;
import hcmus.android.crm.databinding.ActivityAddNewLeadBinding;
import hcmus.android.crm.models.BusinessCard;
import hcmus.android.crm.models.Lead;
import hcmus.android.crm.models.Tag;
import hcmus.android.crm.utilities.Constants;
import hcmus.android.crm.utilities.Utils;


public class AddNewLeadActivity extends DrawerBaseActivity {
    private static final int REQUEST_LOCATION = 1;
    private ActivityAddNewLeadBinding binding;
    private Lead newLead;
    private EditText leadName, leadEmail, leadPhone, leadJob, leadCompany, leadNotes;
    private String tagId;
    private AutoCompleteTextView tagsDropdown;
    private TextView leadLocation;
    private Button newLeadSaveButton;
    private ProgressBar progressBar;
    private RoundedImageView leadImage;
    private FrameLayout layoutImage;
    private String encodedImage;
    private SwitchMaterial getLeadLocation;
    private LocationManager locationManager;
    private LocationRequest locationRequest;
    private String leadLat, leadLong, address;
    private boolean isEditMode = false;
    private String leadId;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Add new lead");

        binding = ActivityAddNewLeadBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(2000)
                .build();

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
        leadName = binding.leadName;
        leadEmail = binding.leadEmail;
        leadPhone = binding.leadPhone;
        leadJob = binding.leadJob;
        leadCompany = binding.leadCompany;
        leadNotes = binding.leadNotes;
        getLeadLocation = binding.getLeadLocation;
        leadImage = binding.leadImage;
        layoutImage = binding.layoutImage;
        leadLocation = binding.leadlocation;
        tagsDropdown = binding.tagsDropdown;

        progressBar = binding.progressBar;
        newLeadSaveButton = binding.buttonSaveLead;


        leadId = getIntent().getStringExtra("leadId");
        newLead = getIntent().getParcelableExtra("lead");

        if (leadId != null) {
            isEditMode = true;
            setTitle("Edit lead");
            populateEventData();
        }

        fetchTags();
        setListeners();
    }

    private void populateEventData() {
        leadName.setText(newLead.getName());
        leadEmail.setText(newLead.getEmail());
        leadPhone.setText(newLead.getPhone());
        leadLocation.setText(newLead.getAddress());
        leadCompany.setText(newLead.getCompany());
        leadJob.setText(newLead.getJob());
        leadNotes.setText(newLead.getNotes());

        // Decode base64 encoded image string and set it to ImageView
        String imageString = newLead.getImage();
        if (imageString != null && !imageString.isEmpty()) {
            byte[] bytes = Base64.decode(imageString, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            encodedImage = encodeImage(bitmap);
            leadImage.setImageBitmap(bitmap);
        }
    }

    private void setListeners() {
        newLeadSaveButton.setEnabled(false); // Initially disable the button

        // Add text change listeners to required fields
        leadName.addTextChangedListener(new FieldTextWatcher());
        leadEmail.addTextChangedListener(new FieldTextWatcher());
        leadPhone.addTextChangedListener(new FieldTextWatcher());
        leadLocation.addTextChangedListener(new FieldTextWatcher());

        layoutImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });

        tagsDropdown.setOnItemClickListener((parent, view, position, id) -> {
            StringWithTag selectedTag = (StringWithTag) parent.getItemAtPosition(position);
            String selectedTagTitle = selectedTag.getString();
            String selectedTagId = selectedTag.getTagId();
            tagId = selectedTagId;
        });
        if (isEditMode) {
            newLeadSaveButton.setText("Update Lead");
            newLeadSaveButton.setOnClickListener(v -> {
                loading(true);
                handleUpdateLead();
            });
        } else {
            newLeadSaveButton.setText("Save Lead");
            newLeadSaveButton.setOnClickListener(v -> {
                loading(true);
                handleAddNewLead();
            });
        }


        getLeadLocation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isChecked()) {
                    // check location permission
                    CheckedLocationPermission();
                }
            }
        });
    }

    private void CheckedLocationPermission() {
        locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            onGPS();
            getLeadLocation.setChecked(false);
        } else {
            getCurrentLocation();
        }
    }

    // Fetch list of tags and display them in the dropdown
    private void fetchTags() {
        db.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID))
                .collection(Constants.KEY_COLLECTION_TAGS)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Tag> tags = new ArrayList<>();
                        for (DocumentSnapshot document : task.getResult()) {
                            Tag tag = document.toObject(Tag.class);
                            tag.setId(document.getId());
                            tags.add(tag);
                        }
                        // Create a list to hold both tag titles and IDs
                        List<StringWithTag> tagList = new ArrayList<>();
                        if (!tags.isEmpty()) {
                            for (Tag tag : tags) {
                                tagList.add(new StringWithTag(tag.getTitle(), tag.getId()));
                            }

                            tagList.add(new StringWithTag("None", null));
                            // Create an ArrayAdapter with custom layout for dropdown items
                            ArrayAdapter<StringWithTag> adapter = new ArrayAdapter<StringWithTag>(AddNewLeadActivity.this, R.layout.dropdown_item_tag, tagList);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            tagsDropdown.setAdapter(adapter);

                            // Enable dropdown
                            tagsDropdown.setEnabled(true);

                            if (isEditMode && newLead != null) {
                                setDefaultTagForDropdown(tagsDropdown, newLead.getTagId());
                            }

                        } else {
                            tagList.add(new StringWithTag("None", null));
                        }
                    } else {
                        showToast("Failed to fetch tags: " + task.getException().getMessage(), Toast.LENGTH_SHORT);
                    }
                });
    }

    private void setDefaultTagForDropdown(AutoCompleteTextView dropdown, String defaultTagId) {
        for (int i = 0; i < dropdown.getAdapter().getCount(); i++) {
            StringWithTag item = (StringWithTag) dropdown.getAdapter().getItem(i);
            if (item.getTagId() != null && item.getTagId().equals(defaultTagId)) {
                dropdown.setText(item.getString(), false);
                return;
            }
        }
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                AddNewLeadActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(AddNewLeadActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        } else {
            LocationServices.getFusedLocationProviderClient(AddNewLeadActivity.this).requestLocationUpdates(locationRequest, new LocationCallback() {
                @Override
                public void onLocationResult(@NonNull LocationResult locationResult) {
                    super.onLocationResult(locationResult);

                    LocationServices.getFusedLocationProviderClient(AddNewLeadActivity.this).removeLocationUpdates(this);

                    if (locationResult != null && locationResult.getLocations().size() > 0) {
                        int index = locationResult.getLocations().size() - 1;
                        double latitude = locationResult.getLocations().get(index).getLatitude();
                        double longitude = locationResult.getLocations().get(index).getLongitude();

                        leadLat = String.valueOf(latitude);
                        leadLong = String.valueOf(longitude);

                        getAddressFromLocation(AddNewLeadActivity.this, latitude, longitude);
                    }
                }
            }, Looper.getMainLooper());
        }

    }

    public void getAddressFromLocation(Context context, double LATITUDE, double LONGITUDE) {
        try {
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());

            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null && addresses.size() > 0) {
                // get country name, postal code, state, city name
                address = addresses.get(0).getAddressLine(0);
            }
            leadLocation.setText(address);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getLocationFromAddress(String strAddress) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addressList = geocoder.getFromLocationName(strAddress, 1);
            if (addressList != null && addressList.size() > 0) {
                Address address = addressList.get(0);
                double latitude = address.getLatitude();
                double longitude = address.getLongitude();

                leadLat = String.valueOf(latitude);
                leadLong = String.valueOf(longitude);
            } else {
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void onGPS() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Enable GPS").setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        final AlertDialog dialog = builder.create();
        dialog.show();
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
                !leadLocation.getText().toString().trim().isEmpty();
    }

    private void handleUpdateLead() {
        // Get input values
        String name = leadName.getText().toString().trim();
        String email = leadEmail.getText().toString().trim();
        String phone = leadPhone.getText().toString().trim();
        String job = leadJob.getText().toString().trim();
        String company = leadCompany.getText().toString().trim();
        String notes = leadNotes.getText().toString().trim();

        if (!leadLocation.getText().toString().trim().isEmpty()) {
            address = leadLocation.getText().toString().trim();
            getLocationFromAddress(address);
        }

        if (!isFieldsFilled()) {
            loading(false);
            showToast("All field is required", 0);
            return;
        }

        Lead updatedLead = new Lead(name, email, phone, address, job, company, notes, encodedImage, leadLat, leadLong, tagId);

        db.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID))
                .collection(Constants.KEY_COLLECTION_LEADS)
                .document(leadId)
                .set(updatedLead)
                .addOnSuccessListener(documentReference -> {
                    // Reset fields
                    resetFields();

                    // Hide loading state
                    loading(false);
                    showToast("Lead updated successful", 0);

                    // Send back the new lead data to LeadDetailActivity
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("updatedLead", updatedLead);
                    setResult(Activity.RESULT_OK, resultIntent);

                    finish();
                })
                .addOnFailureListener(e -> {
                    // Handle failure, e.g., show error message
                    loading(false);
                    showToast("Failed to update lead", 0);
                });
    }

    private void handleAddNewLead() {
        // Get input values
        String name = leadName.getText().toString().trim();
        String email = leadEmail.getText().toString().trim();
        String phone = leadPhone.getText().toString().trim();
        String job = leadJob.getText().toString().trim();
        String company = leadCompany.getText().toString().trim();
        String notes = leadNotes.getText().toString().trim();

        if (!leadLocation.getText().toString().trim().isEmpty()) {
            address = leadLocation.getText().toString().trim();
            getLocationFromAddress(address);
        }

        if (!isFieldsFilled()) {
            loading(false);
            showToast("All field is required", 0);
            return;
        }

        newLead = new Lead(name, email, phone, address, job, company, notes, encodedImage, leadLat, leadLong, tagId);
        db.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID))
                .collection(Constants.KEY_COLLECTION_LEADS)
                .add(newLead)
                .addOnSuccessListener(documentReference -> {
                    // Reset fields
                    resetFields();

                    // Hide loading state
                    loading(false);
                    showToast("New lead added successful", 0);
                    Intent intent = new Intent(this, LeadActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
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
        leadJob.setText("");
        leadCompany.setText("");
        leadNotes.setText("");
        leadLocation.setText("");
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

    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }

}
