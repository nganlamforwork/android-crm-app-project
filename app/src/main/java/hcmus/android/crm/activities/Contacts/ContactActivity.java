package hcmus.android.crm.activities.Contacts;

import static hcmus.android.crm.utilities.Utils.encodeImage;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import hcmus.android.crm.R;
import hcmus.android.crm.activities.Contacts.adapters.ContactAdapter;
import hcmus.android.crm.activities.DrawerBaseActivity;
import hcmus.android.crm.activities.Leads.AddNewLeadActivity;
import hcmus.android.crm.activities.Leads.LeadActivity;
import hcmus.android.crm.activities.Leads.ScanBusinessCardActivity;
import hcmus.android.crm.activities.Search.SearchActivity;
import hcmus.android.crm.databinding.ActivityContactBinding;
import hcmus.android.crm.models.Contact;
import hcmus.android.crm.utilities.Constants;

public class ContactActivity extends DrawerBaseActivity {
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    private ActivityContactBinding binding;
    private RecyclerView recyclerView;
    private FirebaseFirestore db;
    private ContactAdapter contactAdapter;

    private Animation rotateOpen;
    private Animation rotateClose;
    private Animation fromBottom;
    private Animation toBottom;
    private boolean clicked;


    //sync contact
    AlertDialog.Builder syncBuilder;
    private List<Contact> contactList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Contacts");

        binding = ActivityContactBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        db = FirebaseFirestore.getInstance();

        recyclerView = binding.contactRecyclerView;
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        setupContactRecyclerView();

        //sync setup
        syncBuilder = new AlertDialog.Builder(this);
        contactList = new ArrayList<>();

        rotateOpen = AnimationUtils.loadAnimation(this, R.anim.rotate_open_anim);
        rotateClose = AnimationUtils.loadAnimation(this, R.anim.rotate_close_anim);
        fromBottom = AnimationUtils.loadAnimation(this, R.anim.from_bottom_anim);
        toBottom = AnimationUtils.loadAnimation(this, R.anim.to_bottom_anim);


