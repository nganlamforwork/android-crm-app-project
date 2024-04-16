package hcmus.android.crm.activities.Main;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import java.util.ArrayList;

import hcmus.android.crm.R;
import hcmus.android.crm.activities.Calendar.WeekViewActivity;
import hcmus.android.crm.activities.DrawerBaseActivity;
import hcmus.android.crm.activities.Main.adapters.Calendar.CalendarAdapter;
import hcmus.android.crm.activities.Search.SearchActivity;
import hcmus.android.crm.databinding.ActivityMainBinding;
import hcmus.android.crm.services.EventSchedulerService;
import hcmus.android.crm.utilities.CalendarUtils;
import hcmus.android.crm.utilities.Constants;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import java.time.LocalDate;
import static hcmus.android.crm.utilities.CalendarUtils.daysInMonthArray;
import static hcmus.android.crm.utilities.CalendarUtils.monthYearFromDate;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivity extends DrawerBaseActivity implements CalendarAdapter.OnItemListener {
    private ActivityMainBinding binding;
    private TextView monthYearText;
    private RecyclerView calendarRecyclerView;
    private CalendarAdapter calendarAdapter;

    private int lastClickedPosition = -1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        calendarRecyclerView = binding.calendarRecyclerView;
        monthYearText = binding.monthYearTV;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CalendarUtils.selectedDate = LocalDate.now();
        }

        setMonthView();
        getFCMToken();
        scheduleEventUpdate();
    }
    private void scheduleEventUpdate() {
        JobScheduler jobScheduler = getSystemService(JobScheduler.class);
        ComponentName componentName = new ComponentName(this, EventSchedulerService.class);

        JobInfo.Builder info = new JobInfo.Builder(EventSchedulerService.JOB_ID, componentName);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            info.setRequiresBatteryNotLow(true);
        }
        info.setRequiredNetworkType(JobInfo.NETWORK_TYPE_NONE);
        info.setPeriodic(60 * 60 * 1000);

        if(jobScheduler != null) {
            int result = jobScheduler.schedule(info.build());
            if(result == JobScheduler.RESULT_SUCCESS) {
                showToast("Event is updated", 0);
            } else {
                showToast("An error occurred and failed to start event jobs scheduler", 0);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search, menu);

        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reset calendar to the current date when returning to the home screen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CalendarUtils.selectedDate = LocalDate.now();
        }
        setMonthView();
    }

    private void setMonthView() {
        monthYearText.setText(monthYearFromDate(CalendarUtils.selectedDate));
        ArrayList<LocalDate> daysInMonth = daysInMonthArray(CalendarUtils.selectedDate);

        calendarAdapter = new CalendarAdapter(daysInMonth, this);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), 7);
        calendarRecyclerView.setLayoutManager(layoutManager);
        calendarRecyclerView.setAdapter(calendarAdapter);
    }

    public void previousMonthAction(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CalendarUtils.selectedDate = CalendarUtils.selectedDate.minusMonths(1);
        }
        setMonthView();
    }

    public void nextMonthAction(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CalendarUtils.selectedDate = CalendarUtils.selectedDate.plusMonths(1);
        }
        setMonthView();
    }

    @Override
    public void onItemClick(int position, LocalDate date) {
        if (date != null) {
            CalendarUtils.selectedDate = date;
            setMonthView();
        }
    }

    public void weeklyAction(View view) {
        startActivity(new Intent(this, WeekViewActivity.class));
    }

    void getFCMToken() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String token = task.getResult();
                FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_USERS)
                        .document(preferenceManager.getString(Constants.KEY_USER_ID))
                        .update(Constants.KEY_FCM_TOKEN, token);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_search) {
            Intent intent = new Intent(this, SearchActivity.class);
            startActivity(intent);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
