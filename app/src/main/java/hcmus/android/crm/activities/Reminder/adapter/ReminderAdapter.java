package hcmus.android.crm.activities.Reminder.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import hcmus.android.crm.R;
import hcmus.android.crm.activities.Calendar.EventEditActivity;
import hcmus.android.crm.activities.Reminder.AddNewReminderActivity;
import hcmus.android.crm.models.Reminder;
import hcmus.android.crm.utilities.CalendarUtils;
import hcmus.android.crm.utilities.Constants;
import hcmus.android.crm.utilities.FirebaseUtils;

public class ReminderAdapter extends FirestoreRecyclerAdapter<Reminder, ReminderAdapter.ReminderViewHolder> {

    Context context;
    String reminderId;
    private List<Reminder> reminderList;
    private LayoutInflater inflater;
    public SimpleDateFormat dateFormat = new SimpleDateFormat("EE dd MMM yyyy", Locale.US);
    public SimpleDateFormat inputDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    Date date = null;
    String outputDateString = null;


    public ReminderAdapter(@NonNull FirestoreRecyclerOptions<Reminder> options, Context context) {
        super(options);
        this.context = context;
        this.reminderList = options.getSnapshots();
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    @NonNull
    @Override
    public ReminderAdapter.ReminderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_reminder, parent, false);
        return new ReminderViewHolder(view);
    }

    @Override
    protected void onBindViewHolder(@NonNull ReminderAdapter.ReminderViewHolder holder, int position, @NonNull Reminder model) {
        Reminder reminder = reminderList.get(position);

        holder.reminderTitle.setText(reminder.getReminderTitle());
        holder.reminderDescription.setText(reminder.getReminderDescrption());
        holder.reminderTime.setText(reminder.getTimeAlarm());
        holder.reminderOptions.setOnClickListener(v -> {
            showSettingsMenu(v, getSnapshots().getSnapshot(position).getId(), model, holder);
        });

        try {
            date = inputDateFormat.parse(reminder.getDate());
            outputDateString = dateFormat.format(date);

            String[] items1 = outputDateString.split(" ");
            String day = items1[0];
            String dd = items1[1];
            String month = items1[2];

            holder.reminderDay.setText(day);
            holder.reminderDate.setText(dd);
            holder.reminderMonth.setText(month);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public Context getContext() {
        return context;
    }


    @Override
    public int getItemCount() {
        return reminderList.size();
    }

    public class ReminderViewHolder extends RecyclerView.ViewHolder {
        TextView reminderDay;
        TextView reminderDate;
        TextView reminderMonth;
        TextView reminderTitle;
        TextView reminderDescription;
        TextView reminderStatus;
        ImageView reminderOptions;
        TextView reminderTime;

        ReminderViewHolder(@NonNull View itemView) {
            super(itemView);
            reminderDay = itemView.findViewById(R.id.reminderDay);
            reminderDate = itemView.findViewById(R.id.reminderDate);
            reminderMonth = itemView.findViewById(R.id.reminderMonth);
            reminderTitle = itemView.findViewById(R.id.reminderTitle);
            reminderDescription = itemView.findViewById(R.id.reminderDescription);
            reminderOptions = itemView.findViewById(R.id.reminderOptions);
            reminderTime = itemView.findViewById(R.id.reminderTime);
        }
    }

    private void showSettingsMenu(View anchorView, String reminderId, Reminder reminder, ReminderAdapter.ReminderViewHolder holder) {
        androidx.appcompat.widget.PopupMenu popupMenu = new androidx.appcompat.widget.PopupMenu(anchorView.getContext(), anchorView);
        popupMenu.inflate(R.menu.option_reminder);


        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.menuDelete) {
                showDeleteConfirmationDialog(reminderId, reminder);
                return true;
            } else if (itemId == R.id.menuEdit) {
                editReminder(reminderId, holder, item, reminder);
                return true;
            }
            return false;
        });

        popupMenu.show();
    }

    private void showDeleteConfirmationDialog(String reminderId, Reminder reminder) {
        androidx.appcompat.app.AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Delete Reminder");
        builder.setMessage("Are you sure you want to delete this reminder?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteReminder(reminderId);
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

    private void editReminder(String reminderId, ReminderAdapter.ReminderViewHolder holder, MenuItem menuItem,@NonNull Reminder model) {
        DocumentSnapshot snapshot = getSnapshots()
                .getSnapshot(holder.getAbsoluteAdapterPosition());
        reminderId = snapshot.getId();

        Intent intent = new Intent(context, AddNewReminderActivity.class);

        intent.putExtra("reminderId", reminderId);
        intent.putExtra("selectedDate", String.valueOf(model.getDate()));
        intent.putExtra("title", model.getReminderTitle());
        intent.putExtra("description", model.getReminderDescrption());
        intent.putExtra("time", model.getTimeAlarm());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        context.startActivity(intent);
    }

    public void deleteReminder(String reminderId) {
        FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_USERS)
                .document(FirebaseUtils.currentUserId())
                .collection(Constants.KEY_COLLECTION_REMINDERS)
                .document(reminderId).delete();
        notifyDataSetChanged();
        Toast.makeText(context, "Reminder is deleted successfully", Toast.LENGTH_SHORT);
    }

}
