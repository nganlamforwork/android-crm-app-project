package hcmus.android.crm.activities.Leads;

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
import hcmus.android.crm.activities.Leads.adapters.LeadAdapter;
import hcmus.android.crm.databinding.ActivityLeadBinding;
import hcmus.android.crm.models.Lead;
import hcmus.android.crm.utilities.Constants;
import hcmus.android.crm.utilities.TouchHelper;

public class LeadActivity extends DrawerBaseActivity implements LeadAdapter.OnLeadItemClickListener {

    private ActivityLeadBinding binding;
    private RecyclerView recyclerView;
    private Query query;
    private ListenerRegistration listenerRegistration;
    private FirebaseFirestore db;
    private LeadAdapter leadAdapter;
    private List<Lead> leadList;
    private ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @SuppressLint("NotifyDataSetChanged")
        @Override
        public void onActivityResult(ActivityResult result) {
            if(result.getResultCode() == RESULT_OK) {
                Intent intent  = result.getData();
                assert intent != null;
                Lead newLead = intent.getParcelableExtra("newLead");
                leadList.add(0, newLead);
                leadAdapter.notifyItemInserted(0);
                leadAdapter.notifyDataSetChanged();
            }
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Leads");

        binding = ActivityLeadBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        db = FirebaseFirestore.getInstance();

        recyclerView = binding.leadRecyclerView;
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        leadList = new ArrayList<>();
        leadAdapter = new LeadAdapter(this, leadList);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new TouchHelper(leadAdapter));
        itemTouchHelper.attachToRecyclerView(recyclerView);
        recyclerView.setAdapter(leadAdapter);

        leadAdapter.setOnLeadItemClickListener(this);

        showData();
        setListeners();
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onResume() {
        super.onResume();
        navigationView.setCheckedItem(R.id.nav_leads);
    }


    private void setListeners() {
        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activityResultLauncher.launch(new Intent(LeadActivity.this, AddNewLeadActivity.class));
            }
        });
    }

    @Override
    public void onLeadItemClick(int position) {
        // Handle the item click event
        Lead lead = leadList.get(position);
        Intent intent = new Intent(LeadActivity.this, LeadDetailActivity.class);
        intent.putExtra("leadDetails", lead);
        intent.putExtra("leadId", lead.LeadId);
        startActivity(intent);
    }

    private void showData() {
        query = db.collection(Constants.KEY_COLLECTION_LEADS).orderBy("createdAt", Query.Direction.DESCENDING);
        listenerRegistration = query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                assert value != null;
                for (DocumentChange documentChange : value.getDocumentChanges()) {
                    String id = documentChange.getDocument().getId();
                    Lead lead = documentChange.getDocument().toObject(Lead.class).withId(id);

                    leadList.add(lead);
                }
                leadAdapter.notifyDataSetChanged();
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
                leadAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                String query = newText.toLowerCase(Locale.ROOT);
                leadAdapter.getFilter().filter(query);
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
