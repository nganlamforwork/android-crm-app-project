package hcmus.android.crm.activities.Leads;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import hcmus.android.crm.R;
import hcmus.android.crm.activities.DrawerBaseActivity;
import hcmus.android.crm.activities.Leads.adapters.LeadAdapter;
import hcmus.android.crm.activities.Search.SearchActivity;
import hcmus.android.crm.databinding.ActivityLeadBinding;
import hcmus.android.crm.models.Lead;
import hcmus.android.crm.utilities.Constants;
import hcmus.android.crm.utilities.TouchHelper;

public class LeadActivity extends DrawerBaseActivity {

    private static final int REQUEST_CODE_FILE_PICKER = 101;
    private static final int REQUEST_CODE_STORAGE_PERMISSION = 102;
    private ActivityLeadBinding binding;
    private RecyclerView recyclerView;
    private FirebaseFirestore db;
    private LeadAdapter leadAdapter;
    private Animation rotateOpen;
    private Animation rotateClose;
    private Animation fromBottom;
    private Animation toBottom;
    private boolean clicked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Leads");

        binding = ActivityLeadBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        db = FirebaseFirestore.getInstance();

        rotateOpen = AnimationUtils.loadAnimation(this, R.anim.rotate_open_anim);
        rotateClose = AnimationUtils.loadAnimation(this, R.anim.rotate_close_anim);
        fromBottom = AnimationUtils.loadAnimation(this, R.anim.from_bottom_anim);
        toBottom = AnimationUtils.loadAnimation(this, R.anim.to_bottom_anim);

