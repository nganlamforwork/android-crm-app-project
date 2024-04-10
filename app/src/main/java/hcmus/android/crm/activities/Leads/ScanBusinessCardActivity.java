package hcmus.android.crm.activities.Leads;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanIntentResult;
import com.journeyapps.barcodescanner.ScanOptions;

import hcmus.android.crm.activities.DrawerBaseActivity;
import hcmus.android.crm.databinding.ActivityScanBusinessCardBinding;
import hcmus.android.crm.models.BusinessCard;
import hcmus.android.crm.models.Lead;
import hcmus.android.crm.utilities.Constants;

public class ScanBusinessCardActivity extends DrawerBaseActivity {
    private ActivityScanBusinessCardBinding binding;
    private FirebaseFirestore db;
    private BusinessCard scannedBusinessCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Scan Business Card");

        binding = ActivityScanBusinessCardBinding.inflate(getLayoutInflater());
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

        setListeners();
    }

    private void setListeners() {
        binding.addNewLeadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleAddNewLead();
            }
        });
        binding.textRescanningButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkPermissionCamera(ScanBusinessCardActivity.this);
            }
        });
        binding.startScanningButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkPermissionCamera(ScanBusinessCardActivity.this);
            }
        });
    }

    private ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    showCamera();
                } else {
                    // Explain
                }
            }
    );
    private ActivityResultLauncher<ScanOptions> scanLauncher = registerForActivityResult(new ScanContract(), new ActivityResultCallback<ScanIntentResult>() {
        @Override
        public void onActivityResult(ScanIntentResult result) {
            if (result.getContents() == null) {
                Toast.makeText(ScanBusinessCardActivity.this, "Cancelled", Toast.LENGTH_SHORT).show();
            } else {
                // JSON to Object
                Gson gson = new Gson();
                scannedBusinessCard = gson.fromJson(result.getContents(), BusinessCard.class);
                showBusinessCardData();
            }
        }
    });

    private void checkPermissionCamera(Context context) {
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            showCamera();
        } else if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
            Toast.makeText(context, "Camera permission required", Toast.LENGTH_SHORT).show();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void showCamera() {
        ScanOptions options = new ScanOptions();
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
        options.setPrompt("Scan QR Code");
        options.setCameraId(0);
        options.setBeepEnabled(false);
        options.setBarcodeImageEnabled(true);
        options.setOrientationLocked(false);
        scanLauncher.launch(options);
    }
    private void showBusinessCardData(){
        binding.textViewFullname.setText(scannedBusinessCard.getFullname());
        binding.textViewCompany.setText(scannedBusinessCard.getCompany());
        binding.textViewJobTitle.setText(scannedBusinessCard.getJobTitle());
        binding.textViewEmail.setText(scannedBusinessCard.getEmail());
        binding.textViewPhone.setText(scannedBusinessCard.getPhone());
        binding.cardDetailView.setVisibility(View.VISIBLE);

        binding.description.setText("If you are not satisfied about this information or you want to scan another card, please click the \"Rescanning\" button below.");
        binding.startScanningButton.setVisibility(View.GONE);
        binding.addNewLeadButton.setVisibility(View.VISIBLE);
        binding.textRescanningButton.setVisibility(View.VISIBLE);
    }

    private void handleAddNewLead() {
        // Get input values
        String name = scannedBusinessCard.getFullname();
        String email = scannedBusinessCard.getEmail();
        String phone = scannedBusinessCard.getPhone();
        String job = scannedBusinessCard.getJobTitle();
        String company = scannedBusinessCard.getCompany();
        String notes = scannedBusinessCard.getNote();

        Lead newLead = new Lead(name, email, phone, "", job, company, notes, "", "", "");
        db.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID))
                .collection(Constants.KEY_COLLECTION_LEADS)
                .add(newLead)
                .addOnSuccessListener(documentReference -> {
                    showToast("New lead added successful", 0);
                    Intent intent = new Intent(this, LeadActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    showToast("Failed to add new lead", 0);
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }
}