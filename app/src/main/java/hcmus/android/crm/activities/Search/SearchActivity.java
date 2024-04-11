package hcmus.android.crm.activities.Search;

// Android and androidx imports
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

// Firebase imports
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

// Project specific imports
import hcmus.android.crm.R;
import hcmus.android.crm.activities.Contacts.adapters.ContactAdapter;
import hcmus.android.crm.activities.DrawerBaseActivity;
import hcmus.android.crm.activities.Leads.adapters.LeadAdapter;
import hcmus.android.crm.databinding.ActivitySearchBinding;
import hcmus.android.crm.models.Lead;
import hcmus.android.crm.utilities.Constants;
import hcmus.android.crm.utilities.PreferenceManager;

public class SearchActivity extends DrawerBaseActivity {
    private ActivitySearchBinding searchBinding;
    private RecyclerView leadRecyclerView;
    private LeadAdapter leadAdapter;
    private FirebaseFirestore db;
    private PreferenceManager preferenceManager;
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Search");
        searchBinding = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(searchBinding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());

        leadRecyclerView = searchBinding.leadRecyclerView;
        db = FirebaseFirestore.getInstance();
        setupLeadRecyclerView();

        searchView = searchBinding.searchView;

        listenerSearching();
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

    private void listenerSearching() {
        searchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchView.setIconified(false);
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                leadAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                leadAdapter.getFilter().filter(newText);
                Log.d("SearchActivity", newText);
                return false;
            }
        });
    }

}