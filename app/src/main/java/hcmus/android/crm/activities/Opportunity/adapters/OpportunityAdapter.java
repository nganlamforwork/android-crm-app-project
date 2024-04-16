package hcmus.android.crm.activities.Opportunity.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;

import hcmus.android.crm.R;
import hcmus.android.crm.activities.Opportunity.OpportunityDetailActivity;
import hcmus.android.crm.models.Opportunity;


public class OpportunityAdapter extends FirestoreRecyclerAdapter<Opportunity, OpportunityAdapter.OpportunityViewHolder> {
    Context context;

    public OpportunityAdapter(@NonNull FirestoreRecyclerOptions<Opportunity> options, Context context) {

        super(options);
        this.context = context;
    }


    @Override
    protected void onBindViewHolder(@NonNull OpportunityAdapter.OpportunityViewHolder holder, int position, @NonNull Opportunity model) {
        holder.opportunityName.setText(model.getName());
        holder.expectedDate.setText(model.getExpectedDate());
        holder.opportunityStatus.setText(model.getStatus());

        if (model.getStatus().equals("In Prospect")) {
            holder.opportunityStatus.setTextColor(ContextCompat.getColor(context, R.color.pre));
        } else if (model.getStatus().equals("Negotiation")) {
            holder.opportunityStatus.setTextColor(ContextCompat.getColor(context, R.color.warning));
        } else if (model.getStatus().equals("Closed Won")) {
            holder.opportunityStatus.setTextColor(ContextCompat.getColor(context, R.color.success));
        } else if (model.getStatus().equals("Closed Lost")) {
            holder.opportunityStatus.setTextColor(ContextCompat.getColor(context, R.color.error));
        }

        holder.itemView.setOnClickListener(v -> {
            DocumentSnapshot snapshot = getSnapshots()
                    .getSnapshot(holder.getAbsoluteAdapterPosition());
            String opportunityId = snapshot.getId();

            Intent intent = new Intent(context, OpportunityDetailActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("opportunityDetails", model);
            intent.putExtra("opportunityId", opportunityId);
            context.startActivity(intent);
        });
    }

    @NonNull
    @Override
    public OpportunityAdapter.OpportunityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.opportunity_layout, parent, false);
        return new OpportunityAdapter.OpportunityViewHolder(view);
    }

    static class OpportunityViewHolder extends RecyclerView.ViewHolder {
        TextView opportunityName, expectedDate, opportunityStatus;

        public OpportunityViewHolder(@NonNull View itemView) {
            super(itemView);

            opportunityName = itemView.findViewById(R.id.opportunityName);
            expectedDate = itemView.findViewById(R.id.expectedDate);
            opportunityStatus = itemView.findViewById(R.id.opportunityStatus);
        }
    }
}

