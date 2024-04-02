package hcmus.android.crm.activities.Contacts.adapters;


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
import hcmus.android.crm.models.Contact;
import hcmus.android.crm.utilities.Constants;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.MyViewHolder> implements Filterable {
    private final List<Contact> contactList;
    private List<Contact> contactListFiltered;
    private final Context context;
    private FirebaseFirestore db;

    private static OnContactItemClickListener listener;

    public ContactAdapter(Context context, List<Contact> contactList) {
        this.contactList = contactList;
        this.contactListFiltered = contactList;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_custom_list_view, parent, false);
        db = FirebaseFirestore.getInstance();

        return new MyViewHolder(view);
    }

    public void deleteContact(int position) {
        Contact contact = contactList.get(position);
        db.collection(Constants.KEY_COLLECTION_CONTACTS).document(contact.ContactId).delete();
        contactList.remove(position);
        notifyItemRemoved(position);
    }

    public Context getContext() {
        return context;
    }

    public void editContact(int position) {
        Contact contact = contactList.get(position);
        Bundle bundle = new Bundle();

        bundle.putString("contactName", contact.getName());
        bundle.putString("contactEmail", contact.getEmail());
        bundle.putString("contactPhone", contact.getPhone());
        bundle.putString("contactAddress", contact.getAddress());
        bundle.putString("contactJob", contact.getJob());
        bundle.putString("contactCompany", contact.getCompany());
        bundle.putString("contactImage", contact.getImage());
        bundle.putString("contactNotes", contact.getNotes());
    }

    public void setOnContactItemClickListener(OnContactItemClickListener listener) {
        ContactAdapter.listener = listener;
    }

    @Override
    public void onBindViewHolder(@NonNull ContactAdapter.MyViewHolder holder, int position) {
        Contact contact = contactList.get(position);
        holder.contactName.setText(contact.getName());
        holder.contactPhone.setText(contact.getPhone());

        byte[] bytes = Base64.decode(contact.getImage(), Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        holder.contactImage.setImageBitmap(bitmap);
    }

    public interface OnContactItemClickListener {
        void onContactItemClick(int position);
    }


    @Override
    public int getItemCount() {
        return contactListFiltered.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView contactName, contactPhone;
        RoundedImageView contactImage;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            contactName = itemView.findViewById(R.id.nameLabel);
            contactPhone = itemView.findViewById(R.id.phoneLabel);
            contactImage = itemView.findViewById(R.id.imageIcon);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAbsoluteAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onContactItemClick(position);
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
                    contactListFiltered = contactList;
                } else {
                    List<Contact> filteredList = new ArrayList<>();
                    for (Contact contact : contactList) {
                        if (contact.getName().toLowerCase().contains(charString)
                                || contact.getPhone().toLowerCase().contains(charString)
                                || contact.getCompany().toLowerCase().contains(charString)) {
                            filteredList.add(contact);
                        }
                    }

                    contactListFiltered = filteredList;
                }

                filterResults.values = contactListFiltered;

                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                contactListFiltered = (ArrayList<Contact>) filterResults.values;

                for (Contact contact : contactListFiltered) {
                    Log.d("NAME", contact.getName());
                }
                // refresh the list with filtered data
                notifyDataSetChanged();
            }
        };
    }
}
