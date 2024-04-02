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
import java.io.InputStream;
import java.util.List;
import java.util.Locale;

import hcmus.android.crm.activities.DrawerBaseActivity;
import hcmus.android.crm.databinding.ActivityAddNewContactBinding;
import hcmus.android.crm.models.Contact;
import hcmus.android.crm.utilities.Constants;
import hcmus.android.crm.utilities.Utils;


public class AddNewContactActivity extends DrawerBaseActivity {
    private static final int REQUEST_LOCATION = 1;
    private ActivityAddNewContactBinding binding;
    private Contact newContact;
    private EditText contactName, contactEmail, contactPhone, contactJob, contactCompany, contactNotes;
//    private TextView contactLocation;
    private Button newContactSaveButton;
    private ProgressBar progressBar;
    private RoundedImageView contactImage;
    private FrameLayout layoutImage;
    private String encodedImage;
    private SwitchMaterial getContactLocation;
    private LocationManager locationManager;
    private LocationRequest locationRequest;
    private String contactLat, contactLong, address;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Add new contact");

        binding = ActivityAddNewContactBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Get element id
        contactName = binding.contactName;
        contactEmail = binding.contactEmail;
        contactPhone = binding.contactPhone;
        contactJob = binding.contactJob;
        contactCompany = binding.contactCompany;
        contactNotes = binding.contactNotes;
        getContactLocation = binding.getContactLocation;
        contactImage = binding.contactImage;
        layoutImage = binding.layoutImage;
//        contactLocation = binding.contactlocation;

        progressBar = binding.progressBar;
        newContactSaveButton = binding.buttonSaveContact;

        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(2000)
                .build();


        // Handling logic here
        db = FirebaseFirestore.getInstance();

        Toolbar toolbar = binding.toolbar;
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
        newContactSaveButton.setEnabled(false); // Initially disable the button

        // Add text change listeners to required fields
        contactName.addTextChangedListener(new FieldTextWatcher());
        contactEmail.addTextChangedListener(new FieldTextWatcher());
        contactPhone.addTextChangedListener(new FieldTextWatcher());
//        contactLocation.addTextChangedListener(new FieldTextWatcher());

        layoutImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });

        newContactSaveButton.setOnClickListener(v -> {
            if (isFieldsFilled()) {
                // Show loading state
                loading(true);

                // Get input values
                String name = contactName.getText().toString().trim();
                String email = contactEmail.getText().toString().trim();
                String phone = contactPhone.getText().toString().trim();
                String job = contactJob.getText().toString().trim();
                String company = contactCompany.getText().toString().trim();
                String notes = contactNotes.getText().toString().trim();

                // Add contact to Firestore
                addContactToFirestore(name, email, phone, address, job, company, notes, encodedImage);
            }
        });

        getContactLocation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
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
        } else {
            getCurrentLocation();
        }
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                AddNewContactActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(AddNewContactActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        } else {
            LocationServices.getFusedLocationProviderClient(AddNewContactActivity.this).requestLocationUpdates(locationRequest, new LocationCallback() {
                @Override
                public void onLocationResult(@NonNull LocationResult locationResult) {
                    super.onLocationResult(locationResult);

                    LocationServices.getFusedLocationProviderClient(AddNewContactActivity.this).removeLocationUpdates(this);

                    if (locationResult != null && locationResult.getLocations().size() > 0) {
                        int index = locationResult.getLocations().size() - 1;
                        double latitude = locationResult.getLocations().get(index).getLatitude();
                        double longitude = locationResult.getLocations().get(index).getLongitude();

                        contactLat = String.valueOf(latitude);
                        contactLong = String.valueOf(longitude);

                        getAddressFromLocation(AddNewContactActivity.this, latitude, longitude);
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
//            contactLocation.setText(address);
            // moving text in text view
//            contactLocation.setSelected(true);
        } catch (Exception e) {
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
                !contactPhone.getText().toString().trim().isEmpty() &&
//                !contactLocation.getText().toString().trim().isEmpty() &&
                encodedImage != null && !encodedImage.isEmpty();
    }

    private void addContactToFirestore(String name, String email, String phone, String address, String job, String company, String notes, String image) {
        // Add contact to Firestore
        newContact = new Contact(name, email, phone, address, job, company, notes, image, contactLat, contactLong);
        db.collection(Constants.KEY_COLLECTION_CONTACTS)
                .add(newContact)
                .addOnSuccessListener(documentReference -> {
                    // Reset fields
                    resetFields();

                    // Hide loading state
                    loading(false);
                    showToast("New contact added successful", 0);

                    // Send back the new contact data to ContactActivity
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("newContact", newContact);
                    setResult(Activity.RESULT_OK, resultIntent);

                    finish();
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
        contactJob.setText("");
        contactCompany.setText("");
        contactNotes.setText("");
//        contactLocation.setText("");
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
