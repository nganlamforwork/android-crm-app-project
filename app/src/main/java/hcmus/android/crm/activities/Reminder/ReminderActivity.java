package hcmus.android.crm.activities.Reminder;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import hcmus.android.crm.activities.DrawerBaseActivity;
import hcmus.android.crm.activities.Reminder.adapter.ReminderAdapter;
import hcmus.android.crm.databinding.ActivityReminderBinding;
import hcmus.android.crm.models.Reminder;
import hcmus.android.crm.utilities.Constants;

public class ReminderActivity extends DrawerBaseActivity {
    ActivityReminderBinding binding;
    RecyclerView reminderRecycler;
    TextView addReminder;
    List<Reminder> reminderList = new ArrayList<>();
    private FirebaseFirestore db;
    private ReminderAdapter reminderAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityReminderBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setTitle("Reminder");

        db = FirebaseFirestore.getInstance();

        reminderRecycler = binding.reminderRecycler;
        addReminder = binding.addReminder;


        setupReminderRecyclerView();

        setListener();
    }

    private void setupReminderRecyclerView() {
        Query query = db.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID))
                .collection(Constants.KEY_COLLECTION_REMINDERS)
                .orderBy("date")
                .orderBy("timeAlarm");
        Log.d("TESTTT", String.valueOf(query));


        checkIfListEmpty(query);

        FirestoreRecyclerOptions<Reminder> options = new FirestoreRecyclerOptions.Builder<Reminder>()
                .setQuery(query, Reminder.class).build();

        reminderAdapter = new ReminderAdapter(options, this);
        reminderRecycler.setHasFixedSize(true);
        reminderRecycler.setLayoutManager(new LinearLayoutManager(this));
        reminderRecycler.setAdapter(reminderAdapter);


        reminderAdapter.startListening();
    }

    private void checkIfListEmpty(Query query) {
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    Log.d("TESTTT", String.valueOf(task.getResult().getDocuments().size()));
                    if (task.getResult().getDocuments().size() > 0) {
                        reminderRecycler.setVisibility(View.VISIBLE);
                        binding.emptyView.setVisibility(View.GONE);
                    } else {
                        reminderRecycler.setVisibility(View.GONE);
                        binding.emptyView.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
    }

    private void setListener() {
        addReminder.setOnClickListener(view -> {
            startActivity(new Intent(ReminderActivity.this, AddNewReminderActivity.class));
        });
    }
}