package hcmus.android.crm.activities.Contacts;

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
import hcmus.android.crm.databinding.ActivityContactDetailBinding;
import hcmus.android.crm.models.Contact;
import hcmus.android.crm.utilities.Constants;
import hcmus.android.crm.utilities.Utils;

public class ContactDetailActivity extends DrawerBaseActivity {
    private ActivityContactDetailBinding binding;
    private static final int REQUEST_PHONE_CALL = 1;

    private Contact contact;
    private String contactId;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Contact Details");

        binding = ActivityContactDetailBinding.inflate(getLayoutInflater());
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
            contact = intent.getParcelableExtra("contactDetails");
            contactId = intent.getStringExtra("contactId");
            byte[] bytes = Base64.decode(contact.getImage(), Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            binding.avatar.setImageBitmap(bitmap);

            binding.contactName.setText(contact.getName());
            binding.contactPhone.setText(contact.getPhone());
            binding.contactEmail.setText(contact.getEmail());
            binding.contactNotes.setText(contact.getNotes());
        }

        setListeners();
    }

    private void setListeners() {
        binding.contactMakePhoneCall.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, REQUEST_PHONE_CALL);
            } else {
                makePhoneCall();
            }
        });

        binding.contactSendMail.setOnClickListener(v -> {
            // Get the email address
            String emailAddress = binding.contactEmail.getText().toString();

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

        binding.textDeleteContact.setOnClickListener(v -> {
            showDeleteConfirmationDialog();
        });
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Contact");
        builder.setMessage("Are you sure you want to delete this contact?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteContact();
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

    private void deleteContact() {
        db.collection(Constants.KEY_COLLECTION_CONTACTS).document(contactId).delete();
        startActivity(new Intent(this, ContactActivity.class));
        finish();
    }

    private void makePhoneCall() {
        String phoneNumber = binding.contactPhone.getText().toString();
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
