package hcmus.android.crm.activities.Mails;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.google.firebase.firestore.FirebaseFirestore;
import hcmus.android.crm.activities.DrawerBaseActivity;
import hcmus.android.crm.databinding.ActivityTemplateDetailBinding;
import hcmus.android.crm.models.Template;
import hcmus.android.crm.utilities.Constants;

public class TemplateDetailActivity extends DrawerBaseActivity {
    private static final int REQUEST_CODE_EDIT_TEMPLATE = 101;
    private ActivityTemplateDetailBinding binding;
    private Template template;
    private String templateId;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Template Detail");

        binding = ActivityTemplateDetailBinding.inflate(getLayoutInflater());
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
            template = intent.getParcelableExtra("templateDetail");
            templateId = intent.getStringExtra("templateId");
            updateUI(template);
        }

        setListeners();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_EDIT_TEMPLATE && resultCode == RESULT_OK) {
            if (data != null) {
                Template updatedTemplate = data.getParcelableExtra("updatedTemplate");
                if (updatedTemplate != null) {
                    updateUI(updatedTemplate);
                    template = updatedTemplate;
                }
            }
        }
    }

    private void setListeners() {
        binding.textEditTemplate.setOnClickListener(v -> {
            Intent intent = new Intent(TemplateDetailActivity.this, EditTemplateActivity.class);

            intent.putExtra("templateId", templateId);
            intent.putExtra("template", template);

            startActivityForResult(intent, REQUEST_CODE_EDIT_TEMPLATE);
        });
        binding.textDeleteTemplate.setOnClickListener(v -> {
            showDeleteConfirmationDialog();
        });
    }

    private void updateUI(Template updatedTemplate) {
        binding.templateName.setText(updatedTemplate.getName());
        binding.templateSubject.setText(updatedTemplate.getSubject());
        binding.templateBody.setText(updatedTemplate.getBody());
    }
    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Template");
        builder.setMessage("Are you sure you want to delete this template?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteLead();
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

    private void deleteLead() {
        db.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID))
                .collection(Constants.KEY_COLLECTION_TEMPLATES)
                .document(templateId).delete();
        finish();
    }
    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }
}