package hcmus.android.crm.activities.Leads;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.Manifest;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.firestore.FirebaseFirestore;

import hcmus.android.crm.R;
import hcmus.android.crm.activities.DrawerBaseActivity;
import hcmus.android.crm.activities.Maps.MapsActivity;
import hcmus.android.crm.activities.Notes.NoteActivity;
import hcmus.android.crm.databinding.ActivityLeadDetailBinding;
import hcmus.android.crm.models.Lead;
import hcmus.android.crm.utilities.Constants;

public class LeadDetailActivity extends DrawerBaseActivity {
    private ActivityLeadDetailBinding binding;
    private static final int REQUEST_PHONE_CALL = 1;

    private Lead lead;
    private String leadId;
    private FirebaseFirestore db;

    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @SuppressLint("NotifyDataSetChanged")
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == RESULT_OK) {
                Intent intent = result.getData();
                assert intent != null;
                Lead updatedLead = intent.getParcelableExtra("updatedLead");
                if (updatedLead != null) {
                    updateUI(updatedLead);
                }
            }
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Lead Details");

        binding = ActivityLeadDetailBinding.inflate(getLayoutInflater());
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

        Intent intent = this.getIntent();
        if (intent != null) {
            lead = intent.getParcelableExtra("leadDetails");
            leadId = intent.getStringExtra("leadId");
            updateUI(lead);
        }

        setListeners();
    }

    private void updateUI(Lead updatedLead) {
        binding.leadName.setText(updatedLead.getName());
        binding.leadPhone.setText(updatedLead.getPhone());
        binding.leadEmail.setText(updatedLead.getEmail());
        binding.leadAddress.setText(updatedLead.getAddress());
        binding.leadNotes.setText(updatedLead.getNotes());

        if (lead.getImage() != null && !lead.getImage().isEmpty()) {
            byte[] bytes = Base64.decode(lead.getImage(), Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            binding.avatar.setImageBitmap(bitmap);
        } else {
            binding.avatar.setImageResource(R.drawable.avatar);
        }
    }

    private void setListeners() {
        binding.leadLocation.setOnClickListener(v -> {
            String latitude = lead.getLatitude();
            String longitude = lead.getLongitude();
            String leadName = lead.getName();
            String leadAddress = lead.getAddress();
            // Create intent to start MapActivity
            Intent intent = new Intent(this, MapsActivity.class);

            // Add latitude and longitude as extras to the intent
            intent.putExtra("latitude", latitude);
            intent.putExtra("longitude", longitude);
            intent.putExtra("name", leadName);
            intent.putExtra("address", leadAddress);

            startActivity(intent);
        });

        binding.leadMakePhoneCall.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, REQUEST_PHONE_CALL);
            } else {
                makePhoneCall();
            }
        });

        binding.leadSendMail.setOnClickListener(v -> {
            // Get the email address
            String emailAddress = binding.leadEmail.getText().toString();

            // Create the email intent
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);

            // Set the data URI with the email address
            emailIntent.setData(Uri.parse("mailto:" + Uri.encode(emailAddress)));

            if (emailIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(emailIntent);
            } else {
                showToast("No email app found", 0);
            }
        });

        binding.leadFav.setOnClickListener(v -> {
            showToast("Coming soon...", 0);
        });

        binding.textDeleteLead.setOnClickListener(v -> {
            showDeleteConfirmationDialog();
        });

        binding.textViewAllNotes.setOnClickListener(v -> {
            Intent intent = new Intent(this, NoteActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            preferenceManager.putString("selectedLead", leadId);
            startActivity(intent);
        });
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Lead");
        builder.setMessage("Are you sure you want to delete this lead?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteLead();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    private void deleteLead() {
        db.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID))
                .collection(Constants.KEY_COLLECTION_LEADS)
                .document(leadId).delete();
        finish();
    }

    private void makePhoneCall() {
        String phoneNumber = binding.leadPhone.getText().toString();
        Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumber));
        startActivity(callIntent);
    }

    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_edit) {
            Intent intent = new Intent(getApplicationContext(), AddNewLeadActivity.class);
            intent.putExtra("leadId", leadId);
            intent.putExtra("lead", lead);
            activityResultLauncher.launch(intent);
        }

        return super.onOptionsItemSelected(item);
    }
}
