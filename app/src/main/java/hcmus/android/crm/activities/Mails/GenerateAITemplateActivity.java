package hcmus.android.crm.activities.Mails;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import hcmus.android.crm.R;
import hcmus.android.crm.activities.DrawerBaseActivity;
import hcmus.android.crm.databinding.ActivityAddNewTemplateBinding;
import hcmus.android.crm.databinding.ActivityGenerateAitemplateBinding;
import hcmus.android.crm.utilities.Constants;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.VolleyError;

public class GenerateAITemplateActivity extends DrawerBaseActivity {
    private ActivityGenerateAitemplateBinding binding;
    private Button generateButton;
    private EditText writeAbout;
    private AutoCompleteTextView tonesDropdown, lengthsDropdown;
    private String output = "";

    // Define arrays for options
    private String[] toneOptions = {"Professional", "Casual", "Informational", "Funny", "Enthusiastic"};
    private String[] lengthOptions = {"Short", "Medium", "Long"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle("Generate Template With AI");

        binding = ActivityGenerateAitemplateBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = binding.appBar.toolbar;
        setSupportActionBar(toolbar);

        getElementsById();
        // Set up options for tone and length
        setUpOptions();

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setTitle("Generate Template With AI");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }
        setListeners();
    }

    private void getElementsById() {
        // Basic fields
        writeAbout = binding.writeAbout;
        tonesDropdown = binding.tonesDropdown;
        lengthsDropdown = binding.lengthsDropdown;
        // Create Button
        generateButton = binding.generateButton;
    }

    private void setUpOptions() {
        // Set adapter for tone options
        ArrayAdapter<String> toneAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, toneOptions);
        tonesDropdown.setAdapter(toneAdapter);

        // Set adapter for length options
        ArrayAdapter<String> lengthAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, lengthOptions);
        lengthsDropdown.setAdapter(lengthAdapter);
    }

    private void setListeners() {
        generateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generateTemplate();
            }
        });
    }


    private void generateTemplate() {
        String writeAbout = this.writeAbout.getText().toString();
        String selectedTone = tonesDropdown.getText().toString();
        String selectedLength = lengthsDropdown.getText().toString();

        // Xử lý gen AI ở đây rồi gắn dô 2 cái dưới
        String requirement = "Write me an email about: " + writeAbout + ", with tone is: " +
                selectedTone + ", and the length of the email is: " + selectedLength;

        chatGPTModel(requirement);
    }

    private void chatGPTModel(String stringInput) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("model", "gpt-3.5-turbo");

            JSONArray jsonArrayMessage = new JSONArray();
            // System message
            JSONObject jsonObjectSystemMessage = new JSONObject();
            jsonObjectSystemMessage.put("role", "system");
            jsonObjectSystemMessage.put("content", Constants.EMAIL_PROMPT);
            jsonArrayMessage.put(jsonObjectSystemMessage);

            // User message
            JSONObject jsonObjectUserMessage = new JSONObject();
            jsonObjectUserMessage.put("role", "user");

            jsonObjectUserMessage.put("content", stringInput);
            jsonArrayMessage.put(jsonObjectUserMessage);
            Log.d("MESSAGE", jsonArrayMessage.toString());
            jsonObject.put("messages", jsonArrayMessage);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                Constants.END_POINT, jsonObject, new Response.Listener<JSONObject>() {
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
                Log.d("RESPONSE", output);

                String generatedBody = output;

                Intent resultIntent = new Intent();
                resultIntent.putExtra("generatedBody", generatedBody);
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> mapHeader = new HashMap<>();
                mapHeader.put("Authorization", "Bearer " + Constants.OPENAI_API_KEY);
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
}