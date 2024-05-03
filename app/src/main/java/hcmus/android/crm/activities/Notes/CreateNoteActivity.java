package hcmus.android.crm.activities.Notes;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.firestore.FirebaseFirestore;


import hcmus.android.crm.activities.DrawerBaseActivity;
import hcmus.android.crm.activities.Opportunity.OpportunityDetailActivity;
import hcmus.android.crm.databinding.ActivityCreateNoteBinding;
import hcmus.android.crm.models.Note;
import hcmus.android.crm.models.Opportunity;
import hcmus.android.crm.utilities.Constants;

public class CreateNoteActivity extends DrawerBaseActivity {

    private ActivityCreateNoteBinding binding;
    private FirebaseFirestore db;
    private boolean isEditMode = false;

    private String noteId, opportunityId;

    private boolean fromOppo = false;
    private Opportunity opportunity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Add new note");

        binding = ActivityCreateNoteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        db = FirebaseFirestore.getInstance();

        Toolbar toolbar = binding.toolbarCreateNote;
        setSupportActionBar(toolbar);

        // Enable the back button in the action bar or toolbar
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        noteId = getIntent().getStringExtra("noteId");
        opportunityId = getIntent().getStringExtra("opportunityId");
        opportunity = getIntent().getParcelableExtra("opportunityDetails");

        if (opportunityId != null) {
            fromOppo = true;
        }
        if (noteId != null) {
            isEditMode = true;
            setTitle("Edit event");
            populateNoteData();
        }

        setListeners();
    }

    private void populateNoteData() {
        binding.createNoteTitle.setText(getIntent().getStringExtra("title"));
        binding.createNoteContent.setText(getIntent().getStringExtra("content"));
    }


    private void setListeners() {

        if (isEditMode) {
            if (!fromOppo) {
                binding.saveNote.setOnClickListener(v -> {
                    handleUpdateNote();
                });
            } else {
                binding.saveNote.setOnClickListener(v -> {
                    handleUpdateNoteToOppo();
                });
            }

        } else {
            if (!fromOppo) {
                binding.saveNote.setOnClickListener(v -> {
                    handleSaveNote();
                });
            } else {
                binding.saveNote.setOnClickListener(v -> {
                    handleSaveNoteToOppo();
                });
            }
        }
    }

    private void handleSaveNoteToOppo() {
        String title = binding.createNoteTitle.getText().toString();
        String content = binding.createNoteContent.getText().toString();

        if (title.isEmpty() || content.isEmpty()) {
            showToast("Both field are required", 0);
        } else {
            Note note = new Note(title, content);
            db.collection(Constants.KEY_COLLECTION_USERS)
                    .document(preferenceManager.getString(Constants.KEY_USER_ID))
                    .collection(Constants.KEY_COLLECTION_OPPORTUNITIES)
                    .document(opportunityId)
                    .collection(Constants.KEY_COLLECTION_NOTES)
                    .add(note)
                    .addOnSuccessListener(documentReference -> {
                        showToast("New note created", 0);
                        Intent intent = new Intent(CreateNoteActivity.this, OpportunityDetailActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra("opportunityDetails", opportunity);
                        intent.putExtra("opportunityId", opportunityId);
                        startActivity(intent);
                    })
                    .addOnFailureListener(e -> {
                        // Handle failure, e.g., show error message
                        showToast("Failed to create note", 0);
                    });
        }
    }

    private void handleUpdateNoteToOppo() {
        String title = binding.createNoteTitle.getText().toString();
        String content = binding.createNoteContent.getText().toString();

        if (title.isEmpty() || content.isEmpty()) {
            showToast("Both field are required", 0);
        } else {
            Note updatedNote = new Note(title, content);
            db.collection(Constants.KEY_COLLECTION_USERS)
                    .document(preferenceManager.getString(Constants.KEY_USER_ID))
                    .collection(Constants.KEY_COLLECTION_OPPORTUNITIES)
                    .document(opportunityId)
                    .collection(Constants.KEY_COLLECTION_NOTES)
                    .document(noteId)
                    .set(updatedNote)
                    .addOnSuccessListener(documentReference -> {
                        showToast("Note is updated", 0);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        // Handle failure, e.g., show error message
                        showToast("Failed to update note", 0);
                    });
        }
    }

    private void handleUpdateNote() {
        String title = binding.createNoteTitle.getText().toString();
        String content = binding.createNoteContent.getText().toString();

        if (title.isEmpty() || content.isEmpty()) {
            showToast("Both field are required", 0);
        } else {
            Note updatedNote = new Note(title, content);
            db.collection(Constants.KEY_COLLECTION_USERS)
                    .document(preferenceManager.getString(Constants.KEY_USER_ID))
                    .collection(Constants.KEY_COLLECTION_LEADS)
                    .document(preferenceManager.getString("selectedLead"))
                    .collection(Constants.KEY_COLLECTION_NOTES)
                    .document(noteId)
                    .set(updatedNote)
                    .addOnSuccessListener(documentReference -> {
                        showToast("Note is updated", 0);
                        Intent intent = new Intent(this, NoteActivity.class);
                        startActivity(intent);
                    })
                    .addOnFailureListener(e -> {
                        // Handle failure, e.g., show error message
                        showToast("Failed to update note", 0);
                    });
        }
    }

    private void handleSaveNote() {
        String title = binding.createNoteTitle.getText().toString();
        String content = binding.createNoteContent.getText().toString();

        if (title.isEmpty() || content.isEmpty()) {
            showToast("Both field are required", 0);
        } else {
            Note note = new Note(title, content);
            db.collection(Constants.KEY_COLLECTION_USERS)
                    .document(preferenceManager.getString(Constants.KEY_USER_ID))
                    .collection(Constants.KEY_COLLECTION_LEADS)
                    .document(preferenceManager.getString("selectedLead"))
                    .collection(Constants.KEY_COLLECTION_NOTES)
                    .add(note)
                    .addOnSuccessListener(documentReference -> {
                        showToast("New note created", 0);
                        Intent intent = new Intent(this, NoteActivity.class);
                        startActivity(intent);
                    })
                    .addOnFailureListener(e -> {
                        // Handle failure, e.g., show error message
                        showToast("Failed to create note", 0);
                    });
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }

}