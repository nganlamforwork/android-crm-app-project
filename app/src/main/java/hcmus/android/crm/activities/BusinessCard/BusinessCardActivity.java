package hcmus.android.crm.activities.BusinessCard;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import hcmus.android.crm.R;
import hcmus.android.crm.activities.DrawerBaseActivity;
import hcmus.android.crm.databinding.ActivityBusinessCardBinding;
public class BusinessCardActivity extends DrawerBaseActivity {
    private ActivityBusinessCardBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle("Your Business Card");

        binding = ActivityBusinessCardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCardViewClicked(v);
            }
        });
    }
    protected void onResume() {
        super.onResume();
        navigationView.setCheckedItem(R.id.nav_business_card);
    }
    public void onCardViewClicked(View view) {

        Intent intent = new Intent(BusinessCardActivity.this, AddNewBusinessCardActivity.class);
        startActivity(intent);
        Toast.makeText(this, "CardView clicked", Toast.LENGTH_SHORT).show();
    }

}