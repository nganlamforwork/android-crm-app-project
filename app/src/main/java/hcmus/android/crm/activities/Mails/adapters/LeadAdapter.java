package hcmus.android.crm.activities.Mails.adapters;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import hcmus.android.crm.R;
import hcmus.android.crm.activities.Mails.ChooseEmailsActivity;
import hcmus.android.crm.activities.Tags.TagDetailActivity;
import hcmus.android.crm.models.Lead;

public class LeadAdapter extends FirestoreRecyclerAdapter<Lead, LeadAdapter.LeadViewHolder> {
    Context context;
    String leadId;
    private Set<String> selectedEmails;


    public LeadAdapter(@NonNull FirestoreRecyclerOptions<Lead> options, Context context) {
        super(options);
        this.context = context;
        this.selectedEmails = new HashSet<>();
    }

    @Override
    protected void onBindViewHolder(@NonNull LeadAdapter.LeadViewHolder holder, int position, @NonNull Lead model) {
        holder.leadName.setText(model.getName());
        holder.leadEmail.setText(model.getEmail());

        holder.checkboxLead.setOnCheckedChangeListener((buttonView, isChecked) -> {
            DocumentSnapshot snapshot = getSnapshots().getSnapshot(holder.getAbsoluteAdapterPosition());
            String email = model.getEmail();

            if (isChecked) {
                selectedEmails.add(email);
            } else {
                selectedEmails.remove(email);
            }

            ((ChooseEmailsActivity) context).updateSaveButtonState();
        });

    }

    @NonNull
    @Override
    public LeadViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_lead_of_tag, parent, false);
        return new LeadAdapter.LeadViewHolder(view);
    }


    public Set<String> getSelectedEmails() {
        return selectedEmails;
    }
    public Context getContext() {
        return context;
    }


    public static class LeadViewHolder extends RecyclerView.ViewHolder {
        TextView leadName, leadEmail;
        CheckBox checkboxLead;

        public LeadViewHolder(@NonNull View itemView) {
            super(itemView);

            leadName = itemView.findViewById(R.id.leadName);
            leadEmail = itemView.findViewById(R.id.leadPhone);
            checkboxLead = itemView.findViewById(R.id.checkboxLead);
        }
    }

}