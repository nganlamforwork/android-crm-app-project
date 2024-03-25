package hcmus.android.crm.activities.Leads;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import hcmus.android.crm.DrawerBase;
import hcmus.android.crm.activities.Authentication.SignUpActivity;
import hcmus.android.crm.activities.Leads.adapters.LeadAdapter;
import hcmus.android.crm.databinding.ActivityLeadBinding;
import hcmus.android.crm.R;
import hcmus.android.crm.databinding.ActivitySettingsBinding;
import hcmus.android.crm.models.Lead;
import hcmus.android.crm.utilities.Constants;

public class LeadActivity extends DrawerBase {

    private AppBarConfiguration appBarConfiguration;
    private ActivityLeadBinding binding;
    private RecyclerView recyclerView;

    private FirebaseFirestore db;
    private LeadAdapter leadAdapter;
    private List<Lead> leadList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLeadBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setTitle("Leads");
        recyclerView = binding.leadRecyclerView;
        db = FirebaseFirestore.getInstance();

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(LeadActivity.this));

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddNewLead.newInstance().show(getSupportFragmentManager(), AddNewLead.TAG);
            }
        });

        leadList = new ArrayList<>();
        leadAdapter = new LeadAdapter(LeadActivity.this, leadList);

        recyclerView.setAdapter(leadAdapter);
        showData();
    }

    private void showData() {
        db.collection(Constants.KEY_COLLECTION_LEADS).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                for (DocumentChange documentChange : value.getDocumentChanges()) {
                    String id = documentChange.getDocument().getId();
                    Lead lead = documentChange.getDocument().toObject(Lead.class).withId(id);

                    leadList.add(lead);
                    leadAdapter.notifyDataSetChanged();
                }
                Collections.reverse(leadList);
            }
        });
    }
}