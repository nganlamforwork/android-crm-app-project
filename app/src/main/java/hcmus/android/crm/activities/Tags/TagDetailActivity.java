package hcmus.android.crm.activities.Tags;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Map;

import hcmus.android.crm.R;
import hcmus.android.crm.activities.BusinessCard.AddNewBusinessCardActivity;
import hcmus.android.crm.activities.DrawerBaseActivity;
import hcmus.android.crm.activities.Leads.AddNewLeadActivity;
import hcmus.android.crm.activities.Leads.adapters.LeadAdapter;
import hcmus.android.crm.activities.Tags.adapters.TagDetailAdapter;
import hcmus.android.crm.databinding.ActivityTagDetailBinding;
import hcmus.android.crm.models.Lead;
import hcmus.android.crm.models.Tag;
import hcmus.android.crm.utilities.Constants;

public class TagDetailActivity extends DrawerBaseActivity {
    private ActivityTagDetailBinding binding;
    private MenuItem saveChangesMenuItem;
    private RecyclerView recyclerView;
    private EditText tagTitle;
    private Tag tag;
    private String tagId;
    private FirebaseFirestore db;
    private boolean isLeadsChanged;
    private boolean isTagTitleChanged;
    private TagDetailAdapter tagDetailAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Tag Detail");

        binding = ActivityTagDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        db = FirebaseFirestore.getInstance();

        Toolbar toolbar = binding.appBar.toolbar;
        setSupportActionBar(toolbar);

        // Enable the back button in the action bar or toolbar
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        Intent intent = this.getIntent();
        if (intent != null) {
            tag = intent.getParcelableExtra("tagDetail");
            tagId = intent.getStringExtra("tagId");
            updateUI(tag);
        }

        isLeadsChanged = false;
        isTagTitleChanged = false;

        setupLeadRecyclerView();
        setListeners();
    }

    private void updateUI(Tag updatedTag) {
        tagTitle = binding.tagTitle;
        tagTitle.setText(updatedTag.getTitle());
    }

    private void setListeners() {
        tagTitle.addTextChangedListener(new TagDetailActivity.FieldTextWatcher());
    }

    private void setupLeadRecyclerView() {
        recyclerView = binding.leadsRecyclerView;
        Query query = db.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID))
                .collection(Constants.KEY_COLLECTION_LEADS)
                .orderBy("createdAt", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<Lead> options = new FirestoreRecyclerOptions.Builder<Lead>().setQuery(query, Lead.class).build();

        checkIfListEmpty(query);

        tagDetailAdapter = new TagDetailAdapter(options, this, tagId);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(tagDetailAdapter);
        recyclerView.addItemDecoration(
                new DividerItemDecoration(
                        recyclerView.getContext(),
                        LinearLayoutManager.VERTICAL
                )
        );
        tagDetailAdapter.startListening();
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
            updateTagDetailToFirestore();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateTagDetailToFirestore() {
        Intent resultIntent = new Intent();
        if (isTagTitleChanged) {
            String newTagTitle = tagTitle.getText().toString().trim();

            Tag updatedTag = new Tag(newTagTitle);
            db.collection(Constants.KEY_COLLECTION_USERS)
                    .document(preferenceManager.getString(Constants.KEY_USER_ID))
                    .collection(Constants.KEY_COLLECTION_TAGS)
                    .document(tagId)
                    .set(updatedTag)
                    .addOnSuccessListener(documentReference -> {
                        // Reset fields
                        tag = updatedTag;
                        isTagTitleChanged = false;

                        // Send back the new tag data to TagDetailActivity
                        resultIntent.putExtra("updatedTag", updatedTag);
                        setResult(Activity.RESULT_OK, resultIntent);
                    })
                    .addOnFailureListener(e -> {
                        showToast("Failed to update tag title", 0);
                        return;
                    });
        }
        if (isLeadsChanged) {
            Map<String, Boolean> leadCheckboxStates = tagDetailAdapter.getLeadCheckboxStates();
            for (String leadId : leadCheckboxStates.keySet()) {
                boolean isChecked = leadCheckboxStates.get(leadId);
                if (isChecked) {
                    // Checked: tagId = this tagId
                    db.collection(Constants.KEY_COLLECTION_USERS)
                            .document(preferenceManager.getString(Constants.KEY_USER_ID))
                            .collection(Constants.KEY_COLLECTION_LEADS)
                            .document(leadId)
                            .update("tagId", tagId)
                            .addOnSuccessListener(aVoid -> {
                            })
                            .addOnFailureListener(e -> {
                                showToast("Failed to update lead with id " + leadId, 0);
                                return;
                            });
                } else {
                    // Unchecked: tagId = null
                    db.collection(Constants.KEY_COLLECTION_USERS)
                            .document(preferenceManager.getString(Constants.KEY_USER_ID))
                            .collection(Constants.KEY_COLLECTION_LEADS)
                            .document(leadId)
                            .update("tagId", null)
                            .addOnSuccessListener(aVoid -> {
                            })
                            .addOnFailureListener(e -> {
                                showToast("Failed to update lead with id " + leadId, 0);
                                return;
                            });
                }
            }
            isLeadsChanged = false;
        }
        showToast("Update tag detail successfully", 0);
        saveChangesMenuItem.setEnabled(false);
    }

    private class FieldTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (!s.toString().equals(tag.getTitle()) || !s.toString().isEmpty()) {
                isTagTitleChanged = true;
                saveChangesMenuItem.setEnabled(true);
            } else {
                isTagTitleChanged = false;
                saveChangesMenuItem.setEnabled(false);
            }

        }
    }
}