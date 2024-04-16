package hcmus.android.crm.activities.Settings;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import hcmus.android.crm.R;
import hcmus.android.crm.activities.Authentication.ChangePasswordActivity;
import hcmus.android.crm.activities.DrawerBaseActivity;
import hcmus.android.crm.databinding.SettingInformationBinding;
import hcmus.android.crm.utilities.Constants;
import hcmus.android.crm.utilities.Utils;

public class Information extends DrawerBaseActivity {
    private SettingInformationBinding binding;
    EditText timezoneEditText;
    private FirebaseFirestore db;
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Information");

        binding = SettingInformationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = binding.appBar.toolbar;
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Information");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        db = FirebaseFirestore.getInstance();

        binding.planEdittext.setEnabled(false);
        binding.timezoneEdittext.setEnabled(false);
        binding.usernameEdittext.setEnabled(false);
        binding.emailEdittext.setEnabled(false);
        binding.phoneEdittext.setEnabled(false);

        loadTimezone();
        loadCredentials();
        setListeners();
    }

    public void onEditClicked(View view) {
        ActionBar actionBar = getSupportActionBar();
        isEditMode = !isEditMode;
        binding.usernameEdittext.setEnabled(isEditMode);
        binding.emailEdittext.setEnabled(isEditMode);
        binding.phoneEdittext.setEnabled(isEditMode);
        invalidateOptionsMenu();

        if (isEditMode) {
            if (actionBar != null) {
                actionBar.setTitle("");
                actionBar.setDisplayHomeAsUpEnabled(false);
                actionBar.setDisplayShowHomeEnabled(false);
            }
            binding.editText.setVisibility(View.GONE);
            binding.usernameEdittext.setBackgroundResource(R.drawable.edittext_background);
            binding.emailEdittext.setBackgroundResource(R.drawable.edittext_background);
            binding.phoneEdittext.setBackgroundResource(R.drawable.edittext_background);
        } else {
            if (actionBar != null) {
                actionBar.setTitle("Information");
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setDisplayShowHomeEnabled(true);
            }
            binding.editText.setVisibility(View.VISIBLE);
            binding.usernameEdittext.setBackgroundColor(Color.TRANSPARENT);
            binding.emailEdittext.setBackgroundColor(Color.TRANSPARENT);
            binding.phoneEdittext.setBackgroundColor(Color.TRANSPARENT);
        }
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
            Intent intent = new Intent(Information.this, ChangePasswordActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (isEditMode) {
            getMenuInflater().inflate(R.menu.edit_infomation, menu);
        } else {
            menu.clear();
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        isEditMode = false;
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Information");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }
        binding.editText.setVisibility(View.VISIBLE);
        binding.usernameEdittext.setBackgroundColor(Color.TRANSPARENT);
        binding.emailEdittext.setBackgroundColor(Color.TRANSPARENT);
        binding.phoneEdittext.setBackgroundColor(Color.TRANSPARENT);
        binding.usernameEdittext.setEnabled(isEditMode);
        binding.emailEdittext.setEnabled(isEditMode);
        binding.phoneEdittext.setEnabled(isEditMode);
        invalidateOptionsMenu();

        if (id == R.id.action_close) {
            return true;
        }

        if (id == R.id.action_save) {
            if (isValidUpdateDetails()) {
                saveChanges();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void saveChanges() {
        String userId = preferenceManager.getString(Constants.KEY_USER_ID);
        DocumentReference userDocRef = db.collection("users").document(userId);
        String newName = binding.usernameEdittext.getText().toString();
        String newEmail = binding.emailEdittext.getText().toString();
        String newPhone = binding.phoneEdittext.getText().toString();

        userDocRef.update("name", newName,
                        "email", newEmail,
                        "phone", newPhone)
                .addOnSuccessListener(aVoid -> {
                    // Handle success
                    Log.d(TAG, "User information updated successfully");
                    preferenceManager.putString(Constants.KEY_NAME, newName);
                    preferenceManager.putString(Constants.KEY_EMAIL, newEmail);
                    preferenceManager.putString(Constants.KEY_PHONE_NUMBER, newPhone);
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                    Log.e(TAG, "Failed to update user information: " + e.getMessage());
                });
    }

    private boolean isValidUpdateDetails() {
        if (binding.usernameEdittext.getText().toString().trim().isEmpty()) {
            showToast("Please enter your name", 0);
            return false;
        } else if (binding.emailEdittext.getText().toString().trim().isEmpty()) {
            showToast("Please enter your email", 0);
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.emailEdittext.getText().toString()).matches()) {
            showToast("Enter valid email", 0);
            return false;
        } else if (binding.phoneEdittext.getText().toString().trim().isEmpty()) {
            showToast("Please enter your phone number", 0);
            return false;
        } else if (!validatePhoneNumber(binding.phoneEdittext.getText().toString())) {
            showToast("Please enter a valid phone number", 0);
            return false;
        } else {
            return true;
        }
    }

    private Boolean validatePhoneNumber(String phoneNumber) {
        String regex = "[0-9]{10,13}";
        Pattern phonePattern = Pattern.compile(regex);
        Matcher phoneMatcher = phonePattern.matcher(phoneNumber);
        return phoneMatcher.matches();
    }

    public void showToast(String message, int length) {
        Utils.showToast(getApplicationContext(), message, length);
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
