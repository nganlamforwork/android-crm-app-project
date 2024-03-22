package hcmus.android.crm.activities.Leads;

import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import hcmus.android.crm.DrawerBase;
import hcmus.android.crm.databinding.ActivityLeadBinding;
import hcmus.android.crm.R;
import hcmus.android.crm.databinding.ActivitySettingsBinding;

public class LeadActivity extends DrawerBase {

    private AppBarConfiguration appBarConfiguration;
    private ActivityLeadBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLeadBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setTitle("Leads");

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAnchorView(R.id.fab)
                        .setAction("Action", null).show();
            }
        });
    }

}