package hcmus.android.crm.activities.Search;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import hcmus.android.crm.activities.Contacts.adapters.ContactAdapter;
import hcmus.android.crm.R;
import hcmus.android.crm.activities.DrawerBaseActivity;
import hcmus.android.crm.activities.Leads.adapters.LeadAdapter;
import hcmus.android.crm.databinding.ActivitySearchBinding;
import hcmus.android.crm.models.Contact;
import hcmus.android.crm.models.Lead;
import hcmus.android.crm.utilities.Constants;
import hcmus.android.crm.utilities.PreferenceManager;

public class SearchActivity extends DrawerBaseActivity {
    private ActivitySearchBinding searchBinding;
    private RecyclerView leadRecyclerView;
    private LeadAdapter leadAdapter;
    private RecyclerView contactRecyclerView;
    private ContactAdapter contactAdapter;
    private FirebaseFirestore db;
    private PreferenceManager preferenceManager;
    private EditText searchInput;

    private Spinner filterSpinner;
    private ArrayAdapter<String> filterAdapter;
    private static final String[] FILTER_OPTIONS = {"Name", "Phone", "Email", "Address", "Company", "Job"};
    public static String selectedFilter = "Name";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Search");
        searchBinding = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(searchBinding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());

        leadRecyclerView = searchBinding.leadRecyclerView;
        contactRecyclerView = searchBinding.contactRecyclerView;
        db = FirebaseFirestore.getInstance();
        setupLeadRecyclerView();
        setupContactRecyclerView();

        searchInput = searchBinding.searchInput;
        Toolbar toolbar = searchBinding.appBar.toolbar;
        setSupportActionBar(toolbar);

        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.primary_dark));

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }
        listenerSearching();

        filterSpinner = findViewById(R.id.filterSpinnerSelect);
        setupFilterSpinner();

        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedFilter = FILTER_OPTIONS[position];
                leadAdapter.getFilter().filter(searchInput.getText().toString().toString());
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedFilter = "Name";
            }
        });
    }

    private void setupFilterSpinner() {
        filterAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, FILTER_OPTIONS);
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterSpinner.setAdapter(filterAdapter);
    }


    private void setupLeadRecyclerView() {

        Query query = db.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID))
                .collection(Constants.KEY_COLLECTION_LEADS)
                .orderBy("createdAt", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<Lead> options = new FirestoreRecyclerOptions.Builder<Lead>()
                .setQuery(query, Lead.class).build();

        leadRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        leadAdapter = new LeadAdapter(options, this);
        leadRecyclerView.setAdapter(leadAdapter);
        leadAdapter.startListening(); // Start listening for data changes
    }

    private void setupContactRecyclerView() {
        Query query = db.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID))
                .collection(Constants.KEY_COLLECTION_CONTACTS)
                .orderBy("createdAt", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<Contact> options = new FirestoreRecyclerOptions.Builder<Contact>()
                .setQuery(query, Contact.class).build();

        contactRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        contactAdapter = new ContactAdapter(options, this);
        contactRecyclerView.setAdapter(contactAdapter);
        contactAdapter.startListening(); // Start listening for data changes
    }

    private void listenerSearching() {
        searchInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                leadAdapter.getFilter().filter(s.toString());
                contactAdapter.getFilter().filter(s.toString());
                Log.d("SearchActivity", s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }

}
