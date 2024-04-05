package hcmus.android.crm.activities.Calendar.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;


import hcmus.android.crm.R;
import hcmus.android.crm.models.Event;
import hcmus.android.crm.models.Lead;
import hcmus.android.crm.utilities.Constants;
import hcmus.android.crm.utilities.PreferenceManager;

public class EventAdapter extends ArrayAdapter<Event> {
    private FirebaseFirestore db;
    private PreferenceManager preferenceManager;

    public EventAdapter(@NonNull Context context, List<Event> events, FirebaseFirestore firestore, PreferenceManager preferenceManager) {
        super(context, 0, events);
        this.db = firestore;
        this.preferenceManager = preferenceManager;
    }
    static class ViewHolder {
        TextView textViewDate;
        TextView textViewName;
        TextView textViewDescription;
        TextView textViewTime;
        TextView textViewLocation;
        ImageView deleteEventBtn;
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.event_cell, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.textViewDate = convertView.findViewById(R.id.textViewDate);
            viewHolder.textViewName = convertView.findViewById(R.id.textViewName);
            viewHolder.textViewDescription = convertView.findViewById(R.id.textViewDescription);
            viewHolder.textViewTime = convertView.findViewById(R.id.textViewTime);
            viewHolder.textViewLocation = convertView.findViewById(R.id.textViewLocation);
            viewHolder.deleteEventBtn = convertView.findViewById(R.id.deleteEventBtn);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Event event = getItem(position);
        if (event != null) {
            viewHolder.textViewDate.setText(event.getDate());
            viewHolder.textViewName.setText(event.getName());
            viewHolder.textViewDescription.setText(event.getDescription());
            viewHolder.textViewTime.setText("Time: " + event.getTime());
            viewHolder.textViewLocation.setText("Location: " + event.getLocation());

            viewHolder.deleteEventBtn.setOnClickListener(v -> {
                showDeleteConfirmationDialog(event);
            });
        }

        return convertView;
    }
    private void showDeleteConfirmationDialog(Event event) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Delete Event");
        builder.setMessage("Are you sure you want to delete this event?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteEvent(event);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }
    public void deleteEvent(Event event) {
        db.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID))
                .collection(Constants.KEY_COLLECTION_EVENTS)
                .document(event.EventId).delete();
        remove(event);
        notifyDataSetChanged();
    }
}
