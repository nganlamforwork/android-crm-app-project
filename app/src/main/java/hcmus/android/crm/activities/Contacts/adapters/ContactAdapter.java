package hcmus.android.crm.activities.Contacts.adapters;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.List;

import hcmus.android.crm.R;
import hcmus.android.crm.activities.Contacts.ContactDetailActivity;
import hcmus.android.crm.activities.Search.SearchActivity;
import hcmus.android.crm.models.Contact;
import hcmus.android.crm.models.Lead;
import hcmus.android.crm.utilities.Constants;

public class ContactAdapter extends FirestoreRecyclerAdapter<Contact, ContactAdapter.ContactViewHolder> implements Filterable {
    Context context;
    String contactId;
    private List<Contact> contactList;
    private List<Contact> contactListFiltered;


    public ContactAdapter(@NonNull FirestoreRecyclerOptions<Contact> options, Context context) {
        super(options);
        this.context = context;
        this.contactList = options.getSnapshots();
        this.contactListFiltered = contactList;
    }

    @Override
    protected void onBindViewHolder(@NonNull ContactAdapter.ContactViewHolder holder, int position, @NonNull Contact model) {
        Contact contact = contactListFiltered.get(position);

        holder.contactName.setText(contact.getName());
        holder.contactPhone.setText(contact.getPhone());

        if (contact.getImage() != null && !contact.getImage().isEmpty()) {
            byte[] bytes = Base64.decode(contact.getImage(), Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            holder.contactImage.setImageBitmap(bitmap);
        } else {
            holder.contactImage.setImageResource(R.drawable.avatar);
        }

        holder.itemView.setOnClickListener(v -> {
            DocumentSnapshot snapshot = getSnapshots()
                    .getSnapshot(holder.getAbsoluteAdapterPosition());
            contactId = snapshot.getId();

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
        DocumentSnapshot snapshot = getSnapshots()
                .getSnapshot(position);
        contactId = snapshot.getId();

        String currentUserId = FirebaseAuth.getInstance().getUid();
        FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_USERS)
                .document(currentUserId)
                .collection(Constants.KEY_COLLECTION_CONTACTS)
                .document(contactId).delete();
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
                    filterResults.values = contactList;
                } else {
                    List<Contact> filteredList = new ArrayList<>();

                    for (Contact contact : contactList) {
                        if (contactMatchesFilter(contact, charString, selectedFilter)) {
                            filteredList.add(contact);
                        }
                    }

                    filterResults.values = filteredList;
                }

                return filterResults;
            }


            @SuppressLint("NotifyDataSetChanged")
            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                contactListFiltered = (List<Contact>) filterResults.values;
                notifyDataSetChanged();
            }
        };
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

    @Override
    public int getItemCount() {
        if (contactListFiltered != null) {
            return contactListFiltered.size();
        } else {
            return 0; // or return a default value, depending on your logic
        }
    }

    private boolean contactMatchesFilter(Contact contact, String filterText, String selectedFilter) {
        // Check if the lead attribute matches the filter text based on the selected filter option
        switch (selectedFilter) {
            case "Name":
                return contact.getName().toLowerCase().contains(filterText);
            case "Phone":
                return contact.getPhone().toLowerCase().contains(filterText);
            case "Email":
                return contact.getEmail().toLowerCase().contains(filterText);
            default:
                return false;
        }
    }
}
