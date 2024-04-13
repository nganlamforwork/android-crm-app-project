package hcmus.android.crm.activities.Leads;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import hcmus.android.crm.R;
import hcmus.android.crm.activities.DrawerBaseActivity;
import hcmus.android.crm.activities.Leads.adapters.LeadAdapter;
import hcmus.android.crm.databinding.ActivityLeadBinding;
import hcmus.android.crm.models.Lead;
import hcmus.android.crm.utilities.Constants;
import hcmus.android.crm.utilities.TouchHelper;

public class LeadActivity extends DrawerBaseActivity {

    private ActivityLeadBinding binding;
    private RecyclerView recyclerView;
    private FirebaseFirestore db;
    private LeadAdapter leadAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Leads");

        binding = ActivityLeadBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        db = FirebaseFirestore.getInstance();

        recyclerView = binding.leadRecyclerView;
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        setupLeadRecyclerView();

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new TouchHelper(leadAdapter));
        itemTouchHelper.attachToRecyclerView(recyclerView);

        setListeners();
    }

    private void setupLeadRecyclerView() {
        Query query = db.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID))
                .collection(Constants.KEY_COLLECTION_LEADS)
                .orderBy("createdAt", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<Lead> options = new FirestoreRecyclerOptions.Builder<Lead>()
                .setQuery(query, Lead.class).build();

        checkIfListEmpty(query);

        leadAdapter = new LeadAdapter(options, this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(leadAdapter);
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
        if (leadAdapter != null)  {
            leadAdapter.startListening();
            leadAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (leadAdapter != null)
            leadAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (leadAdapter != null)
            leadAdapter.stopListening();
    }

    private void setListeners() {
        binding.btnExportToCSV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                exportLeadsToCsv();
            }
        });
        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LeadActivity.this, AddNewLeadActivity.class));
            }
        });
    }

    private void exportLeadsToCsv() {
        // Kiểm tra xem có quyền ghi vào bộ nhớ ngoài không
        if (!isExternalStorageWritable()) {
            Toast.makeText(this, "Không thể ghi tệp CSV. Vui lòng kiểm tra quyền truy cập bộ nhớ.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo tệp CSV
        File csvFile = createCsvFile();

        if (csvFile == null) {
            Toast.makeText(this, "Lỗi khi tạo tệp CSV.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Xuất dữ liệu
        exportToCsv(csvFile);
        Toast.makeText(this, "Xuất danh sách Lead ra file CSV thành công.", Toast.LENGTH_SHORT).show();
    }

    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    private File createCsvFile() {
        File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        String fileName = "leads1.csv";
        return new File(directory, fileName);
    }

    private void exportToCsv(File file) {
        try (FileWriter writer = new FileWriter(file)) {
            // Write header
            writer.append("Name,Phone,Email,Address,Notes\n");

            // Write data
            for (int i = 0; i < leadAdapter.getItemCount(); i++) {
                Lead lead = leadAdapter.getItem(i);
                // Enclose each field in double quotes to handle commas within the data
                String line = "\"" + lead.getName() + "\",\"" + lead.getPhone() + "\",\"" + lead.getEmail() + "\",\"" + lead.getAddress() + "\",\"" + lead.getNotes() + "\"\n";
                writer.append(line);
            }

            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_search) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
