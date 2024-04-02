package hcmus.android.crm.activities.Main;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;


import java.util.ArrayList;

import hcmus.android.crm.activities.Calendar.WeekViewActivity;
import hcmus.android.crm.activities.DrawerBaseActivity;
import hcmus.android.crm.activities.Main.adapters.Calendar.CalendarAdapter;
import hcmus.android.crm.databinding.ActivityMainBinding;
import hcmus.android.crm.utilities.CalendarUtils;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.widget.TextView;

import java.time.LocalDate;
import static hcmus.android.crm.utilities.CalendarUtils.daysInMonthArray;
import static hcmus.android.crm.utilities.CalendarUtils.monthYearFromDate;

public class MainActivity extends DrawerBaseActivity implements CalendarAdapter.OnItemListener {
    private ActivityMainBinding binding;
    private TextView monthYearText;
    private RecyclerView calendarRecyclerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        calendarRecyclerView = binding.calendarRecyclerView;
        monthYearText = binding.monthYearTV;
        CalendarUtils.selectedDate = LocalDate.now();

        setMonthView();
    }


    private void setMonthView()
    {
        monthYearText.setText(monthYearFromDate(CalendarUtils.selectedDate));
        ArrayList<LocalDate> daysInMonth = daysInMonthArray(CalendarUtils.selectedDate);

        CalendarAdapter calendarAdapter = new CalendarAdapter(daysInMonth, this);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), 7);
        calendarRecyclerView.setLayoutManager(layoutManager);
        calendarRecyclerView.setAdapter(calendarAdapter);
    }
    public void previousMonthAction(View view)
    {
        CalendarUtils.selectedDate = CalendarUtils.selectedDate.minusMonths(1);
        setMonthView();
    }

    public void nextMonthAction(View view)
    {
        CalendarUtils.selectedDate = CalendarUtils.selectedDate.plusMonths(1);
        setMonthView();
    }

    @Override
    public void onItemClick(int position, LocalDate date)
    {
        if(date != null)
        {
            CalendarUtils.selectedDate = date;
            setMonthView();
        }
    }
    public void weeklyAction(View view)
    {
        startActivity(new Intent(this, WeekViewActivity.class));
    }
}
