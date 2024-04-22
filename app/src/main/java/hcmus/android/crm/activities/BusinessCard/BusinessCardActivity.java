package hcmus.android.crm.activities.BusinessCard;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.Serializable;

import hcmus.android.crm.R;
import hcmus.android.crm.activities.DrawerBaseActivity;
import hcmus.android.crm.databinding.ActivityBusinessCardBinding;

import hcmus.android.crm.models.BusinessCard;
import hcmus.android.crm.utilities.Constants;

public class BusinessCardActivity extends DrawerBaseActivity {
    private ActivityBusinessCardBinding binding;
    private FirebaseFirestore db;
    private String businessCardId;
    private TextView textDeleteCard;
    private BusinessCard businessCard;
    private TextView textEditCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        setTitle("Your Business Card");

        binding = ActivityBusinessCardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCardViewClicked(v);
            }
        });

        checkIfBusinessCardExists();
    }

    @Override
    protected void onResume() {
        super.onResume();
        navigationView.setCheckedItem(R.id.nav_business_card);
        checkIfBusinessCardExists();
    }

    public void onCardViewClicked(View view) {
        Intent intent = new Intent(BusinessCardActivity.this, AddNewBusinessCardActivity.class);
        startActivity(intent);
    }

    private void checkIfBusinessCardExists() {
        db.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID))
                .collection(Constants.KEY_COLLECTION_CARDS)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult() != null && !task.getResult().isEmpty()) {
                                DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                                businessCardId = documentSnapshot.getId();
                                businessCard = documentSnapshot.toObject(BusinessCard.class);
                                if (businessCard != null) {
                                    updateCardView(businessCard);
                                }
                            } else {
                                // No business card found
                                showCreateCardView();
                            }
                        } else {
                            // Error occurred while checking for business card
                            Log.d("Checking for business card", "Error occurred while checking for business card");
                        }
                    }
                });
    }

    // Method to decode base64 string to Bitmap
    private Bitmap decodeBase64(String input) {
        byte[] decodedBytes = Base64.decode(input, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }

    private void updateCardView(BusinessCard businessCard) {
        // Hide unnecessary views
        binding.noCardContainer.setVisibility(View.GONE);

        // Show business card with information
        binding.textViewFullname.setText(businessCard.getFullname());
        binding.textViewCompany.setText(businessCard.getCompany());
        binding.textViewJobTitle.setText(businessCard.getJobTitle());
        binding.textViewEmail.setText(businessCard.getEmail());
        binding.textViewPhone.setText(businessCard.getPhone());
        binding.cardContainer.setVisibility(View.VISIBLE);

        // Load and display QR code if it exists
        String qrCodeString = businessCard.getQrcode();
        if (qrCodeString != null && !qrCodeString.isEmpty()) {
            Bitmap qrCodeBitmap = decodeBase64(qrCodeString);
            if (qrCodeBitmap != null) {
                binding.qrCodeImageView.setImageBitmap(qrCodeBitmap);
                binding.qrCodeImageView.setVisibility(View.VISIBLE);
            } else {
                binding.qrCodeImageView.setVisibility(View.GONE);
            }
        } else {
            binding.qrCodeImageView.setVisibility(View.GONE);
        }

        // Find the TextView by id
        textDeleteCard = binding.textDeleteCard;
        textDeleteCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Call the method to show delete confirmation dialog
                showDeleteConfirmationDialog();
            }
        });
        textEditCard = binding.textEditCard;
        textEditCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BusinessCardActivity.this, EditBusinessCardActivity.class);

                intent.putExtra("businessCardId", businessCardId);
                intent.putExtra("businessCard", businessCard);

                startActivity(intent);
            }
        });
    }

    private void showCreateCardView() {
        // Show views for creating a new card
        binding.noCardContainer.setVisibility(View.VISIBLE);
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Card");
        builder.setMessage("Are you sure you want to delete this business card?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteCard();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    private void deleteCard() {
        db.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID))
                .collection(Constants.KEY_COLLECTION_CARDS)
                .document(businessCardId).delete()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        showToast("Business card deleted successfully!", Toast.LENGTH_SHORT);
                        // Call the method to update the UI
                        updateUI();
                    } else {
                        showToast("Error deleting business card!", Toast.LENGTH_SHORT);
                    }
                });
        showToast("Business card deleted successfully!", 0);
    }

    private void updateUI() {
        // Hide the business card container
        binding.cardContainer.setVisibility(View.GONE);

        // Clear all card information
        binding.textViewFullname.setText("");
        binding.textViewCompany.setText("");
        binding.textViewJobTitle.setText("");
        binding.textViewEmail.setText("");
        binding.textViewPhone.setText("");

        // Hide QR code image view
        binding.qrCodeImageView.setVisibility(View.GONE);

        // Show the no-card container
        binding.noCardContainer.setVisibility(View.VISIBLE);
    }
}
