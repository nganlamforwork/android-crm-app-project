package hcmus.android.crm.activities.Notes;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.firestore.FirebaseFirestore;


import hcmus.android.crm.activities.DrawerBaseActivity;
import hcmus.android.crm.databinding.ActivityCreateNoteBinding;
import hcmus.android.crm.models.Note;
import hcmus.android.crm.utilities.Constants;

public class CreateNoteActivity extends DrawerBaseActivity {

    private ActivityCreateNoteBinding binding;
    private FirebaseFirestore db;
    private boolean isEditMode = false;

    private String noteId;

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
            // Edit mode: Set button text to "Update Event"
            binding.saveNote.setOnClickListener(v -> {
                handleUpdateNote();
            });
        } else {
            // Add mode: Set button text to "Save Event"
            binding.saveNote.setOnClickListener(v -> {
                handleSaveNote();
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
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
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
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
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