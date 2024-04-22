package hcmus.android.crm.activities.Settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.RadioGroup;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;

import hcmus.android.crm.R;
import hcmus.android.crm.activities.DrawerBaseActivity;
import hcmus.android.crm.databinding.ActivitySettingsAppearanceBinding;
import hcmus.android.crm.databinding.SettingInformationBinding;

public class SettingsAppearanceActivity extends DrawerBaseActivity {
    private ActivitySettingsAppearanceBinding binding;
    private RadioGroup radioGroup;
    private boolean isNightMode;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Appearance");

        binding = ActivitySettingsAppearanceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = binding.appBar.toolbar;
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Appearance");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        // Initialize
        radioGroup = binding.radioGroup;
        sharedPreferences = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);
        editor = sharedPreferences.edit();

        // Set checked state based on saved preference
        int currentMode = sharedPreferences.getInt("mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        if (currentMode == AppCompatDelegate.MODE_NIGHT_YES) {
            radioGroup.check(R.id.radioButtonDark);
        } else {
            radioGroup.check(R.id.radioButtonLight);
        }

        setListeners();
    }
    private void setListeners(){
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            int newMode;
            if (checkedId == R.id.radioButtonLight) {
                newMode = AppCompatDelegate.MODE_NIGHT_NO;
            } else {
                newMode = AppCompatDelegate.MODE_NIGHT_YES;
            }

            if (newMode != sharedPreferences.getInt("mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)) {
                editor.putInt("mode", newMode);
                AppCompatDelegate.setDefaultNightMode(newMode);
                editor.apply();
                recreate();
            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        int savedMode = sharedPreferences.getInt("mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        if (savedMode == AppCompatDelegate.MODE_NIGHT_YES) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

}