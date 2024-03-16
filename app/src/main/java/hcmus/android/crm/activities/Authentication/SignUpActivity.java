package hcmus.android.crm.activities.Authentication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import hcmus.android.crm.databinding.ActivitySignUpBinding;
import hcmus.android.crm.utilities.Constants;
import hcmus.android.crm.utilities.HashHelper;
import hcmus.android.crm.utilities.PreferenceManager;

public class SignUpActivity extends AppCompatActivity {
    private ActivitySignUpBinding binding;
    private PreferenceManager preferenceManager;

    private FirebaseAuth auth;
    // private String encodedImage;

    private static final String TAG = "SignUpActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
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
       /* binding.layoutImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });*/
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
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

                    // Put user data into firestore
                    HashMap<String, Object> user = new HashMap<>();
                    user.put(Constants.KEY_NAME, binding.inputName.getText().toString());
                    user.put(Constants.KEY_EMAIL, binding.inputEmail.getText().toString());
                    user.put(Constants.KEY_PHONE_NUMBER, binding.inputPhoneNumber.getText().toString());
                    user.put(Constants.KEY_PASSWORD, hashedPassword);
                    user.put(Constants.KEY_IMAGE, null);

                    db.collection(Constants.KEY_COLLECTION_USERS).add(user).addOnSuccessListener(docRef -> {
                        // Send verification email
                        assert firebaseUser != null;
                        firebaseUser.sendEmailVerification();
                        showToast("User registered successfully. Please verify your email address.");

                        Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }).addOnFailureListener(ex -> {
                        loading(false);
                        showToast("User registered failed. Please try again.");
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
                        showToast("User already registered with this email. Use another email.");
                    } catch (Exception e) {
                        Log.e(TAG, Objects.requireNonNull(e.getMessage()));
                        showToast(e.getMessage());
                    }
                }
            }
        }).addOnFailureListener(ex -> {
            loading(false);
            showToast(ex.getMessage());
        });
    }

    private String encodeImage(Bitmap bitmap) {
        int previewWidth = 150;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

   /* private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    if (result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        try {
                            assert imageUri != null;
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                            binding.imageProfile.setImageBitmap(bitmap);
                            binding.textAddImage.setVisibility(View.GONE);
                            // encodedImage = encodeImage(bitmap);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
    );*/

    private Boolean isValidSignUpDetails() {
        /*if (encodedImage == null) {
            showToast("Select profile image");
            return false;*/
        //} else
        if (binding.inputName.getText().toString().trim().isEmpty()) {
            showToast("Please enter your name");
            return false;
        } else if (binding.inputEmail.getText().toString().trim().isEmpty()) {
            showToast("Please enter your email");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches()) {
            showToast("Enter valid email");
            return false;
        } else if (binding.inputPhoneNumber.getText().toString().trim().isEmpty()) {
            showToast("Please enter your phone number");
            return false;
        } else if(!validatePhoneNumber(binding.inputPhoneNumber.getText().toString())) {
            showToast("Please enter a valid phone number");
            return false;
        } else if (binding.inputPassword.getText().toString().trim().isEmpty()) {
            showToast("Please enter your password");
            return false;
        } else if (binding.inputConfirmPassword.getText().toString().trim().isEmpty()) {
            showToast("Please confirm your password");
            return false;
        } else if (!binding.inputPassword.getText().toString().equals(binding.inputConfirmPassword.getText().toString())) {
            showToast("Password & confirm password does not match!");
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