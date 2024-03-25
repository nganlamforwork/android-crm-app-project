package hcmus.android.crm;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

import hcmus.android.crm.activities.Authentication.SignInActivity;
import hcmus.android.crm.activities.Authentication.SignUpActivity;
import hcmus.android.crm.activities.Leads.LeadActivity;
import hcmus.android.crm.activities.Settings.SettingsActivity;
import hcmus.android.crm.utilities.Constants;
import hcmus.android.crm.utilities.PreferenceManager;

public class DrawerBase extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ActionBarDrawerToggle drawerToggle;
    Toolbar toolbar;
    private PreferenceManager preferenceManager;
    TextView userName, userEmail;
    private FirebaseAuth auth;



    @Override
    public void setContentView(View view) {
        drawerLayout = (DrawerLayout) getLayoutInflater().inflate(R.layout.activity_drawer_base, null);
        auth = FirebaseAuth.getInstance();


        FrameLayout container = drawerLayout.findViewById(R.id.activityContainer);
        container.addView(view);
        super.setContentView(drawerLayout);

        /* Hooks */
        toolbar = drawerLayout.findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        navigationView = drawerLayout.findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        preferenceManager = new PreferenceManager(getApplicationContext());
        loadCredentials();

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

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void loadCredentials() {
        View headerView = navigationView.getHeaderView(0);
        userName = headerView.findViewById(R.id.textUserName);
        userEmail = headerView.findViewById(R.id.textUserEmail);

        userName.setText(preferenceManager.getString(Constants.KEY_NAME));
        userEmail.setText(preferenceManager.getString(Constants.KEY_EMAIL));
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        showToast("This is a message");

        int id = item.getItemId();

        if (id == R.id.nav_home) {
            Intent intent = new Intent(this, SignUpActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_leads) {
            Intent intent = new Intent(this, LeadActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_contact) {
            Intent intent = new Intent(this, SignInActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_organization) {
            Intent intent = new Intent(this, SignInActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_opportunity) {
            Intent intent = new Intent(this, SignInActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_quotations) {
            Intent intent = new Intent(this, SignInActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_sales) {
            Intent intent = new Intent(this, SignInActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_territory) {
            Intent intent = new Intent(this, SignInActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_communications) {
            Intent intent = new Intent(this, SignInActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_sales_forecast) {
            Intent intent = new Intent(this, SignInActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_reports) {
            Intent intent = new Intent(this, SignInActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_personal) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_logout) {
            signOut();
        }

        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.top_app_bar, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setTitle(String title) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }


    private void signOut() {
        showToast("Signing out...");
        preferenceManager.clear();
        auth.signOut();
        startActivity(new Intent(getApplicationContext(), SignInActivity.class));
        finish();
        setContentView(R.layout.activity_main);

        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setLogo(R.mipmap.ic_launcher);
            getSupportActionBar().setDisplayUseLogoEnabled(true);
        }
        navigationView.setNavigationItemSelectedListener(this);
    }
}