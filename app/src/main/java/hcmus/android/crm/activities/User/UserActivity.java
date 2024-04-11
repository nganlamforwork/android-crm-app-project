package hcmus.android.crm.activities.User;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;

import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import hcmus.android.crm.R;
import hcmus.android.crm.activities.DrawerBaseActivity;
import hcmus.android.crm.activities.User.adapters.UserAdapter;
import hcmus.android.crm.databinding.ActivityUserBinding;
import hcmus.android.crm.models.User;
import hcmus.android.crm.utilities.Constants;

public class UserActivity extends DrawerBaseActivity {
    private final static String TAG = "UserActivity";
    private ActivityUserBinding binding;
    private FirebaseFirestore db;
    private UserAdapter userAdapter;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Co-workers");

        binding = ActivityUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();

        setupUserRecyclerview();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (userAdapter != null)
            userAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (userAdapter != null)
            userAdapter.stopListening();
    }

    private void setupUserRecyclerview() {
        recyclerView = binding.userRecyclerView;
        Query query = db.collection(Constants.KEY_COLLECTION_USERS)
                .whereNotEqualTo("userId", preferenceManager.getString(Constants.KEY_USER_ID));

        checkIfListEmpty(query);

        FirestoreRecyclerOptions<User> options = new FirestoreRecyclerOptions.Builder<User>()
                .setQuery(query, User.class).build();

        userAdapter = new UserAdapter(options, getApplicationContext());
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(userAdapter);
        userAdapter.startListening();
    }

    private void checkIfListEmpty(Query query) {
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().getDocuments().size() > 0) {
                        recyclerView.setVisibility(View.VISIBLE);
                        binding.emptyView.setVisibility(View.GONE);
                    } else {
                        recyclerView.setVisibility(View.GONE);
                        binding.emptyView.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        navigationView.setCheckedItem(R.id.nav_coworkers);
        if (userAdapter != null)
            userAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_search) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}