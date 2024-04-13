package hcmus.android.crm.activities.Notes.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import hcmus.android.crm.R;
import hcmus.android.crm.activities.Chat.ChatActivity;
import hcmus.android.crm.activities.Notes.CreateNoteActivity;
import hcmus.android.crm.models.Note;
import hcmus.android.crm.utilities.CalendarUtils;
import hcmus.android.crm.utilities.Constants;
import hcmus.android.crm.utilities.FirebaseUtils;
import hcmus.android.crm.utilities.PreferenceManager;


public class NoteAdapter extends FirestoreRecyclerAdapter<Note, NoteAdapter.NoteViewHolder> {
    Context context;

    PreferenceManager preferenceManager;

    public NoteAdapter(@NonNull FirestoreRecyclerOptions<Note> options, Context context, PreferenceManager preferenceManager) {

        super(options);
        this.context = context;
        this.preferenceManager = preferenceManager;
    }


    @Override
    protected void onBindViewHolder(@NonNull NoteAdapter.NoteViewHolder holder, int position, @NonNull Note model) {
        holder.title.setText(model.getTitle());
        holder.content.setText(model.getContent());

        int colorCode = getRandomColor();
        holder.note.setBackgroundColor(holder.itemView.getResources().getColor(colorCode, null));

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        });
        DocumentSnapshot snapshot = getSnapshots()
                .getSnapshot(holder.getAbsoluteAdapterPosition());
        String noteId = snapshot.getId();

        holder.menuPopup.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
            popupMenu.setGravity(Gravity.END);
            popupMenu.getMenu().add("Edit").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(@NonNull MenuItem item) {
                    Intent intent = new Intent(v.getContext(), CreateNoteActivity.class);

                    intent.putExtra("noteId", noteId);
                    intent.putExtra("title", model.getTitle());
                    intent.putExtra("content", model.getContent());
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    v.getContext().startActivity(intent);
                    return false;
                }
            });
            popupMenu.getMenu().add("Delete").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(@NonNull MenuItem item) {
                    showDeleteConfirmationDialog(noteId);
                    return false;
                }
            });
            popupMenu.show();
        });
    }

    private void showDeleteConfirmationDialog(String noteId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Delete Note");
        builder.setMessage("Are you sure you want to delete this note?");

        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_USERS)
                        .document(FirebaseUtils.currentUserId())
                        .collection(Constants.KEY_COLLECTION_LEADS)
                        .document(preferenceManager.getString("selectedLead"))
                        .collection(Constants.KEY_COLLECTION_NOTES)
                        .document(noteId).delete();
                notifyDataSetChanged();
                Toast.makeText(context, "Note is deleted successfully", Toast.LENGTH_SHORT);
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

    @NonNull
    @Override
    public NoteAdapter.NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notes_layout, parent, false);
        return new NoteAdapter.NoteViewHolder(view);
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView title, content;
        LinearLayout note;
        ImageView menuPopup;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.noteTitle);
            content = itemView.findViewById(R.id.noteContent);
            menuPopup = itemView.findViewById(R.id.menuPopup);
            note = itemView.findViewById(R.id.note);
        }
    }

    private int getRandomColor() {
        List<Integer> colorCode = new ArrayList<>();
        colorCode.add(R.color.color1);
        colorCode.add(R.color.color2);
        colorCode.add(R.color.color3);
        colorCode.add(R.color.color4);
        colorCode.add(R.color.color5);
        colorCode.add(R.color.color6);
        colorCode.add(R.color.color7);
        colorCode.add(R.color.color8);

        Random random = new Random();
        int number = random.nextInt(colorCode.size());

        return colorCode.get(number);
    }
}
