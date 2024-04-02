package hcmus.android.crm.activities.Contacts;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

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

    private ActivityContactBinding binding;
    private RecyclerView recyclerView;
    private Query query;
    private ListenerRegistration listenerRegistration;
    private FirebaseFirestore db;
    private ContactAdapter contactAdapter;
    private List<Contact> contactList;
    private ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @SuppressLint("NotifyDataSetChanged")
        @Override
        public void onActivityResult(ActivityResult result) {
            if(result.getResultCode() == RESULT_OK) {
                Intent intent  = result.getData();
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
}
