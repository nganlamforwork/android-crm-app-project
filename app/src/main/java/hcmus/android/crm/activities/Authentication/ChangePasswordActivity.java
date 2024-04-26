package hcmus.android.crm.activities.Authentication;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import hcmus.android.crm.activities.DrawerBaseActivity;
import hcmus.android.crm.databinding.ActivityChangePasswordBinding;
import hcmus.android.crm.utilities.Constants;
import hcmus.android.crm.utilities.HashHelper;
import hcmus.android.crm.utilities.PreferenceManager;
import hcmus.android.crm.utilities.Utils;

public class ChangePasswordActivity extends DrawerBaseActivity {
    private ActivityChangePasswordBinding binding;
    private FirebaseAuth auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Change Password");

        binding = ActivityChangePasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);

        // Enable the back button in the action bar or toolbar
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setTitle("Change Password");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        auth = FirebaseAuth.getInstance();
        setListeners();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void setListeners() {
        binding.buttonChangePassword.setOnClickListener(v -> {
            if (isValidChangePasswordDetails()) {
                changePassword();
            }
        });
    }

    public void showToast(String message, int length) {
        Utils.showToast(getApplicationContext(), message, length);
    }

    private void changePassword() {
        loading(true);
        String newPassword = binding.inputPassword.getText().toString();
        String oldPassword = binding.inputOldPassword.getText().toString();
        String email = preferenceManager.getString(Constants.KEY_EMAIL); // Retrieve email

        if (email == null || oldPassword == null) {
            showToast("Email not found in preferences.", Toast.LENGTH_SHORT);
            loading(false);
            return;
        }

        String hashedOldPassword = HashHelper.hashPassword(oldPassword);
        String hashedNewPassword = HashHelper.hashPassword(newPassword);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            showToast("User not authenticated.", Toast.LENGTH_SHORT);
            loading(false);
            return;
        }

        AuthCredential credential = EmailAuthProvider.getCredential(email, hashedOldPassword); // Use retrieved email

        user.reauthenticate(credential).addOnCompleteListener(reauthTask -> {
            if (reauthTask.isSuccessful()) {
                user.updatePassword(hashedNewPassword).addOnCompleteListener(updateTask -> {
                    loading(false);
                    if (updateTask.isSuccessful()) {
                        showToast("Password changed successfully.", Toast.LENGTH_SHORT);
                        logout();
                    } else {
                        Log.e(TAG, "Error updating password: " + updateTask.getException().getMessage());
                        showToast("Failed to update password. Please try again later.", Toast.LENGTH_SHORT);
                    }
                });
            } else {
                loading(false);
                showToast("Authentication failed. Please enter your correct old password.", Toast.LENGTH_SHORT);
            }
        });
    }

    private Boolean isValidChangePasswordDetails() {
        if (binding.inputPassword.getText().toString().trim().isEmpty()) {
            showToast("Please enter your new password", 0);
            return false;
        } else if (binding.inputConfirmPassword.getText().toString().trim().isEmpty()) {
            showToast("Please confirm your new password", 0);
            return false;
        } else if (!binding.inputPassword.getText().toString().equals(binding.inputConfirmPassword.getText().toString())) {
            showToast("New password & confirm password does not match!", 0);
            return false;
        } else {
            return true;
        }
    }

    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.buttonChangePassword.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.buttonChangePassword.setVisibility(View.VISIBLE);
        }
    }

    private void logout() {
        showToast("See you later!", 0);
        preferenceManager.clear();
        auth.signOut();
        startActivity(new Intent(getApplicationContext(), SignInActivity.class));
        finish();
    }
}
