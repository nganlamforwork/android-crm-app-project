package hcmus.android.crm.activities.Contacts.adapters;


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
import hcmus.android.crm.activities.Contacts.ContactDetailActivity;
import hcmus.android.crm.models.Contact;
import hcmus.android.crm.utilities.Constants;

public class ContactAdapter extends FirestoreRecyclerAdapter<Contact, ContactAdapter.ContactViewHolder> {
    Context context;
    String contactId;

    public ContactAdapter(@NonNull FirestoreRecyclerOptions<Contact> options, Context context) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull ContactAdapter.ContactViewHolder holder, int position, @NonNull Contact model) {
        holder.contactName.setText(model.getName());
        holder.contactPhone.setText(model.getPhone());


        if (model.getImage() != null) {
            byte[] bytes = Base64.decode(model.getImage(), Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            holder.contactImage.setImageBitmap(bitmap);
        }
        holder.itemView.setOnClickListener(v -> {
            DocumentSnapshot snapshot = getSnapshots()
                    .getSnapshot(holder.getAbsoluteAdapterPosition());
            String contactId = snapshot.getId();

            Intent intent = new Intent(context, ContactDetailActivity.class);
            intent.putExtra("contactDetails", model);
            intent.putExtra("contactId", contactId);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        });
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.activity_custom_list_view, parent, false);
        return new ContactAdapter.ContactViewHolder(view);
    }

    public Context getContext() {
        return context;
    }

    public void deleteContact(int position) {
        String currentUserId = FirebaseAuth.getInstance().getUid();
        FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_USERS)
                .document(currentUserId)
                .collection(Constants.KEY_COLLECTION_CONTACTS)
                .document(contactId).delete();
        notifyItemRemoved(position);
    }

    public static class ContactViewHolder extends RecyclerView.ViewHolder {
        TextView contactName, contactPhone;
        RoundedImageView contactImage;

        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);

            contactName = itemView.findViewById(R.id.nameLabel);
            contactPhone = itemView.findViewById(R.id.phoneLabel);
            contactImage = itemView.findViewById(R.id.imageIcon);
        }
    }
}
