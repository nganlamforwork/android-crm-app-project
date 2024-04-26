package hcmus.android.crm.activities.Settings;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import hcmus.android.crm.R;

public class SettingsListViewAdapter extends ArrayAdapter<String> {
    Context context;
    String[] options;
    private int selectedItemPosition = -1;

    public SettingsListViewAdapter(Context context, int layoutToBeInflated, String[] options) {
        super(context, R.layout.settings_list_view_item, options);
        this.context = context;
        this.options = options;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        View row;
        if (convertView == null){
            row = inflater.inflate(R.layout.settings_list_view_item, null);
        }else{
            row = (View) convertView;
        }
        TextView settingLabel = (TextView) row.findViewById(R.id.settingText);
        settingLabel.setText(options[position]);
        if (position == selectedItemPosition) {
            // Set background color for the selected item
//            row.setBackgroundColor(0xffffe18f);
        } else {
            // Set default background color for other items
//            row.setBackgroundColor(0xffffffff);
        }

        return (row);
    }

    public void setSelectedItemPosition(int position) {
        selectedItemPosition = position;
    }
}
