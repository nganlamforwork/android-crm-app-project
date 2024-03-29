package hcmus.android.crm.activities.Leads;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.Manifest;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.firestore.FirebaseFirestore;

import hcmus.android.crm.R;
import hcmus.android.crm.activities.DrawerBaseActivity;
import hcmus.android.crm.activities.Maps.MapsActivity;
import hcmus.android.crm.databinding.ActivityLeadDetailBinding;
import hcmus.android.crm.models.Lead;
import hcmus.android.crm.utilities.Constants;
import hcmus.android.crm.utilities.Utils;

public class LeadDetailActivity extends DrawerBaseActivity {
    private ActivityLeadDetailBinding binding;
    private static final int REQUEST_PHONE_CALL = 1;

    private Lead lead;
    private String leadId;
    FirebaseFirestore db;

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
            byte[] bytes = Base64.decode(lead.getImage(), Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            binding.avatar.setImageBitmap(bitmap);

            binding.leadName.setText(lead.getName());
            binding.leadPhone.setText(lead.getPhone());
            binding.leadEmail.setText(lead.getEmail());
            binding.leadAddress.setText(lead.getAddress());
            binding.leadNotes.setText(lead.getNotes());
        }

        setListeners();
    }

    private void showToast(String message, int length) {
        Utils.showToast(getApplicationContext(), message, length);
    }

    private void setListeners() {
        binding.leadLocation.setOnClickListener(v -> {
            String latitude = lead.getLatitude();
            String longitude = lead.getLongitude();
            String leadName = lead.getName();
            // Create intent to start MapActivity
            Intent intent = new Intent(this, MapsActivity.class);

            // Add latitude and longitude as extras to the intent
            intent.putExtra("latitude", latitude);
            intent.putExtra("longitude", longitude);
            intent.putExtra("name", leadName);


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

        binding.textDeleteLead.setOnClickListener(v -> {
            showDeleteConfirmationDialog();
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
        db.collection(Constants.KEY_COLLECTION_LEADS).document(leadId).delete();
        startActivity(new Intent(this, LeadActivity.class));
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_edit) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
