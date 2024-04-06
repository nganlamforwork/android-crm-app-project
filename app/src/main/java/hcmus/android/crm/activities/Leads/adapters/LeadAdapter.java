package hcmus.android.crm.activities.Leads.adapters;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
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
import com.makeramen.roundedimageview.RoundedImageView;

import hcmus.android.crm.R;
import hcmus.android.crm.activities.Leads.LeadDetailActivity;
import hcmus.android.crm.models.Lead;
import hcmus.android.crm.utilities.Constants;

public class LeadAdapter extends FirestoreRecyclerAdapter<Lead, LeadAdapter.LeadViewHolder> {
    Context context;
    String leadId;

    public LeadAdapter(@NonNull FirestoreRecyclerOptions<Lead> options, Context context) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull LeadAdapter.LeadViewHolder holder, int position, @NonNull Lead model) {
        holder.leadName.setText(model.getName());
        holder.leadPhone.setText(model.getPhone());


        if (model.getImage() != null) {
            byte[] bytes = Base64.decode(model.getImage(), Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            holder.leadImage.setImageBitmap(bitmap);
        }
        holder.itemView.setOnClickListener(v -> {
            DocumentSnapshot snapshot = getSnapshots()
                    .getSnapshot(holder.getAbsoluteAdapterPosition());
            String leadId = snapshot.getId();

            Intent intent = new Intent(context, LeadDetailActivity.class);
            intent.putExtra("leadDetails", model);
            intent.putExtra("leadId", leadId);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        });
    }

    @NonNull
    @Override
    public LeadViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.activity_custom_list_view, parent, false);
        return new LeadAdapter.LeadViewHolder(view);
    }

    public Context getContext() {
        return context;
    }

    public void deleteLead(int position) {
        String currentUserId = FirebaseAuth.getInstance().getUid();
        FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_USERS)
                .document(currentUserId)
                .collection(Constants.KEY_COLLECTION_LEADS)
                .document(leadId).delete();
        notifyItemRemoved(position);
    }

    public static class LeadViewHolder extends RecyclerView.ViewHolder {
        TextView leadName, leadPhone;
        RoundedImageView leadImage;

        public LeadViewHolder(@NonNull View itemView) {
            super(itemView);

            leadName = itemView.findViewById(R.id.nameLabel);
            leadPhone = itemView.findViewById(R.id.phoneLabel);
            leadImage = itemView.findViewById(R.id.imageIcon);
        }
    }
}
