package hcmus.android.crm.activities.Tags.adapters;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import hcmus.android.crm.R;
import hcmus.android.crm.activities.Tags.TagDetailActivity;
import hcmus.android.crm.models.Tag;
import hcmus.android.crm.utilities.Constants;

public class TagAdapter extends FirestoreRecyclerAdapter<Tag, TagAdapter.TagViewHolder> {
    Context context;
    String tagId;
    private List<Tag> tagList;


    public TagAdapter(@NonNull FirestoreRecyclerOptions<Tag> options, Context context) {
        super(options);
        this.context = context;
        this.tagList = options.getSnapshots();
    }

    @Override
    protected void onBindViewHolder(@NonNull TagAdapter.TagViewHolder holder, int position, @NonNull Tag model) {
        holder.tagTitle.setText(model.getTitle());


        holder.itemView.setOnClickListener(v -> {
            DocumentSnapshot snapshot = getSnapshots()
                    .getSnapshot(holder.getAbsoluteAdapterPosition());
            tagId = snapshot.getId();

            Intent intent = new Intent(context, TagDetailActivity.class);
            intent.putExtra("tagDetail", model);
            intent.putExtra("tagId", tagId);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        });
    }

    @NonNull
    @Override
    public TagViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.activity_custom_tag_view, parent, false);
        return new TagAdapter.TagViewHolder(view);
    }

    public Context getContext() {
        return context;
    }

    public void deleteTag(int position) {
        DocumentSnapshot snapshot = getSnapshots()
                .getSnapshot(position);
        tagId = snapshot.getId();

        String currentUserId = FirebaseAuth.getInstance().getUid();
        FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_USERS)
                .document(currentUserId)
                .collection(Constants.KEY_COLLECTION_TAGS)
                .document(tagId).delete();
        notifyDataSetChanged();
    }


    public static class TagViewHolder extends RecyclerView.ViewHolder {
        TextView tagTitle;

        public TagViewHolder(@NonNull View itemView) {
            super(itemView);

            tagTitle = itemView.findViewById(R.id.tagTitle);
        }
    }

}
