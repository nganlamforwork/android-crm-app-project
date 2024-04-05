package hcmus.android.crm.activities.Chat;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.Arrays;

import hcmus.android.crm.activities.Chat.adapters.ChatAdapter;
import hcmus.android.crm.activities.DrawerBaseActivity;
import hcmus.android.crm.models.ChatMessage;
import hcmus.android.crm.models.ChatRoom;
import hcmus.android.crm.models.User;
import hcmus.android.crm.databinding.ActivityChatBinding;
import hcmus.android.crm.utilities.Constants;

public class ChatActivity extends DrawerBaseActivity {
    private final static String TAG = "ChatActivity";
    private ActivityChatBinding binding;
    private User otherUser;
    private String otherUserId, chatroomId;
    private EditText messageInput;
    private ImageButton sendMessageBtn;
    private ImageButton backBtn;
    private TextView otherUsername;
    private Query query;
    private RecyclerView recyclerView;
    FirebaseFirestore db;
    private ChatRoom chatRoom;
    private String currentUserId;
    private ChatAdapter chatAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        db = FirebaseFirestore.getInstance();

        otherUserId = getIntent().getStringExtra("otherUserId");
        otherUser = getIntent().getParcelableExtra("otherUserData");

        initialize();
        setupChatRecyclerView();
        setListeners();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (chatAdapter != null)
            chatAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (chatAdapter != null)
            chatAdapter.stopListening();
    }

    private String getChatroomId(String userId1, String userId2) {
        if (userId1.hashCode() < userId2.hashCode()) {
            return userId1 + "_" + userId2;
        } else {
            return userId2 + "_" + userId1;
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }

    private void initialize() {
        // Get id
        messageInput = binding.chatMessageInput;
        sendMessageBtn = binding.messageSendBtn;
        backBtn = binding.backBtn;
        recyclerView = binding.chatRecyclerView;
        otherUsername = binding.otherUsername;

        currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);
        chatroomId = getChatroomId(currentUserId, otherUserId);
        otherUsername.setText(otherUser.getName());

        String imageString = otherUser.getImage();
        if (imageString != null && !imageString.isEmpty()) {
            byte[] bytes = Base64.decode(imageString, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            binding.profilePicLayout.avatar.setImageBitmap(bitmap);
        }
    }

    private void setupChatRecyclerView() {
        query = db.collection(Constants.KEY_COLLECTION_CHAT_ROOMS).document(chatroomId)
                .collection(Constants.KEY_COLLECTION_CHATS)
                .orderBy("timestamp", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<ChatMessage> options = new FirestoreRecyclerOptions.Builder<ChatMessage>()
                .setQuery(query, ChatMessage.class).build();

        chatAdapter = new ChatAdapter(options, getApplicationContext());
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setReverseLayout(true);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(chatAdapter);
        chatAdapter.startListening();
        chatAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                recyclerView.smoothScrollToPosition(0);
            }
        });
    }


    private void setListeners() {
        backBtn.setOnClickListener(v -> {
            getOnBackPressedDispatcher().onBackPressed();
        });

        sendMessageBtn.setOnClickListener(v -> {
            String message = messageInput.getText().toString().trim();
            if (message.isEmpty())
                return;
            sendMessageToUser(message);
        });

        getOrCreateChatRoom();
    }

    private void sendMessageToUser(String message) {
        chatRoom.setLastMessageTimestamp(Timestamp.now());
        chatRoom.setLastMessageSenderId(currentUserId);
        db.collection(Constants.KEY_COLLECTION_CHAT_ROOMS).document(chatroomId).set(chatRoom);

        ChatMessage chatMessage = new ChatMessage(message, currentUserId, Timestamp.now());
        db.collection(Constants.KEY_COLLECTION_CHAT_ROOMS).document(chatroomId)
                .collection(Constants.KEY_COLLECTION_CHATS).add(chatMessage)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if (task.isSuccessful()) {
                            messageInput.setText("");
                        }
                    }
                });
    }

    private void getOrCreateChatRoom() {
        db.collection(Constants.KEY_COLLECTION_CHAT_ROOMS).document(chatroomId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                chatRoom = task.getResult().toObject(ChatRoom.class);
                if (chatRoom == null) {
                    // first time chat
                    chatRoom = new ChatRoom(chatroomId, Arrays.asList(preferenceManager.getString(Constants.KEY_USER_ID), otherUserId), Timestamp.now(), "");
                }
            }
            db.collection(Constants.KEY_COLLECTION_CHAT_ROOMS).document(chatroomId).set(chatRoom);
        });
    }
}