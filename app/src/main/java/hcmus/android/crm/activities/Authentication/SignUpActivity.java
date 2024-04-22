package hcmus.android.crm.activities.Authentication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import hcmus.android.crm.databinding.ActivitySignUpBinding;
import hcmus.android.crm.utilities.Constants;
import hcmus.android.crm.utilities.HashHelper;
import hcmus.android.crm.utilities.Utils;

public class SignUpActivity extends AppCompatActivity {
    private ActivitySignUpBinding binding;

    private FirebaseAuth auth;

    private static final String TAG = "SignUpActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        auth = FirebaseAuth.getInstance();
        setListeners();
    }

    private void setListeners() {
        binding.textSignIn.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        binding.buttonSignUp.setOnClickListener(v -> {
            if (isValidSignUpDetails()) {
                signUp();
            }
        });
    }

    private void showToast(String message, int length) {
        Utils.showToast(getApplicationContext(), message, length);
    }

    private void signUp() {
        loading(true);
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String hashedPassword = HashHelper.hashPassword(binding.inputPassword.getText().toString());
        assert hashedPassword != null;
        auth.createUserWithEmailAndPassword(binding.inputEmail.getText().toString(), hashedPassword).addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    loading(false);
                    FirebaseUser firebaseUser = auth.getCurrentUser();
                    String userId = firebaseUser.getUid(); // Get the user ID

                    // Put user data into firestore
                    HashMap<String, Object> user = new HashMap<>();
                    user.put(Constants.KEY_USER_ID, userId);
                    user.put(Constants.KEY_NAME, binding.inputName.getText().toString());
                    user.put(Constants.KEY_EMAIL, binding.inputEmail.getText().toString());
                    user.put(Constants.KEY_PHONE_NUMBER, binding.inputPhoneNumber.getText().toString());
                    user.put(Constants.KEY_PASSWORD, hashedPassword);
                    user.put(Constants.KEY_IMAGE, null);

                    db.collection(Constants.KEY_COLLECTION_USERS).document(userId).set(user).addOnSuccessListener(docRef -> {
                        // Send verification email
                        assert firebaseUser != null;
                        firebaseUser.sendEmailVerification();
                        showToast("User registered successfully. Please verify your email address.", 0);

                        Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }).addOnFailureListener(ex -> {
                        loading(false);
                        showToast("User registered failed. Please try again.", 0);
                    });
                } else {
                    loading(false);
                    try {
                        throw Objects.requireNonNull(task.getException());
                    } catch (FirebaseAuthWeakPasswordException e) {
                        binding.inputPassword.setError("Your password is too weak. Use a mix of alphabets, numbers and special characters.");
                        binding.inputPassword.requestFocus();
                    } catch (FirebaseAuthInvalidCredentialsException e) {
                        binding.inputEmail.setError(e.getMessage());
                        binding.inputEmail.requestFocus();
                    } catch (FirebaseAuthUserCollisionException e) {
                        showToast("User already registered with this email. Use another email.", 0);
                    } catch (Exception e) {
                        Log.e(TAG, Objects.requireNonNull(e.getMessage()));
                        showToast(e.getMessage(), 0);
                    }
                }
            }
        }).addOnFailureListener(ex -> {
            loading(false);
            showToast(ex.getMessage(), 0);
        });
    }


    private Boolean isValidSignUpDetails() {
        if (binding.inputName.getText().toString().trim().isEmpty()) {
            showToast("Please enter your name", 0);
            return false;
        } else if (binding.inputEmail.getText().toString().trim().isEmpty()) {
            showToast("Please enter your email", 0);
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches()) {
            showToast("Enter valid email", 0);
            return false;
        } else if (binding.inputPhoneNumber.getText().toString().trim().isEmpty()) {
            showToast("Please enter your phone number", 0);
            return false;
        } else if (!validatePhoneNumber(binding.inputPhoneNumber.getText().toString())) {
            showToast("Please enter a valid phone number", 0);
            return false;
        } else if (binding.inputPassword.getText().toString().trim().isEmpty()) {
            showToast("Please enter your password", 0);
            return false;
        } else if (binding.inputConfirmPassword.getText().toString().trim().isEmpty()) {
            showToast("Please confirm your password", 0);
            return false;
        } else if (!binding.inputPassword.getText().toString().equals(binding.inputConfirmPassword.getText().toString())) {
            showToast("Password & confirm password does not match!", 0);
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

    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.buttonSignUp.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.buttonSignUp.setVisibility(View.VISIBLE);
        }
    }
}