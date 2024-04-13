package hcmus.android.crm.services;

import android.annotation.SuppressLint;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.Build;
import android.util.Log;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.time.LocalDate;
import hcmus.android.crm.models.Event;
import hcmus.android.crm.utilities.Constants;
import hcmus.android.crm.utilities.FirebaseUtils;

@SuppressLint("SpecifyJobSchedulerIdRange")
public class EventSchedulerService extends JobService {
    public static final int JOB_ID = 1001;

    @Override
    public boolean onStartJob(JobParameters params) {
        LocalDate currentDate;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            currentDate = LocalDate.now();
        } else {
            currentDate = null;
        }
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
                        LocalDate eventDate = null;
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            eventDate = LocalDate.parse(event.getDate());
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            if (currentDate.isAfter(eventDate)) {
                                // Update Firestore to mark the event as passed
                                event.setPassed(true);
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
                            } else {
                                // Event date is in the future
                                event.setPassed(false);
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("EventUpdateWorker", "Failed to fetch events from Firestore", e);
                });
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
}
