package hcmus.android.crm.activities.Mails.adapters;


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
import hcmus.android.crm.activities.Mails.TemplateDetailActivity;
import hcmus.android.crm.activities.Search.SearchActivity;
import hcmus.android.crm.activities.Tags.TagDetailActivity;
import hcmus.android.crm.activities.Tags.adapters.TagAdapter;
import hcmus.android.crm.models.Lead;
import hcmus.android.crm.models.Tag;
import hcmus.android.crm.models.Template;
import hcmus.android.crm.utilities.Constants;

public class TemplateAdapter extends FirestoreRecyclerAdapter<Template, TemplateAdapter.TemplateViewHolder> {
    Context context;
    String templateId;
    private List<Template> templateList;


    public TemplateAdapter(@NonNull FirestoreRecyclerOptions<Template> options, Context context) {
        super(options);
        this.context = context;
        this.templateList = options.getSnapshots();
    }
    @Override
    protected void onBindViewHolder(@NonNull TemplateAdapter.TemplateViewHolder holder, int position, @NonNull Template model) {
        holder.templateName.setText(model.getName());
        holder.templateSubject.setText(model.getSubject());


        holder.itemView.setOnClickListener(v -> {
            DocumentSnapshot snapshot = getSnapshots()
                    .getSnapshot(holder.getAbsoluteAdapterPosition());
            templateId = snapshot.getId();

            Intent intent = new Intent(context, TemplateDetailActivity.class);
            intent.putExtra("templateDetail", model);
            intent.putExtra("templateId", templateId);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        });
    }

    @NonNull
    @Override
    public TemplateAdapter.TemplateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.activity_custom_template_view, parent, false);
        return new TemplateAdapter.TemplateViewHolder(view);
    }

    public Context getContext() {
        return context;
    }


    public static class TemplateViewHolder extends RecyclerView.ViewHolder {
        TextView templateName, templateSubject;

        public TemplateViewHolder(@NonNull View itemView) {
            super(itemView);

            templateName = itemView.findViewById(R.id.templateName);
            templateSubject = itemView.findViewById(R.id.templateSubject);
        }
    }

}