        setupLeadRecyclerView();

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new TouchHelper(leadAdapter));
        itemTouchHelper.attachToRecyclerView(recyclerView);

        setListeners();
    }

    private void setupLeadRecyclerView() {
        recyclerView = binding.leadRecyclerView;
        Query query = db.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID))
                .collection(Constants.KEY_COLLECTION_LEADS)
                .orderBy("createdAt", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<Lead> options = new FirestoreRecyclerOptions.Builder<Lead>().setQuery(query, Lead.class).build();

        checkIfListEmpty(query);

        leadAdapter = new LeadAdapter(options, this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(leadAdapter);
        recyclerView.addItemDecoration(
                new DividerItemDecoration(
                        recyclerView.getContext(),
                        LinearLayoutManager.VERTICAL
                )
        );
        leadAdapter.startListening();
    }

    private void checkIfListEmpty(Query query) {
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().getDocuments().size() > 0) {
                        recyclerView.setVisibility(View.VISIBLE);
                        binding.emptyView.setVisibility(View.GONE);
                    } else {
                        recyclerView.setVisibility(View.GONE);
                        binding.emptyView.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        navigationView.setCheckedItem(R.id.nav_leads);
        if (leadAdapter != null) {
            leadAdapter.startListening();
            leadAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (leadAdapter != null) leadAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (leadAdapter != null) leadAdapter.stopListening();
    }

    private void exportLeadsToCsv() {
        if (!isExternalStorageWritable()) {
            Toast.makeText(this, "Cannot write CSV file. Please check storage access permission.", Toast.LENGTH_SHORT).show();
            return;
        }
        File csvFile = createCsvFile();
        if (csvFile == null) {
            Toast.makeText(this, "Error creating CSV file.", Toast.LENGTH_SHORT).show();
            return;
        }
        exportToCsv(csvFile);
        String message = "Successfully exported Lead list to CSV file. Path: " + csvFile.getAbsolutePath();
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_FILE_PICKER && resultCode == RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
                if (uri != null) {
                    String filePath = getPathFromUri(uri);
                    if (filePath != null) {
                        File file = new File(filePath);
                        importLeadsFromCsv(file);
                    } else {
                        Toast.makeText(this, "File path is null.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    private String getPathFromUri(Uri uri) {
        if (uri == null) return null;
        String filePath = null;

        if (DocumentsContract.isDocumentUri(this, uri)) {
            String documentId = DocumentsContract.getDocumentId(uri);
            if (documentId.startsWith("raw:")) {
                filePath = documentId.replaceFirst("raw:", "");
            } else {
                String[] split = documentId.split(":");
                if (split.length > 1) {
                    String type = split[0];
                    String path = split[1];

                    if ("primary".equalsIgnoreCase(type)) {
                        filePath = Environment.getExternalStorageDirectory() + "/" + path;
                    }
                }
            }
        } else {
            String[] projection = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                filePath = cursor.getString(columnIndex);
                cursor.close();
            }
        }

        return filePath;
    }

    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    private File createCsvFile() {
        long timestamp = System.currentTimeMillis();
        Date currentDate = new Date(timestamp);

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss", Locale.getDefault());
        String formattedDate = sdf.format(currentDate);

        File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        String fileName = "crm_lead_" + formattedDate + ".csv";

        return new File(directory, fileName);
    }

    private void setListeners() {
        binding.manualButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LeadActivity.this, AddNewLeadActivity.class));
            }
        });
        binding.scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LeadActivity.this, ScanBusinessCardActivity.class));
            }
        });
        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onAddButtonClicked();
            }
        });
    }

    private void openFilePicker() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_STORAGE_PERMISSION);
        } else {
            // Launch the file picker if permission is granted
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*"); // Use "*/*" for all file types
            startActivityForResult(intent, REQUEST_CODE_FILE_PICKER);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, open file picker
                openFilePicker();
                Toast.makeText(this, "Open file picker.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Storage permission is required to import CSV files.", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void exportToCsv(File file) {
        try (FileWriter writer = new FileWriter(file)) {
            writer.append("Name,Phone,Email,Address,Notes,Job,Company,Latitude,Longitude\n");
            for (int i = 0; i < leadAdapter.getItemCount(); i++) {
                Lead lead = leadAdapter.getItem(i);
                String line = "\"" + lead.getName() + "\",\"" + lead.getPhone() + "\",\"" + lead.getEmail() + "\",\"" + lead.getAddress() + "\",\"" + lead.getNotes() + "\",\"" + lead.getJob() + "\",\"" + lead.getCompany() + "\",\"" + lead.getLatitude() + "\",\"" + lead.getLongitude() + "\"\n";
                writer.append(line);
            }

            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void importLeadsFromCsv(File file) {
        if (file != null && file.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                CSVReader reader = new CSVReader(br);
                String[] line;
                line = reader.readNext();
                while ((line = reader.readNext()) != null) {
                    Lead lead = getLead(line);
                    addLeadToFirestore(lead);
                }
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error adding lead: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            } catch (CsvValidationException e) {
                throw new RuntimeException(e);
            }
        } else {
            Toast.makeText(this, "Selected file does not exist.", Toast.LENGTH_SHORT).show();
        }
    }

    @NonNull
    private static Lead getLead(String[] line) {
        String name = line[0];
        String phone = line[1];
        String email = line[2];
        String address = line[3];
        String notes = line[4];
        String job = line[5];
        String company = line[6];
        String latitude = line[7];
        String longitude = line[8];

        // Create Lead object
        Lead lead = new Lead(name, email, phone, address, job, company, notes, "", latitude, longitude);
        return lead;
    }


    private void addLeadToFirestore(Lead lead) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID))
                .collection(Constants.KEY_COLLECTION_LEADS)
                .add(lead)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Lead added.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error adding lead", Toast.LENGTH_SHORT).show();
                });
    }

    private void onAddButtonClicked() {
        setVisibility(clicked);
        setAnimation(clicked);
        clicked = !clicked;
    }

    private void setVisibility(boolean clicked) {
        if (!clicked) {
            binding.manualButton.setVisibility(View.VISIBLE);
            binding.scanButton.setVisibility(View.VISIBLE);
        } else {
            binding.manualButton.setVisibility(View.INVISIBLE);
            binding.scanButton.setVisibility(View.INVISIBLE);
        }

    }

    private void setAnimation(boolean clicked) {
        if (!clicked){
            binding.manualButton.startAnimation(fromBottom);
            binding.scanButton.startAnimation(fromBottom);
            binding.fab.startAnimation(rotateOpen);
        }else{
            binding.manualButton.startAnimation(toBottom);
            binding.scanButton.startAnimation(toBottom);
            binding.fab.startAnimation(rotateClose);

        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_w_options, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_search) {
            Intent intent = new Intent(this, SearchActivity.class);
            startActivity(intent);

            return true;
        }

        if (id == R.id.action_overflow) {
            showOverflowMenuDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showOverflowMenuDialog() {
        PopupMenu popupMenu = new PopupMenu(this, findViewById(R.id.action_overflow));
        popupMenu.getMenuInflater().inflate(R.menu.overflow_menu, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.option1) {
                    openFilePicker();
                    return true;
                } else if (menuItem.getItemId() == R.id.option2) {
                    exportLeadsToCsv();
                    return true;
                } else {
                    return false;
                }

            }
        });

        popupMenu.show();
    }
}
