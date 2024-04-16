package hcmus.android.crm.activities.Opportunity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.firebase.firestore.FirebaseFirestore;

import hcmus.android.crm.R;
import hcmus.android.crm.activities.DrawerBaseActivity;
import hcmus.android.crm.databinding.ActivityOpportunityDetailBinding;
import hcmus.android.crm.models.Opportunity;

public class OpportunityDetailActivity extends DrawerBaseActivity {
    private ActivityOpportunityDetailBinding binding;
    private FirebaseFirestore db;

    private Opportunity opportunity;
    private String opportunityId;

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

        setListeners();
    }

    @SuppressLint("SetTextI18n")
    private void updateUI(Opportunity opportunity) {
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
    }
}