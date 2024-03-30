package hcmus.android.crm.activities;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

import hcmus.android.crm.R;
import hcmus.android.crm.activities.Authentication.SignInActivity;
import hcmus.android.crm.activities.Leads.LeadActivity;
import hcmus.android.crm.activities.Main.MainActivity;
import hcmus.android.crm.activities.Settings.SettingActivity;
import hcmus.android.crm.utilities.Constants;
import hcmus.android.crm.utilities.PreferenceManager;
import hcmus.android.crm.utilities.Utils;

public class DrawerBaseActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private TextView userName, userEmail;
    protected FirebaseAuth auth;
    protected PreferenceManager preferenceManager;
    protected DrawerLayout drawerLayout;
    protected NavigationView navigationView;
    private AppBarConfiguration appBarConfiguration;
    private Class<?> currentActivityClass;

    @Override
    public void setContentView(View view) {
        drawerLayout = (DrawerLayout) getLayoutInflater().inflate(R.layout.activity_drawer_base, null);

        FrameLayout container = drawerLayout.findViewById(R.id.fragment_container);
        container.addView(view);
        super.setContentView(drawerLayout);

        preferenceManager = new PreferenceManager(getApplicationContext());
        auth = FirebaseAuth.getInstance();

        Toolbar toolbar = drawerLayout.findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        navigationView.setCheckedItem(R.id.nav_home);

        // Set up navigation icon
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, findViewById(R.id.toolbar), R.string.open, R.string.close);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        // Handle back button press
        handleBackButton();
        loadCredentials();

        currentActivityClass = getClass();
    }

    private void handleBackButton() {
        OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    setEnabled(false); // Deactivate this callback
                    getOnBackPressedDispatcher().onBackPressed(); // Allow default back button behavior
                }
            }
        };
        getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);
    }

    private void loadCredentials() {
        userName = navigationView.getHeaderView(0).findViewById(R.id.textUserName);
        userEmail = navigationView.getHeaderView(0).findViewById(R.id.textUserEmail);
        userName.setText(preferenceManager.getString(Constants.KEY_NAME));
        userEmail.setText(preferenceManager.getString(Constants.KEY_EMAIL));
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.fragment_container);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        Class<?> targetActivityClass = null;
        if (id == R.id.nav_home) {
            targetActivityClass = MainActivity.class;
        } else if (id == R.id.nav_leads) {
            targetActivityClass = LeadActivity.class;
        } else if (id == R.id.nav_personal) {
            targetActivityClass = SettingActivity.class;
        } else if (id == R.id.nav_logout) {
            logout();
            return true; // Return immediately after logout
        }

        if (targetActivityClass != null && !isActivityInBackStack(targetActivityClass)) {
            Intent intent = new Intent(this, targetActivityClass);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        navigationView.setCheckedItem(R.id.nav_home);
    }

    private void showToast(String message, int length) {
        Utils.showToast(getApplicationContext(), message, length);
    }

    private void logout() {
        showToast("See you later!", 0);
        preferenceManager.clear();
        auth.signOut();
        startActivity(new Intent(getApplicationContext(), SignInActivity.class));
        finish();
    }

    protected boolean isActivityInBackStack(Class<?> activityClass) {
        return activityClass != null && activityClass.equals(currentActivityClass);
    }
}