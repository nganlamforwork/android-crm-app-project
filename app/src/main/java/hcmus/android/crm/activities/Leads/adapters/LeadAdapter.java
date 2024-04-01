package hcmus.android.crm.activities.Leads.adapters;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.List;

import hcmus.android.crm.R;
import hcmus.android.crm.models.Lead;
import hcmus.android.crm.utilities.Constants;

public class LeadAdapter extends RecyclerView.Adapter<LeadAdapter.LeadViewHolder> implements Filterable {
    private final List<Lead> leadList;
    private List<Lead> leadListFiltered;
    private final Context context;
    private FirebaseFirestore db;

    private static OnLeadItemClickListener listener;

    public LeadAdapter(Context context, List<Lead> leadList) {
        this.leadList = leadList;
        this.leadListFiltered = leadList;
        this.context = context;
    }

    @NonNull
    @Override
    public LeadViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_custom_list_view, parent, false);
        db = FirebaseFirestore.getInstance();

        return new LeadViewHolder(view);
    }

    public void deleteLead(int position) {
        Lead lead = leadList.get(position);
        db.collection(Constants.KEY_COLLECTION_LEADS).document(lead.LeadId).delete();
        leadList.remove(position);
        notifyItemRemoved(position);
    }

    public Context getContext() {
        return context;
    }

    public void editLead(int position) {
        Lead lead = leadList.get(position);
        Bundle bundle = new Bundle();

        bundle.putString("leadName", lead.getName());
        bundle.putString("leadEmail", lead.getEmail());
        bundle.putString("leadPhone", lead.getPhone());
        bundle.putString("leadAddress", lead.getAddress());
        bundle.putString("leadJob", lead.getJob());
        bundle.putString("leadCompany", lead.getCompany());
        bundle.putString("leadImage", lead.getImage());
        bundle.putString("leadNotes", lead.getNotes());
    }

    public void setOnLeadItemClickListener(OnLeadItemClickListener listener) {
        LeadAdapter.listener = listener;
    }

    @Override
    public void onBindViewHolder(@NonNull LeadAdapter.LeadViewHolder holder, int position) {
        Lead lead = leadList.get(position);
        holder.leadName.setText(lead.getName());
        holder.leadPhone.setText(lead.getPhone());

        byte[] bytes = Base64.decode(lead.getImage(), Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        holder.leadImage.setImageBitmap(bitmap);
    }

    public interface OnLeadItemClickListener {
        void onLeadItemClick(int position);
    }


    @Override
    public int getItemCount() {
        return leadListFiltered.size();
    }

    public static class LeadViewHolder extends RecyclerView.ViewHolder {
        TextView leadName, leadPhone;
        RoundedImageView leadImage;

        public LeadViewHolder(@NonNull View itemView) {
            super(itemView);

            leadName = itemView.findViewById(R.id.nameLabel);
            leadPhone = itemView.findViewById(R.id.phoneLabel);
            leadImage = itemView.findViewById(R.id.imageIcon);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAbsoluteAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onLeadItemClick(position);
                        }
                    }
                }
            });

        }
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                FilterResults filterResults = new FilterResults();

                if (charString.isEmpty()) {
                    leadListFiltered = leadList;
                } else {
                    List<Lead> filteredList = new ArrayList<>();
                    for (Lead lead : leadList) {
                        if (lead.getName().toLowerCase().contains(charString)
                                || lead.getPhone().toLowerCase().contains(charString)
                                || lead.getCompany().toLowerCase().contains(charString)) {
                            filteredList.add(lead);
                        }
                    }

                    leadListFiltered = filteredList;
                }

                filterResults.values = leadListFiltered;

                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                leadListFiltered = (ArrayList<Lead>) filterResults.values;

                for (Lead lead : leadListFiltered) {
                    Log.d("NAME", lead.getName());
                }
                // refresh the list with filtered data
                notifyDataSetChanged();
            }
        };
    }
}
