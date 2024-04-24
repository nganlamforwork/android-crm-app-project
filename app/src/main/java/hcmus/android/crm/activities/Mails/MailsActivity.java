package hcmus.android.crm.activities.Mails;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

import hcmus.android.crm.R;
import hcmus.android.crm.activities.DrawerBaseActivity;
import hcmus.android.crm.activities.Settings.SettingsAppearanceActivity;
import hcmus.android.crm.activities.Settings.SettingsListViewAdapter;
import hcmus.android.crm.databinding.ActivityMailsBinding;
import hcmus.android.crm.utilities.Constants;
import hcmus.android.crm.utilities.PreferenceManager;

public class MailsActivity extends DrawerBaseActivity {
    private ActivityMailsBinding binding;
    private ListView mailsOptions;
    // The n-th row in the list will consist of [icon, label] where icon = thumbnail[n] and label=items[n]
    private String[] options = {"Send New Mail", "Templates", "History" };

    private PreferenceManager preferenceManager;
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Mails");

        binding = ActivityMailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferenceManager = new PreferenceManager(getApplicationContext());

        loadCredentials();
        setListeners();
    }
    @Override
    protected void onResume() {
        super.onResume();
        navigationView.setCheckedItem(R.id.nav_mails);
    }
    private void loadCredentials() {
        userEmail = preferenceManager.getString(Constants.KEY_EMAIL);
    }
    private void setListeners() {
        SettingsListViewAdapter adapter = new SettingsListViewAdapter(this, R.layout.settings_list_view_item, options);

        // bind intrinsic ListView to custom adapter
        mailsOptions = findViewById(R.id.mailsOptions);
        mailsOptions.setAdapter(adapter);
        mailsOptions.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                adapter.setSelectedItemPosition(position);
                adapter.notifyDataSetChanged();

                if (position == 0) {
                    Intent intent = new Intent(MailsActivity.this, SendNewMailActivity.class);
                    startActivity(intent);
                }
                else if (position == 1){
                    Intent intent = new Intent(MailsActivity.this, SettingsAppearanceActivity.class);
                    startActivity(intent);
                }
            }
        });
    }
}