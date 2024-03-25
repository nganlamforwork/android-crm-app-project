package hcmus.android.crm.activities.Leads.adapters;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.List;

import hcmus.android.crm.R;
import hcmus.android.crm.activities.Leads.LeadActivity;
import hcmus.android.crm.models.Lead;

public class LeadAdapter extends RecyclerView.Adapter<LeadAdapter.MyViewHolder> {
    private List<Lead> leadList;
    private LeadActivity activity;


    public LeadAdapter(LeadActivity leadActivity, List<Lead> leadList) {
        this.leadList = leadList;
        activity = leadActivity;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.activity_custom_list_view, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LeadAdapter.MyViewHolder holder, int position) {
        Lead lead = leadList.get(position);
        holder.leadName.setText(lead.getName());
        holder.leadPhone.setText(lead.getPhone());

        byte[] bytes = Base64.decode(lead.getImage(), Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        holder.leadImage.setImageBitmap(bitmap);
    }

    @Override
    public int getItemCount() {
        return leadList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView leadName, leadPhone;
        RoundedImageView leadImage;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            leadName = itemView.findViewById(R.id.nameLabel);
            leadPhone = itemView.findViewById(R.id.phoneLabel);
            leadImage = itemView.findViewById(R.id.imageIcon);

        }
    }
}
