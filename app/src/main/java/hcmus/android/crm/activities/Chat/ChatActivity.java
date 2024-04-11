package hcmus.android.crm.activities.Chat;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import hcmus.android.crm.activities.Chat.adapters.ChatAdapter;
import hcmus.android.crm.activities.DrawerBaseActivity;
import hcmus.android.crm.models.ChatMessage;
import hcmus.android.crm.models.ChatRoom;
import hcmus.android.crm.models.User;
import hcmus.android.crm.databinding.ActivityChatBinding;
import hcmus.android.crm.utilities.Constants;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

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
                            sendNotification(message);
                        }
                    }
                });
    }

    private void sendNotification(String messageToNotify) {
        db.collection(Constants.KEY_COLLECTION_USERS).document(currentUserId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        User currentUser = task.getResult().toObject(User.class);
                        try {
                            JSONObject jsonObject = new JSONObject();

                            JSONObject notificationObj = new JSONObject();
                            notificationObj.put("title", currentUser.getName());
                            notificationObj.put("body", messageToNotify);

                            JSONObject dataObj = new JSONObject();
                            dataObj.put("userId", currentUser.getUserId());

                            JSONObject messageObj = new JSONObject();

                            messageObj.put("token", otherUser.getFcmToken());
                            messageObj.put("notification", notificationObj);
                            messageObj.put("data", dataObj);

                            jsonObject.put("message", messageObj);
                            Log.d("JSON", jsonObject.toString());

                            Log.d("FCM TOKEN", otherUser.getFcmToken());
                            callApi(jsonObject);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    private static String getAccessToken() throws IOException {
        String serviceAccountJson = "{\n" +
                "  \"type\": \"service_account\",\n" +
                "  \"project_id\": \"android-crm-group-10\",\n" +
                "  \"private_key_id\": \"9b9d1c103da65a0e39a068db4909816efb390798\",\n" +
                "  \"private_key\": \"-----BEGIN PRIVATE KEY-----\\nMIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQC85gW5Kga5KgEi\\nSWGHv8/aEWDc3t30pc5KunyTNdhQdK9UkUXlfTCKs2LKkyCI2PK+qnZuBTKu7vsX\\nUFhGsop/ufARmJxAOB5MDLYYs52Ndc3+tU3a7CDRbmTcv1ASSx23Z28/9YbCgEE6\\n7B4EZu0rYJxahVhSvnG73ZepUVBhhK0ivUb9C8zGQsfm4rwj6pKc2xTSohFYvAMZ\\n0DXRoFeinvnMCLrmFQobrER5SlEbCaGaByXM/sxvkWlr64PxBSsnSJY6ZJSDgJyP\\nfiSwHMSHw/W8WtYtVsnQcn2Cs5lbx6GPh5IW4gzAdrHsYm6MCoNADdWN7Q7MX+i+\\npYxDfraNAgMBAAECggEABnpPNxoV7cV0ZqqvCvLHi9zIM6gl4uTztdQpf6UwpD5Y\\nITWJI3/0npuyYdbZSQVtj58fXWcAEF0sJFDHnNxfC6xtQIvj9yolfyn5baVChs5u\\nexNYa+STPIiwB0eSlUoDpx2HhGE6VuhYiaQoDwfA5C8ANZ94glU7Embx3jx7s0ME\\naj0hns+Fj/bOKUHK9yS/Yj3UB1OX3GrUMntbBbU+74P1JNtAhvUu3Wyvxckq/IdG\\nbq6aGDBbkNgR2pg6kuy6yY0X4AsqFzdZOe0mmmISbbrmvm/RQ6lFwFv/QFUyXFt/\\nlYLmVcnJMDuaPL8hIzi0p0BOLkrw0xc/Eg1NsIdFMQKBgQD0pD0euVJS0uxcx4kA\\n810mK/sA3NGZY1GSoAl3/5mTs/WjO81AnlXNIqoc+RSQZz26F9mPMFzCnhdK93Mj\\nj12YPWwGXzNZh65db7jsUx9UBYZI8tc7UtOkicZKPG3hUdSGVZ3R9/mU51kw06BB\\ns5hComkmuE/FDR9cxdD0ByHb3QKBgQDFqzug/FqnCA428ftv4mV5/02lbqq752cC\\nUbZlJ4NCs6P5y+w6wwdyqxbOZiQkT95wG9Y6GC6GaUhgFeXQXVRW6w1e42/J89hY\\n5KC801cMzeogQH58DZ/jlVE0BljaO9axXbhen0ARBkZ8fbmiv5M707ngr81JhAL4\\nZd/QzRGycQKBgB02p53Nn4kIavVBVpjods0iH10hAV6Bv7H0rmzH9vzuVnzhnUcE\\nIVbXttyy2cPMsFBSnMY+jjUnT7zCA9vROOn/G7E/GRCt5SJ/PuPpoSoVZs27Qfxx\\njZhuY2FmCfEIJorMGoaCSCmB3uK3wsFVwu7iGhBnng733DDl5mfdmit5AoGAMH8d\\nZxbi6t4YFrTx2nZyLSdNFox7SRSx+iiwFktGcgP+9BdrAtO8rQ56zvLSZTJPQbQi\\nsCgkqgeEoUZeFuv++EA9DI3CbIruBok5QnqbfeG7UmRgY/E3zC9jN99ix1AtEWEM\\nDfMGVebzPjHebKaR6fi9PvoC5MmFrQnZirH1aJECgYB6k7cxGU/R1zLufdkbvY9S\\nKbZyoe1fajn/HdOiuhmlT9JEMs4aFJYNIdeAHkSQSV7OXb1MW6wTEqSDVTU7omzl\\nnp1zjRIZ8pvChD/89Y5hsXslKnnE+UziAAEOILyG9rZ8SHo/7ZiesG04bmlnFkoG\\nr3QEUnoBvufNjuY6bSLRKA==\\n-----END PRIVATE KEY-----\\n\",\n" +
                "  \"client_email\": \"firebase-adminsdk-53wpi@android-crm-group-10.iam.gserviceaccount.com\",\n" +
                "  \"client_id\": \"115090666800304679638\",\n" +
                "  \"auth_uri\": \"https://accounts.google.com/o/oauth2/auth\",\n" +
                "  \"token_uri\": \"https://oauth2.googleapis.com/token\",\n" +
                "  \"auth_provider_x509_cert_url\": \"https://www.googleapis.com/oauth2/v1/certs\",\n" +
                "  \"client_x509_cert_url\": \"https://www.googleapis.com/robot/v1/metadata/x509/firebase-adminsdk-53wpi%40android-crm-group-10.iam.gserviceaccount.com\",\n" +
                "  \"universe_domain\": \"googleapis.com\"\n" +
                "}\n";
        InputStream inputStream = new ByteArrayInputStream(serviceAccountJson.getBytes(StandardCharsets.UTF_8));
        GoogleCredentials googleCredentials = GoogleCredentials
                .fromStream(inputStream)
                .createScoped(Arrays.asList(Constants.SCOPES));
        googleCredentials.refresh();
        return googleCredentials.getAccessToken().getTokenValue();
    }

    private void callApi(JSONObject jsonObject) throws PackageManager.NameNotFoundException, IOException {
        new Thread(() -> {
            final String token;
            try {
                token = getAccessToken();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            new Handler(Looper.getMainLooper()).post(() -> {
                if(token != null) {
                    Log.d("Access Token", token);

                    MediaType JSON = MediaType.get("application/json; charset=utf-8");

                    OkHttpClient client = new OkHttpClient();

                    RequestBody body = RequestBody.create(jsonObject.toString(), JSON);
                    Request request = new Request.Builder()
                            .url("https://fcm.googleapis.com/v1/projects/android-crm-group-10/messages:send")
                            .post(body)
                            .header("Authorization", "Bearer " + token)
                            .build();

                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(@NonNull Call call, @NonNull IOException e) {
                            Log.d("Failed", e.getMessage());

                        }

                        @Override
                        public void onResponse(@NonNull Call call, @NonNull Response response) {
                            Log.d("RESPONSE", response.toString());
                        }
                    });
                }
            });
        }).start();
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