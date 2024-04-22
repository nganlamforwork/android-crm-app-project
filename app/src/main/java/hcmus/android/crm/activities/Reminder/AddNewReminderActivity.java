package hcmus.android.crm.activities.Reminder;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TimePicker;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import hcmus.android.crm.activities.DrawerBaseActivity;
import hcmus.android.crm.activities.Reminder.Receiver.ReminderAlarmReceiver;
import hcmus.android.crm.databinding.ActivityAddNewReminderBinding;
import hcmus.android.crm.models.Reminder;
import hcmus.android.crm.utilities.Constants;

public class AddNewReminderActivity extends DrawerBaseActivity {
    private ActivityAddNewReminderBinding binding;
    private Reminder newReminder;
    private EditText reminderTitle, reminderDescription, reminderDate, reminderTime;
    private Button newReminderSaveButton;
    private boolean isEditMode = false;
    private String reminderId;
    private ProgressBar progressBar;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Add new reminder");

        binding = ActivityAddNewReminderBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        createNotificationChannel();

        Toolbar toolbar = binding.appBar.toolbar;
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        newReminderSaveButton = binding.addReminderBtn;
        reminderTitle = binding.addReminderTitle;
        reminderDescription = binding.addReminderDescription;
        reminderDate = binding.reminderDate;
        reminderTime = binding.reminderTime;
        progressBar = binding.progressBar;

        if (getIntent().hasExtra("reminderId")) {
            isEditMode = true;
            setTitle("Edit reminder");
            populateReminderData();
        }

        setListeners();
    }

    private void populateReminderData() {
        reminderTitle.setText(getIntent().getStringExtra("title"));
        reminderDescription.setText(getIntent().getStringExtra("description"));
        reminderDate.setText(getIntent().getStringExtra("selectedDate"));
        reminderTime.setText(getIntent().getStringExtra("time"));
    }

    private void setListeners() {
        reminderDate.setOnClickListener(v -> {
            showDatePicker();
        });
        reminderTime.setOnClickListener(v -> {
            showTimePicker();
        });

        reminderDescription.addTextChangedListener(new AddNewReminderActivity.FieldTextWatcher());
        reminderDate.addTextChangedListener(new AddNewReminderActivity.FieldTextWatcher());
        reminderTime.addTextChangedListener(new AddNewReminderActivity.FieldTextWatcher());
        reminderTitle.addTextChangedListener(new AddNewReminderActivity.FieldTextWatcher());

        if (isEditMode) {
            newReminderSaveButton.setText("Update Reminder");
            newReminderSaveButton.setOnClickListener(v -> {
                loading(true);
                handleUpdateReminder();
            });
        } else {
            newReminderSaveButton.setText("Save Reminder");
            newReminderSaveButton.setOnClickListener(v -> {
                loading(true);
                handleAddNewReminder();
            });
        }

        newReminderSaveButton.setEnabled(false); // Initially disable the button
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
                        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                        String selectedDate = dateFormat.format(calendar.getTime());

                        reminderDate.setText(selectedDate);
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
                        reminderTime.setText(selectedTime);
                    }
                }, hourOfDay, minute, false);

        timePickerDialog.show();
    }

    private void loading(Boolean isLoading) {
        if (isLoading) {
            newReminderSaveButton.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.INVISIBLE);
            newReminderSaveButton.setVisibility(View.VISIBLE);
        }
    }

    private boolean isFieldsFilled() {
        // Check if all required fields are filled
        return !reminderTitle.getText().toString().trim().isEmpty() &&
                !reminderDescription.getText().toString().trim().isEmpty() &&
                !reminderTime.getText().toString().trim().isEmpty() &&
                !reminderDate.getText().toString().trim().isEmpty();
    }

    private void handleUpdateReminder() {
        // Get input values
        String title = reminderTitle.getText().toString().trim();
        String description = reminderDescription.getText().toString().trim();
        String date = reminderDate.getText().toString().trim();
        String time = reminderTime.getText().toString().trim();

        if (!isFieldsFilled()) {
            loading(false);
            showToast("All field is required", 0);
            return;
        }

        Reminder updatedReminder = new Reminder();
        updatedReminder.setReminderTitle(title);
        updatedReminder.setReminderDescrption(description);
        updatedReminder.setDate(date);
        updatedReminder.setTimeAlarm(time);


        db.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID))
                .collection(Constants.KEY_COLLECTION_REMINDERS)
                .document(reminderId)
                .set(updatedReminder)
                .addOnSuccessListener(documentReference -> {
                    // Reset fields
                    resetFields();

                    // Hide loading state
                    loading(false);
                    showToast("Reminder updated successful", 0);

                    // Send back the new reminder data to ReminderDetailActivity
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("updatedReminder", updatedReminder);
                    setResult(Activity.RESULT_OK, resultIntent);

                    finish();
                })
                .addOnFailureListener(e -> {
                    // Handle failure, e.g., show error message
                    loading(false);
                    showToast("Failed to update reminder", 0);
                });
    }

    private void handleAddNewReminder() {
        // Get input values
        String title = reminderTitle.getText().toString().trim();
        String description = reminderDescription.getText().toString().trim();
        String date = reminderDate.getText().toString().trim();
        String time = reminderTime.getText().toString().trim();

        if (!isFieldsFilled()) {
            loading(false);
            showToast("All field is required", 0);
            return;
        }

        newReminder = new Reminder();
        newReminder.setReminderTitle(title);
        newReminder.setReminderDescrption(description);
        newReminder.setDate(date);
        newReminder.setTimeAlarm(time);


        db.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID))
                .collection(Constants.KEY_COLLECTION_REMINDERS)
                .add(newReminder)
                .addOnSuccessListener(documentReference -> {
                    // Reset fields
                    resetFields();

                    // Hide loading state
                    loading(false);
                    showToast("New reminder added successful", 0);
                    Intent intent = new Intent(this, ReminderActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    setUpAlarm(title, description, date, time);
                })
                .addOnFailureListener(e -> {
                    // Handle failure, e.g., show error message
                    loading(false);
                    showToast("Failed to add new reminder", 0);
                });
    }

    private void setUpAlarm(String title, String description, String date, String time) {
        // Convert date and time to milliseconds
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm a");
        try {
            calendar.setTime(dateFormat.parse(date + " " + time));
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(this, ReminderAlarmReceiver.class);
        intent.putExtra("title", title);
        intent.putExtra("description", description);
        // Add more extras if needed

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Set alarm
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }


    private void resetFields() {
        // Reset input fields
        reminderTitle.setText("");
        reminderDescription.setText("");
        reminderTime.setText("");
        reminderDate.setText("");

        // Disable save button
        newReminderSaveButton.setEnabled(false);
    }

    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }

    private class FieldTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            // Enable/disable the button based on field content
            newReminderSaveButton.setEnabled(isFieldsFilled());
        }
    }

    private void createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "ReminderChannel";
            String description = "Channel For Alarm Manager";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("crm", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);

        }


    }
}
