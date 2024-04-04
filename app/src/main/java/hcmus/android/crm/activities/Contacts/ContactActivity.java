package hcmus.android.crm.activities.Contacts;

import static hcmus.android.crm.utilities.Utils.encodeImage;

import android.Manifest;
import android.annotation.SuppressLint;
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
import android.provider.ContactsContract;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import hcmus.android.crm.R;
import hcmus.android.crm.activities.DrawerBaseActivity;
import hcmus.android.crm.activities.Contacts.adapters.ContactAdapter;
import hcmus.android.crm.databinding.ActivityContactBinding;
import hcmus.android.crm.models.Contact;
import hcmus.android.crm.utilities.Constants;
import hcmus.android.crm.utilities.TouchHelper;

public class ContactActivity extends DrawerBaseActivity implements ContactAdapter.OnContactItemClickListener {

    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    private ActivityContactBinding binding;
    private RecyclerView recyclerView;
    private Query query;
    private ListenerRegistration listenerRegistration;
    private FirebaseFirestore db;
    private ContactAdapter contactAdapter;
    private List<Contact> contactList;

    //sync contact
    private Button syncButton;
    AlertDialog.Builder syncBuilder;

    private ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @SuppressLint("NotifyDataSetChanged")
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == RESULT_OK) {
                Intent intent = result.getData();
                assert intent != null;
                Contact newContact = intent.getParcelableExtra("newContact");
                contactList.add(0, newContact);
                contactAdapter.notifyItemInserted(0);
                contactAdapter.notifyDataSetChanged();
            }
        }
    });

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

        contactList = new ArrayList<>();
        contactAdapter = new ContactAdapter(this, contactList);

//        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new TouchHelper(contactAdapter));
//        itemTouchHelper.attachToRecyclerView(recyclerView);
        recyclerView.setAdapter(contactAdapter);

        contactAdapter.setOnContactItemClickListener(this);

        //sync setup
        syncBuilder = new AlertDialog.Builder(this);
        syncButton = binding.syncContact;

        showData();
        setListeners();


    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onResume() {
        super.onResume();
        navigationView.setCheckedItem(R.id.nav_contact);
    }


    private void setListeners() {
        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activityResultLauncher.launch(new Intent(ContactActivity.this, AddNewContactActivity.class));
            }
        });
        syncButton.setOnClickListener(new View.OnClickListener() {
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
    }

    @Override
    public void onContactItemClick(int position) {
        // Handle the item click event
        Contact contact = contactList.get(position);
        Intent intent = new Intent(ContactActivity.this, ContactDetailActivity.class);
        intent.putExtra("contactDetails", contact);
        intent.putExtra("contactId", contact.ContactId);
        startActivity(intent);
    }

    private void showData() {
        contactList.clear();
        query = db.collection(Constants.KEY_COLLECTION_CONTACTS).orderBy("createdAt", Query.Direction.DESCENDING);
        listenerRegistration = query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                assert value != null;
                for (DocumentChange documentChange : value.getDocumentChanges()) {
                    String id = documentChange.getDocument().getId();
                    Contact contact = documentChange.getDocument().toObject(Contact.class).withId(id);

                    contactList.add(contact);
                }
                contactAdapter.notifyDataSetChanged();
                listenerRegistration.remove();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search, menu);

        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setQueryHint("Type here to search");
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String newText) {
                String query = newText.toLowerCase(Locale.ROOT);
                contactAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                String query = newText.toLowerCase(Locale.ROOT);
                contactAdapter.getFilter().filter(query);
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_search) {
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
            List<Contact> contacts = getContactNames();
            for (Contact column : contacts) {
                db.collection(Constants.KEY_COLLECTION_CONTACTS)
                        .add(column);
            }

            showData();
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

    private List<Contact> getContactNames() {
        String[] projection = {
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.PHOTO_URI
        };
        List<Contact> contacts = new ArrayList<>();

        // Get the ContentResolver
        ContentResolver cr = getContentResolver();

        // Get the Cursor of all the contacts
        Cursor cursor = cr.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection,
                null,
                null,
                null
        );

        // Move the cursor to the first item
        if (cursor != null && cursor.moveToFirst()) {
            // Iterate through the cursor
            do {
                // Get the contact's name, phone, and image URI
                @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(projection[0]));
                @SuppressLint("Range") String phone = cursor.getString(cursor.getColumnIndex(projection[1]));
                @SuppressLint("Range") String img = cursor.getString(cursor.getColumnIndex(projection[2]));

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

                // Update the ImageView with the selected image
                Contact newContact = new Contact();
                newContact.setName(name);
                newContact.setPhone(phone);
                newContact.setImage(encodedImage);

                if (!checkPhoneExist(newContact)) {
                    // Add the name to the list of contacts
                    contacts.add(newContact);
                }
            } while (cursor.moveToNext());
            // Close the cursor
            cursor.close();
        }

        return contacts;
    }

    private boolean checkPhoneExist(Contact newContact) {
        for (int i = 0; i < contactList.size(); i++) {
            if (contactList.get(i).getPhone().equals(newContact.getPhone())) {
                return true;
            }
        }
        return false;
    }

}

