package hcmus.android.crm;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

import hcmus.android.crm.activities.SignInActivity;
import hcmus.android.crm.activities.SignUpActivity;
import hcmus.android.crm.databinding.ActivityMainBinding;
import hcmus.android.crm.utilities.Constants;
import hcmus.android.crm.utilities.PreferenceManager;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle drawerToggle;
    private Toolbar toolbar;
    private TextView userName, userEmail;

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private ActivityMainBinding binding;
    private PreferenceManager preferenceManager;
    private FirebaseAuth auth;

    BarChart barChart;
    TextView chartLabel;
    TextView chartDateLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        auth = FirebaseAuth.getInstance();

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        loadCredentials();
        drawerToggle = new
                ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        ListView listView = findViewById(R.id.customListView);
        String[] customerList = {"Francis Holzworth", "Kaylyn Yokel", "Kimberly Muro", "Jack Sause", "Rebekkah Lafantano"};
        String[] customerListId = {"00220", "00221", "00222", "00223", "00224"};
        int[] customerImgs = {R.drawable.ava1, R.drawable.ava2, R.drawable.ava3, R.drawable.ava4, R.drawable.ava5};

        CustomBaseAdapter customBaseAdapter = new CustomBaseAdapter(getApplicationContext(), customerList, customerListId, customerImgs);
        listView.setAdapter(customBaseAdapter);

        // Initialize the bar chart
        barChart = findViewById(R.id.barChart);
        chartLabel = findViewById(R.id.chartLabel);
        chartDateLabel = findViewById(R.id.chartDateLabel);

        // Data for the bar chart
        ArrayList<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0f, new float[]{50f, 30f}));
        entries.add(new BarEntry(1f, new float[]{60f, 40f}));
        entries.add(new BarEntry(2f, new float[]{70f, 50f}));
        entries.add(new BarEntry(3f, new float[]{80f, 60f}));
        entries.add(new BarEntry(4f, new float[]{90f, 70f}));
        entries.add(new BarEntry(5f, new float[]{100f, 80f}));
        entries.add(new BarEntry(6f, new float[]{110f, 90f}));

        // Labels for the bars
        ArrayList<String> labels = new ArrayList<>();
        labels.add("Mon");
        labels.add("Tue");
        labels.add("Wed");
        labels.add("Thu");
        labels.add("Fri");
        labels.add("Sat");
        labels.add("Sun");

        BarDataSet dataSet = new BarDataSet(entries, "Legend");
        dataSet.setColors(new int[]{Color.parseColor("#8E97A3"), Color.parseColor("#1C2E46")});
        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.4f); // Set the width of the bars

        barChart.setData(barData);
        barChart.setFitBars(true); // Make the x-axis fit exactly all bars
        barChart.invalidate(); // Refresh the chart

        // Customize the legend
        Legend legend = barChart.getLegend();
        legend.setForm(Legend.LegendForm.CIRCLE);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
        legend.setWordWrapEnabled(true);

        // Update chart labels
        chartLabel.setText("Week in Review");
        chartDateLabel.setText("Apr 10th - Apr 17th");
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Handle back press logic here
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                }
            }
        });
    }


    private void loadCredentials() {
        View headerView = navigationView.getHeaderView(0);
        userName = headerView.findViewById(R.id.textUserName);
        userEmail = headerView.findViewById(R.id.textUserEmail);

        userName.setText(preferenceManager.getString(Constants.KEY_NAME));
        userEmail.setText(preferenceManager.getString(Constants.KEY_EMAIL));
        binding.textName.setText("Welcome back, " + preferenceManager.getString(Constants.KEY_NAME));
     /*   byte[] bytes = Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE), Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        binding.imageProfile.setImageBitmap(bitmap);*/
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void signOut() {
        showToast("Signing out...");
        preferenceManager.clear();
        auth.signOut();
        startActivity(new Intent(getApplicationContext(), SignInActivity.class));
        finish();
        setContentView(R.layout.activity_main);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);


        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setLogo(R.mipmap.ic_launcher);
            getSupportActionBar().setDisplayUseLogoEnabled(true);
        }
        navigationView.setNavigationItemSelectedListener(this);
        showToast("This is a message");
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        showToast("This is a message");

        int id = item.getItemId();

        if (id == R.id.nav_home) {
            showToast("This is a message");
            Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_leads) {
            Intent intent = new Intent(MainActivity.this, SignInActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_contact) {
            Intent intent = new Intent(MainActivity.this, SignInActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_organization) {
            Intent intent = new Intent(MainActivity.this, SignInActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_opportunity) {
            Intent intent = new Intent(MainActivity.this, SignInActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_quotations) {
            Intent intent = new Intent(MainActivity.this, SignInActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_sales) {
            Intent intent = new Intent(MainActivity.this, SignInActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_territory) {
            Intent intent = new Intent(MainActivity.this, SignInActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_communications) {
            Intent intent = new Intent(MainActivity.this, SignInActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_sales_forecast) {
            Intent intent = new Intent(MainActivity.this, SignInActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_reports) {
            Intent intent = new Intent(MainActivity.this, SignInActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_logout) {
            signOut();
        }

//        drawerLayout.closeDrawer(GravityCompat.START);

        return true;

    }
}