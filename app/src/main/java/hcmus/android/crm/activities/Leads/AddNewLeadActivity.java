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

import hcmus.android.crm.activities.DrawerBaseActivity;
import hcmus.android.crm.databinding.ActivityAddNewLeadBinding;
import hcmus.android.crm.models.Lead;
import hcmus.android.crm.utilities.Constants;
import hcmus.android.crm.utilities.Utils;


public class AddNewLeadActivity extends DrawerBaseActivity {
    private static final int REQUEST_LOCATION = 1;
    private ActivityAddNewLeadBinding binding;
    private Lead newLead;
    private EditText leadName, leadEmail, leadPhone, leadJob, leadCompany, leadNotes;
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
        leadJob = binding.leadJob;
        leadCompany = binding.leadCompany;
        leadNotes = binding.leadNotes;
        getLeadLocation = binding.getLeadLocation;
        leadImage = binding.leadImage;
        layoutImage = binding.layoutImage;
        leadLocation = binding.leadlocation;

        progressBar = binding.progressBar;
        newLeadSaveButton = binding.buttonSaveLead;

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

        setListeners();
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

        newLeadSaveButton.setOnClickListener(v -> {
            if (isFieldsFilled()) {
                // Show loading state
                loading(true);

                // Get input values
                String name = leadName.getText().toString().trim();
                String email = leadEmail.getText().toString().trim();
                String phone = leadPhone.getText().toString().trim();
                String job = leadJob.getText().toString().trim();
                String company = leadCompany.getText().toString().trim();
                String notes = leadNotes.getText().toString().trim();

                if(!leadLocation.getText().toString().trim().isEmpty()) {
                    address = leadLocation.getText().toString().trim();
                    getLocationFromAddress(address);
                }

                // Add lead to Firestore
                addLeadToFirestore(name, email, phone, address, job, company, notes, encodedImage);
            }
        });

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
                !leadLocation.getText().toString().trim().isEmpty() &&
                encodedImage != null && !encodedImage.isEmpty();
    }


    private void addLeadToFirestore(String name, String email, String phone, String address, String job, String company, String notes, String image) {
        // Add lead to Firestore
        newLead = new Lead(name, email, phone, address, job, company, notes, image, leadLat, leadLong);
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
