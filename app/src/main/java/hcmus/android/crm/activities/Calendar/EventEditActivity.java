package hcmus.android.crm.activities.Calendar;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import hcmus.android.crm.activities.DrawerBaseActivity;
import hcmus.android.crm.databinding.ActivityEventEditBinding;
import hcmus.android.crm.models.Event;
import hcmus.android.crm.models.Lead;
import hcmus.android.crm.utilities.Constants;

public class EventEditActivity extends DrawerBaseActivity {
    private ActivityEventEditBinding binding;
    private EditText eventName, eventLocation, eventDescription, eventDate, eventTime;
    private FirebaseFirestore db;
    private Event newEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Add new event");

        binding = ActivityEventEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        db = FirebaseFirestore.getInstance();

        Toolbar toolbar = binding.appBar.toolbar;
        setSupportActionBar(toolbar);

        // Enable the back button in the action bar or toolbar
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        eventName = binding.eventName;
        eventDescription = binding.eventDescription;
        eventLocation = binding.eventLocation;
        eventDate = binding.eventDate;
        eventTime = binding.eventTime;

        setListeners();
    }

    private void setListeners() {
        eventDate.setOnClickListener(v -> {
            showDatePicker();
        });
        eventTime.setOnClickListener(v -> {
            showTimePicker();
        });
        binding.buttonSaveEvent.setOnClickListener(v -> {
            loading(true);
            handleSaveEvent();
        });
    }

    private void handleSaveEvent() {
        String name = eventName.getText().toString().trim();
        String description = eventDescription.getText().toString().trim();
        String location = eventLocation.getText().toString().trim();
        String date = eventDate.getText().toString().trim();
        String time = eventTime.getText().toString().trim();

        if(!isFieldsFilled()) {
            showToast("All field is required", 0);
            eventName.setError("Event name is required");
            eventName.requestFocus();
            return;
        }
        newEvent = new Event(name, description, location, date, time);

        db.collection(Constants.KEY_COLLECTION_EVENTS)
                .add(newEvent)
                .addOnSuccessListener(documentReference -> {
                    // Reset fields
                    resetFields();

                    // Hide loading state
                    loading(false);
                    showToast("New event added successful", 0);

                    finish();
                })
                .addOnFailureListener(e -> {
                    // Handle failure, e.g., show error message
                    loading(false);
                    showToast("Failed to add new event", 0);
                });
    }
    private void resetFields() {
        eventName.setText("");
        eventDescription.setText("");
        eventLocation.setText("");
        eventDate.setText("");
        eventTime.setText("");
    }
    private boolean isFieldsFilled() {
        // Check if all required fields are filled
        return !eventName.getText().toString().trim().isEmpty() &&
                !eventDescription.getText().toString().trim().isEmpty() &&
                !eventLocation.getText().toString().trim().isEmpty() &&
                !eventDate.getText().toString().trim().isEmpty() &&
                !eventTime.getText().toString().trim().isEmpty();
    }
    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }

    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.buttonSaveEvent.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.buttonSaveEvent.setVisibility(View.VISIBLE);
        }
    }
    public void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int day) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(year, month, day);
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                        String selectedDate = dateFormat.format(calendar.getTime());

                        eventDate.setText(selectedDate);
                    }
                }, year, month, dayOfMonth);

        datePickerDialog.show();
    }

    public void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        // Determine AM or PM
                        String am_pm = (hourOfDay < 12) ? "AM" : "PM";

                        // Convert 24-hour format to 12-hour format
                        int hour = (hourOfDay == 0 || hourOfDay == 12) ? 12 : hourOfDay % 12;

                        // Format minute to have leading zero if necessary
                        String minuteStr = (minute < 10) ? "0" + minute : String.valueOf(minute);

                        // Set the selected time in the EditText
                        String selectedTime = hour + ":" + minuteStr + " " + am_pm;
                        eventTime.setText(selectedTime);
                    }
                }, hourOfDay, minute, false);

        timePickerDialog.show();
    }
}
