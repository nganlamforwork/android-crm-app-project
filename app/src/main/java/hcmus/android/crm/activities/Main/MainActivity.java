package hcmus.android.crm.activities.Main;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import java.util.ArrayList;

import hcmus.android.crm.BuildConfig;
import hcmus.android.crm.R;
import hcmus.android.crm.activities.Calendar.WeekViewActivity;
import hcmus.android.crm.activities.DrawerBaseActivity;
import hcmus.android.crm.activities.Main.adapters.Calendar.CalendarAdapter;
import hcmus.android.crm.databinding.ActivityMainBinding;
import hcmus.android.crm.services.EventSchedulerService;
import hcmus.android.crm.utilities.CalendarUtils;
import hcmus.android.crm.utilities.Constants;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import static hcmus.android.crm.utilities.CalendarUtils.daysInMonthArray;
import static hcmus.android.crm.utilities.CalendarUtils.monthYearFromDate;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import static android.Manifest.permission.RECORD_AUDIO;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.VolleyError;

import java.util.HashMap;


public class MainActivity extends DrawerBaseActivity implements CalendarAdapter.OnItemListener {
    private ActivityMainBinding binding;
    private TextView monthYearText;
    private RecyclerView calendarRecyclerView;
    private CalendarAdapter calendarAdapter;

    private final String OPENAI_API_KEY = BuildConfig.OPENAI_API_KEY;
    private final String END_POINT = "https://api.openai.com/v1/chat/completions";

    private String output = "";
    private TextToSpeech textToSpeech;

