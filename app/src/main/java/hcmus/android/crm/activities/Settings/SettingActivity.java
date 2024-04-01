package hcmus.android.crm.activities.Settings;

import static hcmus.android.crm.utilities.Utils.encodeImage;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.io.FileNotFoundException;
import java.io.InputStream;

import hcmus.android.crm.R;
import hcmus.android.crm.activities.DrawerBaseActivity;
import hcmus.android.crm.databinding.ActivitySettingBinding;
import hcmus.android.crm.utilities.Constants;
import hcmus.android.crm.utilities.PreferenceManager;
import hcmus.android.crm.utilities.Utils;

public class SettingActivity extends DrawerBaseActivity {
    private ActivitySettingBinding binding;
    ListView settingsOptions;
    // The n-th row in the list will consist of [icon, label] where icon = thumbnail[n] and label=items[n]
    String[] options = {"Information", "Devices", "Notifications", "Appearance", "Language", "Privacy & Security", "Storage"};
    private String encodedImage;
    private PreferenceManager preferenceManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Settings");

        binding = ActivitySettingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferenceManager = new PreferenceManager(getApplicationContext());

        loadCredentials();
        setListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        navigationView.setCheckedItem(R.id.nav_personal);
    }

    private void loadCredentials() {
        binding.displayName.setText(preferenceManager.getString(Constants.KEY_NAME));
        binding.textUserEmail.setText(preferenceManager.getString(Constants.KEY_EMAIL));

        byte[] bytes = Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE), Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        binding.avatar.setImageBitmap(bitmap);
    }

    private void setListeners() {
        // the arguments of the custom adapter are: activityContex, layout-to-be-inflated, labels, icons
        SettingsListViewAdapter adapter = new SettingsListViewAdapter(this, R.layout.settings_list_view_item, options);

        // bind intrinsic ListView to custom adapter
        settingsOptions = findViewById(R.id.settingsOptions);
        settingsOptions.setAdapter(adapter);
        settingsOptions.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Update selected item position
                adapter.setSelectedItemPosition(position);

                // Notify the adapter that the data set has changed
                adapter.notifyDataSetChanged();

                // Handle click event for "Information" item
                if (position == 0) {
                    // Create an Intent to navigate to SettingInformationActivity
                    Intent intent = new Intent(SettingActivity.this, Information.class);
                    startActivity(intent);
                }
            }
        });
        binding.layoutImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });
    }
    private void showToast(String message, int length) {
        Utils.showToast(getApplicationContext(), message, length);
    }
    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    if (result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        try {
                            assert imageUri != null;
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                            binding.avatar.setImageBitmap(bitmap);
                            encodedImage = encodeImage(bitmap);
                            FirebaseFirestore db = FirebaseFirestore.getInstance();

                            String userId = preferenceManager.getString(Constants.KEY_USER_ID);
                            db.collection(Constants.KEY_COLLECTION_USERS)
                                    .document(userId)
                                    .update(Constants.KEY_IMAGE, encodedImage)
                                    .addOnSuccessListener(aVoid -> {
                                        showToast("Profile image updated successfully", 0);
                                    })
                                    .addOnFailureListener(e -> {
                                        showToast("Failed to update profile image", 0);
                                    });
                            preferenceManager.putString(Constants.KEY_IMAGE, encodedImage);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
    );

}