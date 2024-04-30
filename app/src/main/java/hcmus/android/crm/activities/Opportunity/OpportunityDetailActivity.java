package hcmus.android.crm.activities.Opportunity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import hcmus.android.crm.R;
import hcmus.android.crm.activities.DrawerBaseActivity;
import hcmus.android.crm.activities.Leads.adapters.LeadAdapter;
import hcmus.android.crm.activities.Mails.ChooseEmailsActivity;
import hcmus.android.crm.activities.Notes.CreateNoteActivity;
import hcmus.android.crm.activities.Notes.adapters.NoteAdapter;
import hcmus.android.crm.databinding.ActivityOpportunityDetailBinding;
import hcmus.android.crm.models.Lead;
import hcmus.android.crm.models.Note;
import hcmus.android.crm.models.Opportunity;
import hcmus.android.crm.utilities.Constants;

public class OpportunityDetailActivity extends DrawerBaseActivity {
    private ActivityOpportunityDetailBinding binding;
    private FirebaseFirestore db;

    private Opportunity opportunity;
    private String opportunityId;
    private NoteAdapter noteAdapter;
    private LeadAdapter leadAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Opportunity");

        binding = ActivityOpportunityDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        db = FirebaseFirestore.getInstance();

        Toolbar toolbar = binding.appBar.toolbar;
        setSupportActionBar(toolbar);

        // Enable the back button in the action bar or toolbar
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Intent intent = this.getIntent();
        if (intent != null) {
            opportunity = intent.getParcelableExtra("opportunityDetails");
            opportunityId = intent.getStringExtra("opportunityId");
            updateUI(opportunity);
        }
        setupLeadRecyclerView();
        setupNotesRecyclerview();
        setListeners();
    }

    private void setupNotesRecyclerview() {

        Query query = db.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID))
                .collection(Constants.KEY_COLLECTION_OPPORTUNITIES)
                .document(opportunityId)
                .collection(Constants.KEY_COLLECTION_NOTES)
                .orderBy("createdAt", Query.Direction.DESCENDING);
        checkIfNotesListEmpty(query);

        FirestoreRecyclerOptions<Note> options = new FirestoreRecyclerOptions.Builder<Note>()
                .setQuery(query, Note.class).build();

        noteAdapter = new NoteAdapter(options, this, preferenceManager, opportunityId);
        binding.noteRecyclerView.setHasFixedSize(false);
        binding.noteRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.noteRecyclerView.setAdapter(noteAdapter);
        noteAdapter.startListening();
    }

    private void setupLeadRecyclerView() {
        Query query = db.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID))
                .collection(Constants.KEY_COLLECTION_OPPORTUNITIES)
                .document(opportunityId)
                .collection(Constants.KEY_COLLECTION_LEADS)
                .orderBy("createdAt", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<Lead> options = new FirestoreRecyclerOptions.Builder<Lead>().setQuery(query, Lead.class).build();

        checkIfLeadsListEmpty(query);

        leadAdapter = new LeadAdapter(options, this);
        binding.leadRecyclerView.setHasFixedSize(false);
        binding.leadRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.leadRecyclerView.setAdapter(leadAdapter);
        binding.leadRecyclerView.addItemDecoration(
                new DividerItemDecoration(
                        binding.leadRecyclerView.getContext(),
                        LinearLayoutManager.VERTICAL
                )
        );
        leadAdapter.startListening();
    }

    private void checkIfLeadsListEmpty(Query query) {
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().getDocuments().size() > 0) {
                        binding.leadRecyclerView.setVisibility(View.VISIBLE);
                        binding.leadHeader.setVisibility(View.VISIBLE);
                    } else {
                        binding.leadRecyclerView.setVisibility(View.GONE);
                        binding.leadHeader.setVisibility(View.GONE);
                    }
                }
            }
        });
    }

    private void checkIfNotesListEmpty(Query query) {
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().getDocuments().size() > 0) {
                        binding.noteRecyclerView.setVisibility(View.VISIBLE);
                        binding.noteHeader.setVisibility(View.VISIBLE);
                    } else {
                        binding.noteRecyclerView.setVisibility(View.GONE);
                        binding.noteHeader.setVisibility(View.GONE);
                    }
                }
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void updateUI(Opportunity opportunity) {
        if (opportunity != null) {
            binding.opportunityName.setText(opportunity.getName());
            binding.expectedDate.setText("Created On " + opportunity.getExpectedDate());
            binding.opportunityStatus.setText("Status " + opportunity.getStatus());
            if (opportunity.getStatus().equals("In Prospect")) {
                binding.opportunityStatus.setTextColor(ContextCompat.getColor(this, R.color.pre));
            } else if (opportunity.getStatus().equals("Negotiation")) {
                binding.opportunityStatus.setTextColor(ContextCompat.getColor(this, R.color.warning));
            } else if (opportunity.getStatus().equals("Closed Won")) {
                binding.opportunityStatus.setTextColor(ContextCompat.getColor(this, R.color.success));
            } else if (opportunity.getStatus().equals("Closed Lost")) {
                binding.opportunityStatus.setTextColor(ContextCompat.getColor(this, R.color.error));
            }

            binding.opportunityPrice.setText(opportunity.getPrice().toString());
            binding.opportunityPossibility.setText("Posibility (" + opportunity.getPossibility().toString() + "%)");
            double possibility = opportunity.getPossibility();
            int roundedPossibility = (int) Math.round(possibility);
            binding.opportunitySeekBar.setProgress(roundedPossibility);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setListeners() {
        binding.opportunitySeekBar.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        binding.editOpportunity.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), CreateOpportunityActivity.class);
            intent.putExtra("opportunityId", opportunityId);
            intent.putExtra("opportunity", opportunity);
            startActivity(intent);
        });

        binding.btnAddNote.setOnClickListener(v -> {
            Intent intent = new Intent(OpportunityDetailActivity.this, CreateNoteActivity.class);
            intent.putExtra("opportunityId", opportunityId);
            intent.putExtra("opportunityDetails", opportunity);
            startActivity(intent);
        });

        binding.btnAddLead.setOnClickListener(v -> {
            Intent intent = new Intent(OpportunityDetailActivity.this, ChooseEmailsActivity.class);
            intent.putExtra("opportunityId", opportunityId);
            startActivity(intent);
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUI(opportunity);
        if (noteAdapter != null) {
            noteAdapter.startListening();
            noteAdapter.notifyDataSetChanged();
        }
        if (leadAdapter != null) {
            leadAdapter.startListening();
            leadAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (noteAdapter != null) noteAdapter.startListening();
        if (leadAdapter != null) leadAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (leadAdapter != null) leadAdapter.startListening();
        if (noteAdapter != null) noteAdapter.stopListening();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.delete, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_delete) {
            showDeleteConfirmationDialog();
        }

        return super.onOptionsItemSelected(item);
    }
    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Opportunity");
        builder.setMessage("Are you sure you want to delete this opportunity?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteOpportunity();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    private void deleteOpportunity() {
        db.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID))
                .collection(Constants.KEY_COLLECTION_OPPORTUNITIES)
                .document(opportunityId).delete();
        finish();
    }
}