    private SpeechRecognizer speechRecognizer;
    private Intent intent;
    private StringBuilder CONTEXT_DATA;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CONTEXT_DATA = new StringBuilder();
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        calendarRecyclerView = binding.calendarRecyclerView;
        monthYearText = binding.monthYearTV;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CalendarUtils.selectedDate = LocalDate.now();
        }
        ActivityCompat.requestPermissions(this,
                new String[]{RECORD_AUDIO},
                PackageManager.PERMISSION_GRANTED);
        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                textToSpeech.setSpeechRate((float) 0.8);
            }
        });
        intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {
            }

            @Override
            public void onBeginningOfSpeech() {
            }

            @Override
            public void onRmsChanged(float v) {
            }

            @Override
            public void onBufferReceived(byte[] bytes) {
            }

            @Override
            public void onEndOfSpeech() {
            }

            @Override
            public void onError(int i) {
            }

            @Override
            public void onResults(Bundle bundle) {
                ArrayList<String> matches = bundle.getStringArrayList(speechRecognizer.RESULTS_RECOGNITION);
                String string = "";
                if (matches != null) {
                    string = matches.get(0);
                    chatGPTModel(string);
                }
            }

            @Override
            public void onPartialResults(Bundle bundle) {
            }

            @Override
            public void onEvent(int i, Bundle bundle) {
            }
        });
        setMonthView();
        getFCMToken();
        scheduleEventUpdate();
    }

    private void fetchData() {
        CONTEXT_DATA.setLength(0);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = preferenceManager.getString(Constants.KEY_USER_ID);
        // Get current date
        Calendar calendar = Calendar.getInstance();
        Date currentDate = calendar.getTime();

        fetchEvents(db, userId, currentDate);
        fetchLeads(db, userId);
        fetchNotes(db, userId);
    }

    private void scheduleEventUpdate() {
        JobScheduler jobScheduler = getSystemService(JobScheduler.class);
        ComponentName componentName = new ComponentName(this, EventSchedulerService.class);

        JobInfo.Builder info = new JobInfo.Builder(EventSchedulerService.JOB_ID, componentName);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            info.setRequiresBatteryNotLow(true);
        }
        info.setRequiredNetworkType(JobInfo.NETWORK_TYPE_NONE);
        info.setPeriodic(60 * 60 * 1000); // Every 1 hour

        if (jobScheduler != null) {
            int result = jobScheduler.schedule(info.build());
            if (result == JobScheduler.RESULT_SUCCESS) {
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
        fetchData();
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

    public void buttonAssist(View view) {
        if (textToSpeech.isSpeaking()) {
            textToSpeech.stop();
            return;
        }
        output = "";
        speechRecognizer.startListening(intent);
    }

    private void chatGPTModel(String stringInput) {
        textToSpeech.speak("In Progress", TextToSpeech.QUEUE_FLUSH, null, null);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("model", "gpt-3.5-turbo");

            JSONArray jsonArrayMessage = new JSONArray();
            // System message
            JSONObject jsonObjectSystemMessage = new JSONObject();
            jsonObjectSystemMessage.put("role", "system");
            jsonObjectSystemMessage.put("content", "You are a helpful AI assistant, your name is CRM G10, you are responsible for helping users with their problems. " +
                    "You are giving full information context of user's leads, notes, upcoming events and their opportunities. " +
                    "Try your best to assist user and do not hallucinate any information. Only get it from the provided context. " +
                    "If no information found, reply with `INFORMATION NOT FOUND`");
            jsonArrayMessage.put(jsonObjectSystemMessage);

            // User message
            JSONObject jsonObjectUserMessage = new JSONObject();
            jsonObjectUserMessage.put("role", "user");

            String message = CONTEXT_DATA + stringInput;
            jsonObjectUserMessage.put("content", message);
            jsonArrayMessage.put(jsonObjectUserMessage);
            Log.d("MESSAGE", jsonArrayMessage.toString());
            jsonObject.put("messages", jsonArrayMessage);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                END_POINT, jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                String stringText = null;
                try {
                    stringText = response.getJSONArray("choices")
                            .getJSONObject(0)
                            .getJSONObject("message")
                            .getString("content");
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                output = output + stringText;
                textToSpeech.speak(output, TextToSpeech.QUEUE_FLUSH, null, null);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> mapHeader = new HashMap<>();
                mapHeader.put("Authorization", "Bearer " + OPENAI_API_KEY);
                mapHeader.put("Content-Type", "application/json");
                mapHeader.put("User-Agent", "Mozilla/5.0");

                return mapHeader;
            }

            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                return super.parseNetworkResponse(response);
            }
        };


        int intTimeoutPeriod = 60000; // 60 seconds timeout duration defined
        RetryPolicy retryPolicy = new DefaultRetryPolicy(intTimeoutPeriod,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjectRequest.setRetryPolicy(retryPolicy);
        Volley.newRequestQueue(getApplicationContext()).add(jsonObjectRequest);
    }

    private void fetchEvents(FirebaseFirestore db, String userId, Date currentDate) {
        CONTEXT_DATA.append("## EVENTS INFORMATION:");
        db.collection(Constants.KEY_COLLECTION_USERS)
                .document(userId)
                .collection(Constants.KEY_COLLECTION_EVENTS)
                .whereGreaterThanOrEqualTo("createdAt", currentDate)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Map<String, Object> data = document.getData();
                                data.remove("createdAt");
                                CONTEXT_DATA.append(data.toString());
                            }
                        }
                    }
                });
    }


    private void fetchLeads(FirebaseFirestore db, String userId) {
        CONTEXT_DATA.append("## LEADS INFORMATION:");
        db.collection(Constants.KEY_COLLECTION_USERS)
                .document(userId)
                .collection(Constants.KEY_COLLECTION_LEADS)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Map<String, Object> data = document.getData();
                                data.remove("image");
                                data.remove("createdAt");
                                data.remove("latitude");
                                data.remove("longitude");
                                CONTEXT_DATA.append(data.toString());
                            }
                        }
                    }
                });
    }

    private void fetchNotes(FirebaseFirestore db, String userId) {
        CONTEXT_DATA.append("## NOTES OF LEADS INFORMATION:");
        db.collection(Constants.KEY_COLLECTION_USERS)
                .document(userId)
                .collection(Constants.KEY_COLLECTION_NOTES)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Map<String, Object> data = document.getData();
                                data.remove("createdAt");
                                CONTEXT_DATA.append(data.toString());
                            }
                        }
                    }
                });
    }
}