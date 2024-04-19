package hcmus.android.crm.activities.Tags;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import hcmus.android.crm.R;

import hcmus.android.crm.activities.DrawerBaseActivity;
import hcmus.android.crm.activities.Tags.adapters.TagAdapter;
import hcmus.android.crm.databinding.ActivityTagsBinding;
import hcmus.android.crm.models.Tag;
import hcmus.android.crm.utilities.Constants;

public class TagsActivity extends DrawerBaseActivity {
    private ActivityTagsBinding binding;
    private RecyclerView recyclerView;
    private FirebaseFirestore db;
    private TagAdapter tagAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Tags");
        binding = ActivityTagsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        db = FirebaseFirestore.getInstance();

        setupTagRecyclerView();

//        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new TouchHelper(tagAdapter));
//        itemTouchHelper.attachToRecyclerView(recyclerView);

        setListeners();
    }


    private void setupTagRecyclerView() {
        recyclerView = binding.tagRecyclerView;
        Query query = db.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID))
                .collection(Constants.KEY_COLLECTION_TAGS)
                .orderBy("createdAt", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<Tag> options = new FirestoreRecyclerOptions.Builder<Tag>().setQuery(query, Tag.class).build();

        checkIfListEmpty(query);

        tagAdapter = new TagAdapter(options, this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(tagAdapter);
        recyclerView.addItemDecoration(
                new DividerItemDecoration(
                        recyclerView.getContext(),
                        LinearLayoutManager.VERTICAL
                )
        );
        tagAdapter.startListening();
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
        navigationView.setCheckedItem(R.id.nav_tags);
        if (tagAdapter != null) {
            tagAdapter.startListening();
            tagAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (tagAdapter != null) tagAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (tagAdapter != null) tagAdapter.stopListening();
    }
    private void setListeners() {
        binding.manualButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(TagsActivity.this, AddNewTagActivity.class));
            }
        });
    }

}