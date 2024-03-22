package hcmus.android.crm.activities.Settings;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import hcmus.android.crm.DrawerBase;
import hcmus.android.crm.R;
import hcmus.android.crm.databinding.ActivityMainBinding;
import hcmus.android.crm.databinding.ActivitySettingsBinding;

public class SettingsActivity extends DrawerBase {
    ListView settingsOptions;
    // The n-th row in the list will consist of [icon, label] where icon = thumbnail[n] and label=items[n]
    String[] options = {"Information", "Devices", "Notifications", "Appearance", "Language", "Privacy & Security", "Storage"};
    ActivitySettingsBinding activitySettingsBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        activitySettingsBinding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(activitySettingsBinding.getRoot());
        setTitle("Settings");

        // the arguments of the custom adapter are: activityContex, layout-to-be-inflated, labels, icons
        SettingsListViewAdapter adapter = new SettingsListViewAdapter(this, R.layout.settings_list_view_item, options);

        // bind intrinsic ListView to custom adapter
        settingsOptions = (ListView) findViewById(R.id.settingsOptions);
        settingsOptions.setAdapter(adapter);

        settingsOptions.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // Update selected item position
                adapter.setSelectedItemPosition(position);

                // Notify the adapter that the data set has changed
                adapter.notifyDataSetChanged();
            }
        });

    }

}