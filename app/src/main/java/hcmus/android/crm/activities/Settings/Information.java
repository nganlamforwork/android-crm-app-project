package hcmus.android.crm.activities.Settings;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import java.util.TimeZone;

import hcmus.android.crm.activities.Authentication.ChangePasswordActivity;
import hcmus.android.crm.activities.DrawerBaseActivity;
import hcmus.android.crm.databinding.SettingInformationBinding;
import hcmus.android.crm.utilities.Constants;
import hcmus.android.crm.utilities.Utils;

public class Information extends DrawerBaseActivity {
    private SettingInformationBinding binding;
    EditText timezoneEditText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Information");

        binding = SettingInformationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);

        // Enable the back button in the action bar or toolbar
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setTitle("Information");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        loadTimezone();
        loadCredentials();
        setListeners();
    }

    private void loadTimezone(){
        timezoneEditText = binding.timezoneEdittext;
        String defaultTimeZone = TimeZone.getDefault().getID();
        timezoneEditText.setText(defaultTimeZone);
    }
    private void loadCredentials() {
        binding.usernameEdittext.setText(preferenceManager.getString(Constants.KEY_NAME));
        binding.emailEdittext.setText(preferenceManager.getString(Constants.KEY_EMAIL));
        binding.phoneEdittext.setText(preferenceManager.getString(Constants.KEY_PHONE_NUMBER));
        binding.planEdittext.setText("Premium✨");
    }

    private void showToast(String message, int length) {
        Utils.showToast(getApplicationContext(), message, length);
    }

    private void setListeners() {
        binding.passwordIcon.setOnClickListener(v -> {
            // Start ChangePasswordActivity
            Intent intent = new Intent(Information.this, ChangePasswordActivity.class);
            startActivity(intent);
        });
    }
}