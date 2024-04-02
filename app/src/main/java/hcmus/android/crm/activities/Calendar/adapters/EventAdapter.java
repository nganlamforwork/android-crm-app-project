package hcmus.android.crm.activities.Calendar.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import hcmus.android.crm.R;
import hcmus.android.crm.models.Event;

public class EventAdapter extends ArrayAdapter<Event> {

    public EventAdapter(@NonNull Context context, List<Event> events) {
        super(context, 0, events);
    }

    static class ViewHolder {
        TextView eventCellTV;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.event_cell, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.eventCellTV = convertView.findViewById(R.id.eventCellTV);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Event event = getItem(position);
        if (event != null) {
            StringBuilder eventTitleBuilder = new StringBuilder();
            eventTitleBuilder.append(event.getName()).append(" - ").append(event.getTime());
            viewHolder.eventCellTV.setText(eventTitleBuilder.toString());
        } else {
            viewHolder.eventCellTV.setText("");
        }

        return convertView;
    }
}
