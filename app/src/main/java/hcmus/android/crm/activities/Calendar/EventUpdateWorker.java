package hcmus.android.crm.activities.Calendar;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDate;

import hcmus.android.crm.models.Event;
import hcmus.android.crm.utilities.Constants;
import hcmus.android.crm.utilities.FirebaseUtils;

public class EventUpdateWorker extends Worker {

    public EventUpdateWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            // Query Firestore for events scheduled for the current date
            LocalDate currentDate = LocalDate.now();
            FirebaseFirestore.getInstance()
                    .collection(Constants.KEY_COLLECTION_USERS)
                    .document(FirebaseUtils.currentUserId())
                    .collection(Constants.KEY_COLLECTION_EVENTS)
                    .whereEqualTo("date", currentDate.toString())
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                            // Get event details
                            Event event = document.toObject(Event.class);

                            // Check if event date is in the past
                            LocalDate eventDate = LocalDate.parse(event.getDate());
                            if (currentDate.isAfter(eventDate)) {
                                // Update Firestore to mark the event as passed
                                FirebaseFirestore.getInstance()
                                        .collection(Constants.KEY_COLLECTION_USERS)
                                        .document(FirebaseUtils.currentUserId())
                                        .collection(Constants.KEY_COLLECTION_EVENTS)
                                        .document(document.getId())
                                        .update("passed", true)
                                        .addOnSuccessListener(aVoid -> {
                                            Log.d("EventUpdateWorker", "Event marked as passed: " + document.getId());
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e("EventUpdateWorker", "Failed to mark event as passed: " + document.getId(), e);
                                        });
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("EventUpdateWorker", "Failed to fetch events from Firestore", e);
                    });

            return Result.success(); // Task completed successfully
        } catch (Exception e) {
            Log.e("EventUpdateWorker", "Error in doWork: " + e.getMessage());
            return Result.failure(); // Task failed
        }
    }
}
