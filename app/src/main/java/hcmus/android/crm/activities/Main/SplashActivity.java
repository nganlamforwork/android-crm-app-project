package hcmus.android.crm.activities.Main;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.google.firebase.firestore.FirebaseFirestore;

import hcmus.android.crm.R;
import hcmus.android.crm.activities.Authentication.SignInActivity;
import hcmus.android.crm.activities.Chat.ChatActivity;
import hcmus.android.crm.models.User;
import hcmus.android.crm.utilities.Constants;
import hcmus.android.crm.utilities.FirebaseUtils;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        if (FirebaseUtils.isLoggedIn() && getIntent().getExtras() != null) {
            // from notification
            String userId = getIntent().getExtras().getString("userId");
            FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_USERS)
                    .document(userId).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            User model = task.getResult().toObject(User.class);

                            Intent mainIntent = new Intent(this, MainActivity.class);
                            mainIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                            startActivity(mainIntent);

                            Intent intent = new Intent(this, ChatActivity.class);
                            intent.putExtra("otherUserId", model.getUserId());
                            intent.putExtra("otherUserData", model);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        }
                    });
        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(SplashActivity.this, SignInActivity.class));
                }
            }, 2000);
        }

    }
}