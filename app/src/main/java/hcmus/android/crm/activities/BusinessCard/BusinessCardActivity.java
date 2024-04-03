package hcmus.android.crm.activities.BusinessCard;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

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

        // Bắt sự kiện khi CardView được click
        binding.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCardViewClicked(v);
            }
        });
    }
    // Phương thức được gọi khi CardView được click
    public void onCardViewClicked(View view) {
        // Xử lý sự kiện ở đây
        Toast.makeText(this, "CardView clicked", Toast.LENGTH_SHORT).show();
    }

}