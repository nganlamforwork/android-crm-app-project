package hcmus.android.crm.activities.Calendar;

import static hcmus.android.crm.utilities.CalendarUtils.daysInWeekArray;
import static hcmus.android.crm.utilities.CalendarUtils.monthYearFromDate;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import hcmus.android.crm.activities.Calendar.adapters.EventAdapter;
import hcmus.android.crm.activities.DrawerBaseActivity;
import hcmus.android.crm.activities.Main.adapters.Calendar.CalendarAdapter;
import hcmus.android.crm.databinding.ActivityWeekViewBinding;
import hcmus.android.crm.models.Event;
import hcmus.android.crm.models.Lead;
import hcmus.android.crm.utilities.CalendarUtils;
import hcmus.android.crm.utilities.Constants;

public class WeekViewActivity extends DrawerBaseActivity implements CalendarAdapter.OnItemListener {
    private ActivityWeekViewBinding binding;
    private FirebaseFirestore db;
    private TextView monthYearText;
    private RecyclerView calendarRecyclerView;
    private ListView eventListView;

    private Query query;
    private ListenerRegistration listenerRegistration;
    private List<Event> dailyEvents;
    private EventAdapter eventAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Weekly Events");

        binding = ActivityWeekViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        db = FirebaseFirestore.getInstance();

        calendarRecyclerView = binding.calendarRecyclerView;
        monthYearText = binding.monthYearTV;
        eventListView = binding.eventListView;
        eventListView.setEmptyView(binding.empty);

        Toolbar toolbar = binding.appBar.toolbar;
        setSupportActionBar(toolbar);

        // Enable the back button in the action bar or toolbar
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }
        dailyEvents = new ArrayList<>();
        eventAdapter = new EventAdapter(this, dailyEvents, db, preferenceManager);
        eventListView.setAdapter(eventAdapter);

        setWeekView();
        setListeners();
    }

    private void setListeners() {
        eventListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Event event = (Event) parent.getItemAtPosition(position);
                Intent intent = new Intent(view.getContext(), EventEditActivity.class);

                intent.putExtra("eventId", event.EventId);
                intent.putExtra("selectedDate", String.valueOf(CalendarUtils.selectedDate));
                intent.putExtra("name", event.getName());
                intent.putExtra("description", event.getDescription());
                intent.putExtra("time", event.getTime());
                intent.putExtra("location", event.getLocation());

                startActivity(intent);
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
        showData();
    }

    public void previousWeekAction(View view) {
        CalendarUtils.selectedDate = CalendarUtils.selectedDate.minusWeeks(1);
        setWeekView();
    }

    public void nextWeekAction(View view) {
        CalendarUtils.selectedDate = CalendarUtils.selectedDate.plusWeeks(1);
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
        showData();
    }

    private void showData() {
        query = db.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID))
                .collection(Constants.KEY_COLLECTION_EVENTS)
                .whereEqualTo("date", CalendarUtils.selectedDate.toString())
                .orderBy("createdAt", Query.Direction.DESCENDING);
        listenerRegistration = query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.e("Firestore", "Error fetching events: " + error.getMessage());
                    return;
                }
                if (value == null || value.isEmpty()) {
                    Log.d("Firestore", "No events found.");
                    eventAdapter.clear();
                    eventAdapter.notifyDataSetChanged();
                    return;
                }
                // Clear the eventAdapter before adding new events
                eventAdapter.clear();
                for (DocumentChange documentChange : value.getDocumentChanges()) {
                    String id = documentChange.getDocument().getId();
                    Event event = documentChange.getDocument().toObject(Event.class).withId(id);
                    Log.d("EVENT", event.getName());
                    eventAdapter.add(event);
                }
                eventAdapter.notifyDataSetChanged();
                listenerRegistration.remove();
            }
        });
    }

    public void newEventAction(View view) {
        Intent intent = new Intent(this, EventEditActivity.class);

        intent.putExtra("selectedDate", String.valueOf(CalendarUtils.selectedDate));

        startActivity(intent);
    }
}
