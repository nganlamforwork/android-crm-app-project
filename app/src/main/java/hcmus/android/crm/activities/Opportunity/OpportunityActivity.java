package hcmus.android.crm.activities.Opportunity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import hcmus.android.crm.R;
import hcmus.android.crm.activities.DrawerBaseActivity;
import hcmus.android.crm.activities.Opportunity.adapters.OpportunityAdapter;
import hcmus.android.crm.databinding.ActivityOpportunityBinding;
import hcmus.android.crm.models.Opportunity;
import androidx.recyclerview.widget.DividerItemDecoration;
import hcmus.android.crm.utilities.Constants;

public class OpportunityActivity extends DrawerBaseActivity {
    private ActivityOpportunityBinding binding;
    private FirebaseFirestore db;
    private OpportunityAdapter opportunityAdapter;
    private RecyclerView recyclerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Opportunity");

        binding = ActivityOpportunityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        db = FirebaseFirestore.getInstance();

        setupOpportunityRecyclerview();
        setListeners();
    }


    @Override
    protected void onResume() {
        super.onResume();
        navigationView.setCheckedItem(R.id.nav_opportunity);
        if (opportunityAdapter != null) {
            opportunityAdapter.startListening();
            opportunityAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (opportunityAdapter != null) opportunityAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (opportunityAdapter != null) opportunityAdapter.stopListening();
    }

    private void setupOpportunityRecyclerview() {
        recyclerView = binding.opportunityRecyclerView;
        Query query = db.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID))
                .collection(Constants.KEY_COLLECTION_OPPORTUNITIES)
                .orderBy("createdAt", Query.Direction.DESCENDING);

        checkIfListEmpty(query);

        FirestoreRecyclerOptions<Opportunity> options = new FirestoreRecyclerOptions.Builder<Opportunity>()
                .setQuery(query, Opportunity.class).build();

        opportunityAdapter = new OpportunityAdapter(options, this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(opportunityAdapter);
        recyclerView.addItemDecoration(
                new DividerItemDecoration(
                        recyclerView.getContext(),
                        LinearLayoutManager.VERTICAL
                )
        );
        opportunityAdapter.startListening();
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

    private void setListeners() {
        binding.createOpportunity.setOnClickListener(v -> {
            Intent intent = new Intent(OpportunityActivity.this, CreateOpportunityActivity.class);
            startActivity(intent);
        });
    }
}