        setListeners();
    }

    private void setupContactRecyclerView() {
        Query query = db.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID))
                .collection(Constants.KEY_COLLECTION_CONTACTS)
                .orderBy("createdAt", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<Contact> options = new FirestoreRecyclerOptions.Builder<Contact>()
                .setQuery(query, Contact.class).build();

        checkIfListEmpty(query);

        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                // Clear the existing contactList
                contactList.clear();
                // Iterate through the documents and add them to contactList
                for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                    Contact contact = documentSnapshot.toObject(Contact.class);
                    contactList.add(contact);
                }
                // Notify the adapter of the data change
                contactAdapter.notifyDataSetChanged();
                // Check if the list is empty
                checkIfListEmpty(query);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("Error", "Error getting documents: ", e);
            }
        });

        contactAdapter = new ContactAdapter(options, this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(contactAdapter);
        recyclerView.addItemDecoration(
                new DividerItemDecoration(
                        recyclerView.getContext(),
                        LinearLayoutManager.VERTICAL
                )
        );
        contactAdapter.startListening();
    }

    private void checkIfListEmpty(Query query) {
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().getDocuments().size() > 0) {
                        recyclerView.setVisibility(View.VISIBLE);
                        binding.emptyView.setVisibility(View.GONE);
                    } else {
                        recyclerView.setVisibility(View.GONE);
                        binding.emptyView.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        navigationView.setCheckedItem(R.id.nav_contact);
        if (contactAdapter != null) {
            contactAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (contactAdapter != null)
            contactAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (contactAdapter != null)
            contactAdapter.stopListening();
    }

    private void setListeners() {
        binding.syncButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                syncBuilder.setTitle("Confirm Sync Contact")
                        .setMessage("Do you want to sync contact?")
                        .setCancelable(true)
                        .setPositiveButton("Sync", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                syncContacts();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .show();
            }
        });
        binding.manualButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ContactActivity.this, AddNewContactActivity.class));
            }
        });
        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onAddButtonClicked();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_search) {
            Intent intent = new Intent(this, SearchActivity.class);
            startActivity(intent);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void syncContacts() {
        // Check the SDK version and whether the permission is already granted or not.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
            //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
        } else {
            // Android version is lesser than 6.0 or the permission is already granted.
            List<Contact> contacts = getContactFromPhone();

            for (Contact column : contacts) {
                db.collection(Constants.KEY_COLLECTION_USERS)
                        .document(preferenceManager.getString(Constants.KEY_USER_ID))
                        .collection(Constants.KEY_COLLECTION_CONTACTS)
                        .add(column)
                        .addOnSuccessListener(documentReference -> {
                            showToast("Contact updated successful", 0);
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            // Handle failure, e.g., show error message
                            showToast("Failed to update contact", 0);
                        });
            }

            setupContactRecyclerView();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted
                syncContacts();
            } else {
                Toast.makeText(this, "Until you grant the permission, we canot display the names", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @SuppressLint("Range")
    private List<Contact> getContactFromPhone() {
        String DISPLAY_NAME = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ?
                ContactsContract.Contacts.DISPLAY_NAME_PRIMARY : ContactsContract.Contacts.DISPLAY_NAME;

        String FILTER = DISPLAY_NAME + " NOT LIKE '%@%'";

        String ORDER = String.format("%1$s COLLATE NOCASE", DISPLAY_NAME);

        String[] PROJECTION = {
                ContactsContract.Contacts._ID,
                DISPLAY_NAME,
                ContactsContract.Contacts.HAS_PHONE_NUMBER
        };

        try {
            ArrayList<Contact> contacts = new ArrayList<>();

            ContentResolver cr = getContentResolver();
            Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, PROJECTION, FILTER, null, ORDER);
            if (cursor != null && cursor.moveToFirst()) {

                do {
                    // get the contact's information
                    @SuppressLint("Range") String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                    @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(DISPLAY_NAME));
                    @SuppressLint("Range") Integer hasPhone = cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

                    // get the user's email address
                    String email = null;
                    Cursor ce = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
                            ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?", new String[]{id}, null);
                    if (ce != null && ce.moveToFirst()) {
                        email = ce.getString(ce.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
                        ce.close();
                    }

                    // get the user's phone number
                    String phone = null;
                    if (hasPhone > 0) {
                        Cursor cp = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{id}, null);
                        if (cp != null && cp.moveToFirst()) {
                            phone = cp.getString(cp.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            cp.close();
                        }
                    }

                    String img = null;
                    if (hasPhone > 0) {
                        Cursor ci = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{id}, null);
                        if (ci != null && ci.moveToFirst()) {
                            img = ci.getString(ci.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI));
                            ci.close();
                        }
                    }

                    String encodedImage;

                    // Check if image URI is not null
                    if (img != null) {
                        try {
                            // Open an input stream from the image URI
                            InputStream inputStream = getContentResolver().openInputStream(Uri.parse(img));

                            // Decode the input stream into a Bitmap
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                            // Encode the bitmap to a base64 string (if needed)
                            encodedImage = encodeImage(bitmap);

                            System.out.println(encodedImage);
                        } catch (FileNotFoundException e) {
                            // Handle FileNotFoundException appropriately
                            e.printStackTrace();
                            continue; // Move to the next iteration
                        }
                    } else {
                        // Handle case where image URI is null
                        encodedImage = ""; // Or handle this according to your requirements
                    }


                    // if the user user has an email or phone then add it to contacts
                    if ((!TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
                            && !email.equalsIgnoreCase(name)) || (!TextUtils.isEmpty(phone))) {
                        Contact contact = new Contact();
                        contact.setName(name);
                        contact.setEmail(email);
                        contact.setPhone(phone);
                        contact.setImage(encodedImage);
                        if (!checkPhoneExist(contact)) {
                            contacts.add(contact);
                        }
                    }

                } while (cursor.moveToNext());

                // clean up cursor
                cursor.close();
            }

            return contacts;
        } catch (Exception ex) {
            return null;
        }
    }

    private boolean checkPhoneExist(Contact newContact) {
        for (int i = 0; i < contactList.size(); i++) {
            if (contactList.get(i).getPhone().equals(newContact.getPhone())) {
                return true;
            }
        }
        return false;
    }

    private void onAddButtonClicked() {
        setVisibility(clicked);
        setAnimation(clicked);
        clicked = !clicked;
    }

    private void setVisibility(boolean clicked) {
        if (!clicked) {
            binding.manualButton.setVisibility(View.VISIBLE);
            binding.syncButton.setVisibility(View.VISIBLE);
        } else {
            binding.manualButton.setVisibility(View.INVISIBLE);
            binding.syncButton.setVisibility(View.INVISIBLE);
        }

    }

    private void setAnimation(boolean clicked) {
        if (!clicked) {
            binding.manualButton.startAnimation(fromBottom);
            binding.syncButton.startAnimation(fromBottom);
            binding.fab.startAnimation(rotateOpen);
        } else {
            binding.manualButton.startAnimation(toBottom);
            binding.syncButton.startAnimation(toBottom);
            binding.fab.startAnimation(rotateClose);

        }
    }
}
