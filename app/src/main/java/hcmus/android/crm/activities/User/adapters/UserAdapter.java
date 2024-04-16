package hcmus.android.crm.activities.User.adapters;

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
import com.google.firebase.firestore.DocumentSnapshot;
import com.makeramen.roundedimageview.RoundedImageView;

import hcmus.android.crm.R;
import hcmus.android.crm.activities.Chat.ChatActivity;
import hcmus.android.crm.models.User;


public class UserAdapter extends FirestoreRecyclerAdapter<User, UserAdapter.UserViewHolder> {
    Context context;

    public UserAdapter(@NonNull FirestoreRecyclerOptions<User> options, Context context) {

        super(options);
        this.context = context;
    }


    @Override
    protected void onBindViewHolder(@NonNull UserViewHolder holder, int position, @NonNull User model) {
        holder.userName.setText(model.getName());
        holder.userPhone.setText(model.getPhone());

        if (model.getImage() != null) {
            byte[] bytes = Base64.decode(model.getImage(), Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            holder.userImage.setImageBitmap(bitmap);
        } else {
            holder.userImage.setImageResource(R.drawable.avatar);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("otherUserId", model.getUserId());
            intent.putExtra("otherUserData", model);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        });
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.activity_custom_list_view, parent, false);
        return new UserViewHolder(view);
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView userName, userPhone;
        RoundedImageView userImage;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.nameLabel);
            userPhone = itemView.findViewById(R.id.phoneLabel);
            userImage = itemView.findViewById(R.id.imageIcon);
        }
    }
}
