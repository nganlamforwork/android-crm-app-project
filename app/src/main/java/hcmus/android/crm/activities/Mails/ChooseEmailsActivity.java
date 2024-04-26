package hcmus.android.crm.activities.Mails;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import hcmus.android.crm.R;
import hcmus.android.crm.activities.Mails.adapters.LeadAdapter;
import hcmus.android.crm.databinding.ActivityChooseEmailsBinding;
import hcmus.android.crm.models.Lead;
import hcmus.android.crm.utilities.Constants;
import hcmus.android.crm.utilities.PreferenceManager;
import hcmus.android.crm.utilities.Utils;

public class ChooseEmailsActivity extends AppCompatActivity {
    private ActivityChooseEmailsBinding binding;
    private MenuItem saveChangesMenuItem;
    private RecyclerView recyclerView;
    private FirebaseFirestore db;
    private LeadAdapter leadAdapter;
    private boolean isLeadsChanged;
    protected PreferenceManager preferenceManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Choose Emails");

        binding = ActivityChooseEmailsBinding.inflate(getLayoutInflater());
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
        isLeadsChanged = false;
        setupLeadRecyclerView();
    }
    private void setupLeadRecyclerView() {
        recyclerView = binding.leadsRecyclerView;
        Query query = db.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID))
                .collection(Constants.KEY_COLLECTION_LEADS)
                .orderBy("createdAt", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<Lead> options = new FirestoreRecyclerOptions.Builder<Lead>().setQuery(query, Lead.class).build();

        checkIfListEmpty(query);

        leadAdapter = new LeadAdapter(options, this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(leadAdapter);
        recyclerView.addItemDecoration(
                new DividerItemDecoration(
                        recyclerView.getContext(),
                        LinearLayoutManager.VERTICAL
                )
        );
        leadAdapter.startListening();
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
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.save, menu);
        saveChangesMenuItem = menu.findItem(R.id.action_save);
        saveChangesMenuItem.setEnabled(false);
        return true;
    }

    public void updateSaveButtonState() {
        isLeadsChanged = true;
        saveChangesMenuItem.setEnabled(true);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_save) {
            sendEmailsBackToPreviousActivity();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void sendEmailsBackToPreviousActivity() {
        Set<String> selectedEmails = leadAdapter.getSelectedEmails();
        ArrayList<String> selectedEmailsList = new ArrayList<>(selectedEmails);

        Intent resultIntent = new Intent();
        resultIntent.putStringArrayListExtra("selectedEmails", selectedEmailsList);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }
    protected void showToast(String message, int length) {
        Utils.showToast(getApplicationContext(), message, length);
    }

}