package hcmus.android.crm.activities.Mails;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import hcmus.android.crm.R;
import hcmus.android.crm.activities.DrawerBaseActivity;
import hcmus.android.crm.activities.Leads.AddNewLeadActivity;
import hcmus.android.crm.activities.Leads.LeadActivity;
import hcmus.android.crm.activities.Leads.ScanBusinessCardActivity;
import hcmus.android.crm.activities.Leads.adapters.LeadAdapter;
import hcmus.android.crm.activities.Mails.adapters.TemplateAdapter;
import hcmus.android.crm.databinding.ActivityLeadBinding;
import hcmus.android.crm.databinding.ActivityTemplatesBinding;
import hcmus.android.crm.models.Lead;
import hcmus.android.crm.models.Template;
import hcmus.android.crm.utilities.Constants;
import hcmus.android.crm.utilities.PreferenceManager;
import hcmus.android.crm.utilities.Utils;

public class TemplatesActivity extends DrawerBaseActivity {
    private ActivityTemplatesBinding binding;
    private RecyclerView recyclerView;
    private FirebaseFirestore db;
    private boolean clicked;
    private TemplateAdapter templateAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Templates");

        binding = ActivityTemplatesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        db = FirebaseFirestore.getInstance();

        Toolbar toolbar = binding.appBar.toolbar;
        setSupportActionBar(toolbar);
        preferenceManager = new PreferenceManager(getApplicationContext());

        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.primary_dark));

        // Enable the back button in the action bar or toolbar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        setupLeadRecyclerView();
        setListeners();
    }

    private void setupLeadRecyclerView() {
        recyclerView = binding.templateRecyclerView;
        Query query = db.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID))
                .collection(Constants.KEY_COLLECTION_TEMPLATES)
                .orderBy("createdAt", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<Template> options = new FirestoreRecyclerOptions.Builder<Template>().setQuery(query, Template.class).build();

        checkIfListEmpty(query);

        templateAdapter = new TemplateAdapter(options, this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(templateAdapter);
        recyclerView.addItemDecoration(
                new DividerItemDecoration(
                        recyclerView.getContext(),
                        LinearLayoutManager.VERTICAL
                )
        );
        templateAdapter.startListening();
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
        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(TemplatesActivity.this, AddNewTemplateActivity.class));
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (templateAdapter != null) {
            templateAdapter.startListening();
            templateAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (templateAdapter != null) templateAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (templateAdapter != null) templateAdapter.stopListening();
    }
    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }

    protected void showToast(String message, int length) {
        Utils.showToast(getApplicationContext(), message, length);
    }
}