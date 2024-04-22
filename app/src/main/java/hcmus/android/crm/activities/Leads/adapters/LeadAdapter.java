package hcmus.android.crm.activities.Leads.adapters;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.camera2.CaptureRequest;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import hcmus.android.crm.R;
import hcmus.android.crm.activities.Leads.LeadDetailActivity;
import hcmus.android.crm.activities.Search.SearchActivity;
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

        long currentDayMillis = System.currentTimeMillis();
        long leadCreationMillis = lead.getCreatedAt().getTime(); // Convert Date to milliseconds
        long timeDifferenceMillis = currentDayMillis - leadCreationMillis;
        long timeDifferenceDays = TimeUnit.MILLISECONDS.toDays(timeDifferenceMillis);

        if (timeDifferenceDays <= 7) {
            holder.chip.setText("Recently Added");
            holder.chip.setTextColor(ContextCompat.getColor(context, R.color.recently_added_text_color));
            holder.chip.setChipBackgroundColorResource(R.color.recently_added_chip_background_color);
            holder.chip.setChipStrokeColorResource(R.color.recently_added_text_color);
        } else if (timeDifferenceDays <= 30) {
            holder.chip.setText("Over 7 days");
            holder.chip.setTextColor(ContextCompat.getColor(context, R.color.days_ago_text_color));
            holder.chip.setChipBackgroundColorResource(R.color.days_ago_chip_background_color);
            holder.chip.setChipStrokeColorResource(R.color.days_ago_text_color);
        } else {
            holder.chip.setText("Over 30 days");
            holder.chip.setTextColor(ContextCompat.getColor(context, R.color.over_seven_days_text_color));
            holder.chip.setChipBackgroundColorResource(R.color.over_seven_days_chip_background_color);
            holder.chip.setChipStrokeColorResource(R.color.over_seven_days_text_color);
        }

    }

    @NonNull
    @Override
    public LeadViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.custom_list_view_w_chip, parent, false);
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
                String selectedFilter = SearchActivity.selectedFilter;
                FilterResults filterResults = new FilterResults();

                if (charString.isEmpty()) {
                    filterResults.values = leadList;
                } else {
                    List<Lead> filteredList = new ArrayList<>();
                    for (Lead lead : leadList) {
                        if (leadMatchesFilter(lead, charString, selectedFilter)) {
                            filteredList.add(lead);
                        }
                    }

                    filterResults.values = filteredList;
                }

                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                leadListFiltered = (List<Lead>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    private boolean leadMatchesFilter(Lead lead, String filterText, String selectedFilter) {
        // Check if the lead attribute matches the filter text based on the selected filter option
        switch (selectedFilter) {
            case "Name":
                return lead.getName().toLowerCase().contains(filterText);
            case "Phone":
                return lead.getPhone().toLowerCase().contains(filterText);
            case "Company":
                return lead.getCompany().toLowerCase().contains(filterText);
            case "Job":
                return lead.getJob().toLowerCase().contains(filterText);
            case "Email":
                return lead.getEmail().toLowerCase().contains(filterText);
            case "Address":
                return lead.getAddress().toLowerCase().contains(filterText);
            default:
                return false;
        }
    }

    public static class LeadViewHolder extends RecyclerView.ViewHolder {
        Chip chip;
        TextView leadName, leadPhone;
        RoundedImageView leadImage;

        public LeadViewHolder(@NonNull View itemView) {
            super(itemView);

            leadName = itemView.findViewById(R.id.nameLabel);
            leadPhone = itemView.findViewById(R.id.phoneLabel);
            leadImage = itemView.findViewById(R.id.imageIcon);
            chip = itemView.findViewById(R.id.chip);
        }
    }

    @Override
    public int getItemCount() {
        return leadListFiltered.size();
    }
}
