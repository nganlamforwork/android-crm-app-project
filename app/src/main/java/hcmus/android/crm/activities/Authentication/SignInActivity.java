package hcmus.android.crm.activities.Authentication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

import hcmus.android.crm.activities.Main.MainActivity;
import hcmus.android.crm.databinding.ActivitySignInBinding;
import hcmus.android.crm.utilities.Constants;
import hcmus.android.crm.utilities.HashHelper;
import hcmus.android.crm.utilities.PreferenceManager;
import hcmus.android.crm.utilities.Utils;

public class SignInActivity extends AppCompatActivity {

    private ActivitySignInBinding binding;
    private PreferenceManager preferenceManager;
    private FirebaseAuth auth;
    private static final String TAG = "SignInActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        auth = FirebaseAuth.getInstance();
        // Persistent logged in
        if (preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN)) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
        setListeners();
    }

    private void setListeners() {
        binding.textCreateNewAccount.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), SignUpActivity.class)));
        binding.buttonSignIn.setOnClickListener(v -> {
            if (isValidCredentials()) {
                signIn();
            }
        });
    }

    private void showToast(String message, int length) {
        Utils.showToast(getApplicationContext(), message, length);
    }


    private void signIn() {
        loading(true);
        String hashedPassword = HashHelper.hashPassword(binding.inputPassword.getText().toString());

        assert hashedPassword != null;
        auth.signInWithEmailAndPassword(binding.inputEmail.getText().toString(), hashedPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    loading(false);

                    // Get instance of current user
                    FirebaseUser firebaseUser = auth.getCurrentUser();

                    assert firebaseUser != null;

                    if (firebaseUser.isEmailVerified()) {
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        db.collection(Constants.KEY_COLLECTION_USERS)
                                .whereEqualTo(Constants.KEY_EMAIL, firebaseUser.getEmail())
                                .get()
                                .addOnCompleteListener(userTask -> {
                                    if (userTask.isSuccessful() && userTask.getResult() != null && userTask.getResult().getDocuments().size() > 0) {
                                        DocumentSnapshot docSnap = userTask.getResult().getDocuments().get(0);
                                        preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                                        preferenceManager.putString(Constants.KEY_USER_ID, firebaseUser.getUid());
                                        preferenceManager.putString(Constants.KEY_NAME, docSnap.getString(Constants.KEY_NAME));
                                        preferenceManager.putString(Constants.KEY_EMAIL, docSnap.getString(Constants.KEY_EMAIL));
                                        preferenceManager.putString(Constants.KEY_IMAGE, docSnap.getString(Constants.KEY_IMAGE));
                                        preferenceManager.putString(Constants.KEY_PHONE_NUMBER, docSnap.getString(Constants.KEY_PHONE_NUMBER));
                                        showToast("Login successfully", 0);
                                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                    }
                                });
                    } else {
                        auth.signOut();
                        showAlertDialog(firebaseUser, "You must verify your email to login");
                    }
                } else {
                    loading(false);
                    try {
                        throw Objects.requireNonNull(task.getException());
                    } catch (FirebaseAuthInvalidUserException e) {
                        showToast("User not found!", 0);
                    } catch (FirebaseAuthInvalidCredentialsException e) {
                        showToast("Invalid credentials!", 0);
                    } catch (Exception e) {
                        Log.e(TAG, Objects.requireNonNull(e.getMessage()));
                        showToast(e.getMessage(), 0);
                    }
                }
            }
        });
    }

    private void showAlertDialog(FirebaseUser firebaseUser, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(SignInActivity.this);
        builder.setTitle("Email not verified");
        builder.setMessage(message);
        builder.setPositiveButton("Send verification email", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                firebaseUser.sendEmailVerification();
                showToast("We have sent another verification to your email!", 0);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss(); // Dismiss the dialog
            }
        });
        AlertDialog alertDialog = builder.create();

        alertDialog.show();
    }

    private Boolean isValidCredentials() {
        if (binding.inputEmail.getText().toString().trim().isEmpty()) {
            showToast("Please enter your email", 0);
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches()) {
            showToast("Please enter valid email", 0);
            return false;
        } else if (binding.inputPassword.getText().toString().trim().isEmpty()) {
            showToast("Please enter your password", 0);
            return false;
        } else {
            return true;
        }
    }

    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.buttonSignIn.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.buttonSignIn.setVisibility(View.VISIBLE);
        }
    }
}