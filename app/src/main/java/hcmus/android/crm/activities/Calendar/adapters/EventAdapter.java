package hcmus.android.crm.activities.Calendar.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;


import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


import hcmus.android.crm.R;
import hcmus.android.crm.activities.Calendar.EventEditActivity;
import hcmus.android.crm.models.Event;
import hcmus.android.crm.utilities.CalendarUtils;
import hcmus.android.crm.utilities.Constants;
import hcmus.android.crm.utilities.FirebaseUtils;

public class EventAdapter extends FirestoreRecyclerAdapter<Event, EventAdapter.EventViewHolder> {

    Context context;
    String eventId;

    public EventAdapter(@NonNull FirestoreRecyclerOptions<Event> options, Context context) {

        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull EventAdapter.EventViewHolder holder, int position, @NonNull Event model) {
        holder.textViewDate.setText(model.getDate());
        holder.textViewName.setText(model.getName());
        holder.textViewDescription.setText(model.getDescription());
        holder.textViewTime.setText("Time: " + model.getTime());
        holder.textViewLocation.setText(model.getLocation());
        Log.d("PASSED", String.valueOf(model.isPassed()));

        // Check if event is passed
        if (model.isPassed()) {
            holder.textViewStatus.setText("Done");
            holder.textViewStatus.setTextColor(ContextCompat.getColor(context, R.color.success));
            holder.imageViewStatus.setImageResource(R.drawable.baseline_check_circle_outline_24);
            holder.imageViewStatus.setColorFilter(ContextCompat.getColor(context, R.color.success));
        } else {
            holder.textViewStatus.setText("In progress");
            holder.textViewStatus.setTextColor(ContextCompat.getColor(context, R.color.warning));
            holder.imageViewStatus.setImageResource(R.drawable.baseline_access_time_24);
            holder.imageViewStatus.setColorFilter(ContextCompat.getColor(context, R.color.warning));
        }

        holder.settingsOptionsBtn.setOnClickListener(v -> {
            showSettingsMenu(v, getSnapshots().getSnapshot(position).getId(), model, holder);
        });

        holder.itemView.setOnClickListener(v -> {
            DocumentSnapshot snapshot = getSnapshots()
                    .getSnapshot(holder.getAbsoluteAdapterPosition());
            eventId = snapshot.getId();

            Intent intent = new Intent(context, EventEditActivity.class);

            intent.putExtra("eventId", eventId);
            intent.putExtra("selectedDate", String.valueOf(CalendarUtils.selectedDate));
            intent.putExtra("name", model.getName());
            intent.putExtra("description", model.getDescription());
            intent.putExtra("time", model.getTime());
            intent.putExtra("location", model.getLocation());
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            context.startActivity(intent);
        });
    }

    private void showSettingsMenu(View anchorView, String eventId, Event event, EventAdapter.EventViewHolder holder) {
        Context wrapper = new ContextThemeWrapper(context, R.style.PopupMenuStyle);
        PopupMenu popupMenu = new PopupMenu(wrapper, anchorView);
        popupMenu.inflate(R.menu.event_menu_settings);


        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_delete) {
                showDeleteConfirmationDialog(eventId, event);
                return true;
            } else if (itemId == R.id.action_mark_as_done) {
                markEventAsDone(eventId, holder, item);
                return true;
            }
            return false;
        });

        popupMenu.show();
    }

    @NonNull
    @Override
    public EventAdapter.EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.event_cell, parent, false);
        return new EventAdapter.EventViewHolder(view);
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView textViewDate, textViewName, textViewDescription, textViewTime, textViewLocation, textViewStatus;

        ImageView settingsOptionsBtn, imageViewStatus;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);

            textViewDate = itemView.findViewById(R.id.textViewDate);
            textViewName = itemView.findViewById(R.id.textViewName);
            textViewDescription = itemView.findViewById(R.id.textViewDescription);
            textViewTime = itemView.findViewById(R.id.textViewTime);
            textViewLocation = itemView.findViewById(R.id.textViewLocation);
            settingsOptionsBtn = itemView.findViewById(R.id.settingsOptionsBtn);
            textViewStatus = itemView.findViewById(R.id.textViewStatus);
            imageViewStatus = itemView.findViewById(R.id.imageViewStatus);
        }
    }

    private void showDeleteConfirmationDialog(String eventId, Event event) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Delete Event");
        builder.setMessage("Are you sure you want to delete this event?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteEvent(eventId);
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

    public void deleteEvent(String eventId) {
        Log.d("EventId", eventId);
        FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_USERS)
                .document(FirebaseUtils.currentUserId())
                .collection(Constants.KEY_COLLECTION_EVENTS)
                .document(eventId).delete();
        notifyDataSetChanged();
        Toast.makeText(context, "Event is deleted successfully", Toast.LENGTH_SHORT);
    }

    private void markEventAsDone(String eventId, EventAdapter.EventViewHolder holder, MenuItem menuItem) {
        FirebaseFirestore.getInstance()
                .collection(Constants.KEY_COLLECTION_USERS)
                .document(FirebaseUtils.currentUserId())
                .collection(Constants.KEY_COLLECTION_EVENTS)
                .document(eventId)
                .update("passed", true)
                .addOnSuccessListener(aVoid -> {
                    // Update successful
                    notifyDataSetChanged();
                    Log.d("Event", "Event marked as done.");
                    Toast.makeText(context, "Event is marked as done", Toast.LENGTH_SHORT).show();

                    // Update UI - Set checkbox as checked
                    holder.textViewStatus.setText("Done");
                    holder.textViewStatus.setTextColor(ContextCompat.getColor(context, R.color.success));
                    holder.imageViewStatus.setImageResource(R.drawable.baseline_check_circle_outline_24);
                    holder.imageViewStatus.setColorFilter(ContextCompat.getColor(context, R.color.success));

                    // Set the menu item as checked
                    menuItem.setChecked(true);
                })
                .addOnFailureListener(e -> {
                    // Handle error
                    Log.e("Event", "Failed to mark event as done.", e);
                    Toast.makeText(context, "Failed to mark event as done.", Toast.LENGTH_SHORT).show();
                });
    }

}
