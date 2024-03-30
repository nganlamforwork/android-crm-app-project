package hcmus.android.crm.activities.Authentication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;

import hcmus.android.crm.activities.Settings.Information;
import hcmus.android.crm.databinding.ActivityChangePasswordBinding;
import hcmus.android.crm.utilities.Constants;
import hcmus.android.crm.utilities.HashHelper;
import hcmus.android.crm.utilities.PreferenceManager;
import hcmus.android.crm.utilities.Utils;

public class ChangePasswordActivity extends AppCompatActivity {
    private ActivityChangePasswordBinding binding;

    private FirebaseAuth auth;

    private static final String TAG = "ChangePasswordActivity";
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Change Password");

        binding = ActivityChangePasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferenceManager = (PreferenceManager) getIntent().getSerializableExtra("preferenceManager");

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

    private void showToast(String message, int length) {
        Utils.showToast(getApplicationContext(), message, length);
    }

    private void changePassword() {
        loading(true);
        String hashedPassword = HashHelper.hashPassword(binding.inputPassword.getText().toString());
        assert hashedPassword != null;

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        auth.signInWithEmailAndPassword(preferenceManager.getString(Constants.KEY_EMAIL), hashedPassword).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String newPassword = binding.inputPassword.getText().toString();
                    user.updatePassword(newPassword).addOnCompleteListener(updateTask -> {
                        if (updateTask.isSuccessful()) {
                            loading(false);
                            showToast("Password changed successfully.", Toast.LENGTH_SHORT);
                            logout();
                        } else {
                            loading(false);
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
