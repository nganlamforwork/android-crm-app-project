package hcmus.android.crm.activities.Calendar;

import static hcmus.android.crm.utilities.CalendarUtils.daysInWeekArray;
import static hcmus.android.crm.utilities.CalendarUtils.monthYearFromDate;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.time.LocalDate;
import java.util.ArrayList;
import hcmus.android.crm.activities.Calendar.adapters.EventAdapter;
import hcmus.android.crm.activities.DrawerBaseActivity;
import hcmus.android.crm.activities.Main.adapters.Calendar.CalendarAdapter;
import hcmus.android.crm.databinding.ActivityWeekViewBinding;
import hcmus.android.crm.models.Event;
import hcmus.android.crm.utilities.CalendarUtils;
import hcmus.android.crm.utilities.Constants;

public class WeekViewActivity extends DrawerBaseActivity implements CalendarAdapter.OnItemListener {
    private ActivityWeekViewBinding binding;
    private FirebaseFirestore db;
    private TextView monthYearText;
    private RecyclerView calendarRecyclerView;
    private EventAdapter eventAdapter;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Weekly Events");

        binding = ActivityWeekViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        db = FirebaseFirestore.getInstance();

        calendarRecyclerView = binding.calendarRecyclerView;
        monthYearText = binding.monthYearTV;

        Toolbar toolbar = binding.appBar.toolbar;
        setSupportActionBar(toolbar);

        // Enable the back button in the action bar or toolbar
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        setWeekView();
    }

    private void setupEventRecyclerview() {
        recyclerView = binding.eventRecyclerView;
        Query query = db.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID))
                .collection(Constants.KEY_COLLECTION_EVENTS)
                .whereEqualTo("date", CalendarUtils.selectedDate.toString())
                .orderBy("createdAt", Query.Direction.DESCENDING);

        checkIfListEmpty(query);

        FirestoreRecyclerOptions<Event> options = new FirestoreRecyclerOptions.Builder<Event>()
                .setQuery(query, Event.class).build();

        eventAdapter = new EventAdapter(options, this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(eventAdapter);


        eventAdapter.startListening();
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

    private void setWeekView() {
        monthYearText.setText(monthYearFromDate(CalendarUtils.selectedDate));
        ArrayList<LocalDate> days = daysInWeekArray(CalendarUtils.selectedDate);

        CalendarAdapter calendarAdapter = new CalendarAdapter(days, this);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, 7);
        calendarRecyclerView.setLayoutManager(layoutManager);
        calendarRecyclerView.setAdapter(calendarAdapter);
        setupEventRecyclerview();
    }

    public void previousWeekAction(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CalendarUtils.selectedDate = CalendarUtils.selectedDate.minusWeeks(1);
        }
        setWeekView();
    }

    public void nextWeekAction(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CalendarUtils.selectedDate = CalendarUtils.selectedDate.plusWeeks(1);
        }
        setWeekView();
    }

    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }

    @Override
    public void onItemClick(int position, LocalDate date) {
        CalendarUtils.selectedDate = date;
        setWeekView();
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (eventAdapter != null) {
            eventAdapter.startListening();
            eventAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (eventAdapter != null)
            eventAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (eventAdapter != null)
            eventAdapter.stopListening();
    }

    public void newEventAction(View view) {
        Intent intent = new Intent(this, EventEditActivity.class);
        intent.putExtra("selectedDate", String.valueOf(CalendarUtils.selectedDate));

        startActivity(intent);
    }
}
