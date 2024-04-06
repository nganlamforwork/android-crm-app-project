package hcmus.android.crm.activities.Settings;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.EditText;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import java.util.TimeZone;

import hcmus.android.crm.activities.Authentication.ChangePasswordActivity;
import hcmus.android.crm.activities.DrawerBaseActivity;
import hcmus.android.crm.databinding.SettingInformationBinding;
import hcmus.android.crm.utilities.Constants;

public class Information extends DrawerBaseActivity {
    private SettingInformationBinding binding;
    EditText timezoneEditText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Information");

        binding = SettingInformationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = binding.appBar.toolbar;
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


    private void setListeners() {
        binding.passwordIcon.setOnClickListener(v -> {
            // Start ChangePasswordActivity
            Intent intent = new Intent(Information.this, ChangePasswordActivity.class);
            startActivity(intent);
        });
    }
    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }
    // Declare the launcher at the top of your Activity/Fragment:
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // FCM SDK (and your app) can post notifications.
                } else {
                    // TODO: Inform user that that your app will not show notifications.
                }
            });

    private void askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED) {
                // FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // TODO: display an educational UI explaining to the user the features that will be enabled
                //       by them granting the POST_NOTIFICATION permission. This UI should provide the user
                //       "OK" and "No thanks" buttons. If the user selects "OK," directly request the permission.
                //       If the user selects "No thanks," allow the user to continue without notifications.
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }
}