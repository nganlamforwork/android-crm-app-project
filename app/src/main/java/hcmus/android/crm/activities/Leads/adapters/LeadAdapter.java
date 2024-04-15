package hcmus.android.crm.activities.Leads.adapters;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.List;

import hcmus.android.crm.R;
import hcmus.android.crm.activities.Leads.LeadDetailActivity;
import hcmus.android.crm.models.Lead;
import hcmus.android.crm.utilities.Constants;

public class LeadAdapter extends FirestoreRecyclerAdapter<Lead, LeadAdapter.LeadViewHolder> implements Filterable {
    Context context;
    String leadId;
    private List<Lead> leadList;
    private List<Lead> leadListFiltered;


    public LeadAdapter(@NonNull FirestoreRecyclerOptions<Lead> options, Context context) {
        super(options);
        this.context = context;
        this.leadList = options.getSnapshots();
        this.leadListFiltered = leadList;
    }

    @Override
    protected void onBindViewHolder(@NonNull LeadAdapter.LeadViewHolder holder, int position, @NonNull Lead model) {
        Lead lead = leadListFiltered.get(position);

        holder.leadName.setText(lead.getName());
        holder.leadPhone.setText(lead.getPhone());

        if (lead.getImage() != null && !lead.getImage().isEmpty()) {
            byte[] bytes = Base64.decode(lead.getImage(), Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            holder.leadImage.setImageBitmap(bitmap);
        } else {
            holder.leadImage.setImageResource(R.drawable.avatar);
        }

        holder.itemView.setOnClickListener(v -> {
            DocumentSnapshot snapshot = getSnapshots()
                    .getSnapshot(holder.getAbsoluteAdapterPosition());
            leadId = snapshot.getId();

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
        DocumentSnapshot snapshot = getSnapshots()
                .getSnapshot(position);
        leadId = snapshot.getId();

        String currentUserId = FirebaseAuth.getInstance().getUid();
        FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_USERS)
                .document(currentUserId)
                .collection(Constants.KEY_COLLECTION_LEADS)
                .document(leadId).delete();
        notifyDataSetChanged();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString().toLowerCase();
                FilterResults filterResults = new FilterResults();

                if (charString.isEmpty()) {
                    // If the filter query is empty, restore the original list
                    filterResults.values = leadList;
                } else {
                    List<Lead> filteredList = new ArrayList<>();

                    // Filter the list based on the query
                    for (Lead lead : leadList) {
                        if (lead.getName().toLowerCase().contains(charString) ||
                                lead.getPhone().toLowerCase().contains(charString) ||
                                lead.getCompany().toLowerCase().contains(charString)) {
                            filteredList.add(lead);
                        }
                    }

                    filterResults.values = filteredList;
                }

                return filterResults;
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                leadListFiltered = (List<Lead>) filterResults.values;
                notifyDataSetChanged();
            }
        };
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

    @Override
    public int getItemCount() {
        return leadListFiltered.size();
    }
}
