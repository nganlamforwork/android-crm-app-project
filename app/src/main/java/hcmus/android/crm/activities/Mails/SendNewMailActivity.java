package hcmus.android.crm.activities.Mails;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.text.BreakIterator;
import java.util.ArrayList;

import hcmus.android.crm.R;
import hcmus.android.crm.activities.BusinessCard.BusinessCardActivity;
import hcmus.android.crm.activities.BusinessCard.EditBusinessCardActivity;
import hcmus.android.crm.activities.DrawerBaseActivity;
import hcmus.android.crm.databinding.ActivityAddNewBussinessCardAcitivityBinding;
import hcmus.android.crm.databinding.ActivitySendNewMailBinding;

public class SendNewMailActivity extends DrawerBaseActivity {
    private ActivitySendNewMailBinding binding;
    private EditText emailSubject, emailBody;
    private TextView chooseEmailsButton;
    private Button sendButton;
    private FirebaseFirestore db;
    private ArrayList<String> choosenEmails;
    private static final int CHOOSE_EMAILS_REQUEST_CODE = 100;
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
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setTitle("Send New Mail");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }
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
}