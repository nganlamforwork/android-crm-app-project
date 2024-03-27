package hcmus.android.crm.activities.Leads;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import hcmus.android.crm.R;
import hcmus.android.crm.databinding.ActivityLeadDetailBinding;
import hcmus.android.crm.models.Lead;
import hcmus.android.crm.utilities.Constants;

public class LeadDetailActivity extends AppCompatActivity {
    private ActivityLeadDetailBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLeadDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);

        // Enable the back button in the action bar or toolbar
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setTitle("Lead Details");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        Intent intent = this.getIntent();
        if(intent != null) {
            Lead lead = intent.getParcelableExtra("leadDetails");

            byte[] bytes = Base64.decode(lead.getImage(), Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            binding.avatar.setImageBitmap(bitmap);

            binding.leadName.setText(lead.getName());
            binding.leadPhone.setText(lead.getPhone());
            binding.leadEmail.setText(lead.getEmail());
            binding.leadAddress.setText(lead.getAddress());
            binding.leadNotes.setText(lead.getNotes());
        }
    }
    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_edit) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
