package hcmus.android.crm.activities.Mails;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;

import hcmus.android.crm.R;
import hcmus.android.crm.activities.BusinessCard.BusinessCardActivity;
import hcmus.android.crm.activities.BusinessCard.EditBusinessCardActivity;
import hcmus.android.crm.activities.DrawerBaseActivity;
import hcmus.android.crm.databinding.ActivityAddNewBussinessCardAcitivityBinding;
import hcmus.android.crm.databinding.ActivitySendNewMailBinding;
import hcmus.android.crm.models.Template;
import hcmus.android.crm.utilities.Constants;

public class SendNewMailActivity extends DrawerBaseActivity {
    private static final int CHOOSE_EMAILS_REQUEST_CODE = 100;
    private ActivitySendNewMailBinding binding;
    private EditText emailSubject, emailBody;
    private TextView chooseEmailsButton;
    private Button sendButton;
    private FirebaseFirestore db;
    private ArrayList<String> choosenEmails;
    private AutoCompleteTextView templateDropdown;
    private List<Template> templatesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Send New Mail");
        binding = ActivitySendNewMailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = binding.appBar.toolbar;
        setSupportActionBar(toolbar);

        getElementsById();

        db = FirebaseFirestore.getInstance();
        // Enable the back button in the action bar or toolbar
        // setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setTitle("Send New Mail");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }
        // Get UI elements
        templateDropdown = binding.templateDropdown;

        // Fetch templates from Firestore
        fetchTemplates();

        // Set listeners
        setListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (choosenEmails != null && !choosenEmails.isEmpty()) {
            sendButton.setEnabled(true);
        } else {
            sendButton.setEnabled(false);
        }
    }

    private void getElementsById() {
        // Basic fields
        emailSubject = binding.emailSubjectField;
        emailBody = binding.emailBodyField;

        // Buttons
        chooseEmailsButton = binding.textChooseEmails;
        sendButton = binding.sendButton;
    }

    private void setListeners() {
        chooseEmailsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SendNewMailActivity.this, ChooseEmailsActivity.class);
                startActivityForResult(intent, CHOOSE_EMAILS_REQUEST_CODE);
            }
        });
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSendEmail();
            }
        });
        templateDropdown.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Handle selection of a template
                Template selectedTemplate = templatesList.get(position);
                emailBody.setText(selectedTemplate.getBody());
                emailSubject.setText(selectedTemplate.getSubject());
            }
        });
    }

    private void onSendEmail() {
        String subject = emailSubject.getText().toString().trim();
        String body = emailBody.getText().toString().trim();
        StringBuilder builder = new StringBuilder();
        if (body.equals("") || subject.equals("")) {
            showToast("All fields are required!", 0);
        }
        for (String email : choosenEmails) {
            builder.append(email).append(", ");
        }
        String emailsString = builder.substring(0, builder.length() - 2);

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{emailsString});
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, body);
        intent.setType("message/rfc822");
        startActivity(Intent.createChooser(intent, "Choose email client:"));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CHOOSE_EMAILS_REQUEST_CODE) {
            if (resultCode == RESULT_OK && data != null) {
                choosenEmails = data.getStringArrayListExtra("selectedEmails");
                if (choosenEmails != null && !choosenEmails.isEmpty()) {
                    StringBuilder builder = new StringBuilder();
                    for (String email : choosenEmails) {
                        builder.append(email).append(", ");
                    }
                    String emailsString = builder.substring(0, builder.length() - 2);
                    binding.choosenEmails.setText(emailsString);
                } else {
                    binding.choosenEmails.setText("No emails selected");
                }
            }
        }
    }

    private void fetchTemplates() {
        Query query = db.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID))
                .collection(Constants.KEY_COLLECTION_TEMPLATES)
                .orderBy("createdAt", Query.Direction.ASCENDING);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    templatesList = new ArrayList<>(); // Initialize the list here
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Template template = document.toObject(Template.class);
                        templatesList.add(template);
                    }
                    // Populate the dropdown
                    Log.d("FetchTemplates", "Number of templates fetched: " + templatesList.size());
                    if (!templatesList.isEmpty()) {
                        populateDropdown();
                    }
                } else {
                    // Handle errors
                    Toast.makeText(SendNewMailActivity.this, "Failed to fetch templates", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void populateDropdown() {
        List<String> templateNames = new ArrayList<>();
        for (Template template : templatesList) {
            templateNames.add(template.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, templateNames);
        templateDropdown.setAdapter(adapter);
    }

}