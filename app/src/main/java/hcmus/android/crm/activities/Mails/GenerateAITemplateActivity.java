package hcmus.android.crm.activities.Mails;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.firestore.FirebaseFirestore;

import hcmus.android.crm.R;
import hcmus.android.crm.activities.DrawerBaseActivity;
import hcmus.android.crm.databinding.ActivityAddNewTemplateBinding;
import hcmus.android.crm.databinding.ActivityGenerateAitemplateBinding;

public class GenerateAITemplateActivity extends DrawerBaseActivity {
    private ActivityGenerateAitemplateBinding binding;
    private Button generateButton;
    private EditText writeAbout;
    private AutoCompleteTextView tonesDropdown, lengthsDropdown;

    // Define arrays for options
    private String[] toneOptions = {"Professional", "Casual", "Informational", "Funny", "Enthusiastic"};
    private String[] lengthOptions = {"Short", "Medium", "Long"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle("Generate Template With AI");

        binding = ActivityGenerateAitemplateBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = binding.appBar.toolbar;
        setSupportActionBar(toolbar);

        getElementsById();
        // Set up options for tone and length
        setUpOptions();

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setTitle("Generate Template With AI");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }
        setListeners();
    }

    private void getElementsById() {
        // Basic fields
        writeAbout = binding.writeAbout;
        tonesDropdown = binding.tonesDropdown;
        lengthsDropdown = binding.lengthsDropdown;
        // Create Button
        generateButton = binding.generateButton;
    }

    private void setUpOptions() {
        // Set adapter for tone options
        ArrayAdapter<String> toneAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, toneOptions);
        tonesDropdown.setAdapter(toneAdapter);

        // Set adapter for length options
        ArrayAdapter<String> lengthAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, lengthOptions);
        lengthsDropdown.setAdapter(lengthAdapter);
    }

    private void setListeners() {
        generateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generateTemplate();
            }
        });
    }


    private void generateTemplate() {
        String writeAbout = this.writeAbout.getText().toString();
        String selectedTone = tonesDropdown.getText().toString();
        String selectedLength = lengthsDropdown.getText().toString();

        // Xử lý gen AI ở đây rồi gắn dô 2 cái dưới

        String generatedSubject = "Subject đây nè";
        String generatedBody = "Body đây nè: " + selectedTone + " " + selectedLength;

        Intent resultIntent = new Intent();
        resultIntent.putExtra("generatedSubject", generatedSubject);
        resultIntent.putExtra("generatedBody", generatedBody);
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}