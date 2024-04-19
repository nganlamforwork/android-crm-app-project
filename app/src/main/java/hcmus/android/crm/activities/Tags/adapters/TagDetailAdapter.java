package hcmus.android.crm.activities.Tags.adapters;


import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hcmus.android.crm.R;
import hcmus.android.crm.activities.Tags.TagDetailActivity;
import hcmus.android.crm.models.Tag;
import hcmus.android.crm.models.Lead;
import hcmus.android.crm.utilities.Constants;

public class TagDetailAdapter extends FirestoreRecyclerAdapter<Lead, TagDetailAdapter.TagViewHolder> {
    Context context;
    String leadId;
    String tagId;
    private Map<String, Boolean> leadCheckboxStates;
    private List<Lead> leadsList;


    public TagDetailAdapter(@NonNull FirestoreRecyclerOptions<Lead> options, Context context, String tagId) {
        super(options);
        this.context = context;
        this.leadsList = options.getSnapshots();
        this.tagId = tagId;
        leadCheckboxStates = new HashMap<>();
    }

    @Override
    protected void onBindViewHolder(@NonNull TagDetailAdapter.TagViewHolder holder, int position, @NonNull Lead model) {
        holder.leadName.setText(model.getName());
        holder.leadPhone.setText(model.getPhone());
        holder.checkboxLead.setChecked(model.getTagId() != null && model.getTagId().equals(tagId));


        holder.checkboxLead.setOnCheckedChangeListener((buttonView, isChecked) -> {
            DocumentSnapshot snapshot = getSnapshots()
                    .getSnapshot(holder.getAbsoluteAdapterPosition());
            leadId = snapshot.getId();

            leadCheckboxStates.put(leadId, isChecked);

            ((TagDetailActivity) context).updateSaveButtonState();
        });

        holder.itemView.setOnClickListener(v -> {
            DocumentSnapshot snapshot = getSnapshots()
                    .getSnapshot(holder.getAbsoluteAdapterPosition());
            leadId = snapshot.getId();
        });
    }

    @NonNull
    @Override
    public TagViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_lead_of_tag, parent, false);
        return new TagDetailAdapter.TagViewHolder(view);
    }
    public Map<String, Boolean> getLeadCheckboxStates() {
        return leadCheckboxStates;
    }

    public Context getContext() {
        return context;
    }


    public static class TagViewHolder extends RecyclerView.ViewHolder {
        TextView leadName, leadPhone;
        CheckBox checkboxLead;

        public TagViewHolder(@NonNull View itemView) {
            super(itemView);

            leadName = itemView.findViewById(R.id.leadName);
            leadPhone = itemView.findViewById(R.id.leadPhone);
            checkboxLead = itemView.findViewById(R.id.checkboxLead);
        }
    }

}
