package com.example.testapp.features.chs

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class ReportUserViewModel : ViewModel() {
    private val TAG = "ReportUserViewModel"
    private val db = FirebaseFirestore.getInstance()

    fun sendReport(
        reportedUserUid: String,
        reasons: List<String>,
        additionalComments: String?,
        onComplete: (Boolean) -> Unit
    ) {
        if (reasons.isEmpty()) {
            Log.w(TAG, "sendReport called with empty reasons list")
            onComplete(false)
            return
        }

        val reportsCollection = db.collection("users")
            .document(reportedUserUid)
            .collection("reports")

        val reportData = hashMapOf(
            "reasons" to reasons,
            "additionalComments" to (additionalComments ?: ""),
            "timestamp" to Timestamp.now()
        )

        reportsCollection.add(reportData)
            .addOnSuccessListener {
                Log.d(TAG, "Report added successfully for user with UID: $reportedUserUid")

                // After adding report, count reports
                reportsCollection.get()
                    .addOnSuccessListener { snapshot ->
                        val reportsCount = snapshot.size()
                        Log.d(TAG, "User $reportedUserUid has $reportsCount reports")

                        val threshold = 10
                        if (reportsCount >= threshold) {
                            // Ban user for 24h (timeout)
                            val banDurationMillis = 24 * 60 * 60 * 1000L
                            val banExpiresAt = Timestamp.now().toDate().time + banDurationMillis

                            db.collection("users").document(reportedUserUid)
                                .update(
                                    mapOf(
                                        "banned" to true,
                                        "banExpiresAt" to banExpiresAt
                                    )
                                )
                                .addOnSuccessListener {
                                    Log.d(TAG, "User $reportedUserUid banned for timeout")
                                    onComplete(true)
                                }
                                .addOnFailureListener { e ->
                                    Log.e(TAG, "Failed to ban user $reportedUserUid", e)
                                    onComplete(false)
                                }
                        } else {
                            onComplete(true)
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Failed to count reports for user $reportedUserUid", e)
                        onComplete(false)
                    }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to add report for user with UID: $reportedUserUid", e)
                onComplete(false)
            }
    }


    fun blockUser(reportedUid: String, onComplete: (Boolean) -> Unit) {
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: run {
            onComplete(false)
            return
        }
        val userRef = db.collection("users").document(currentUid)

        userRef.update("blocked", FieldValue.arrayUnion(reportedUid))
            .addOnSuccessListener {
                Log.d(TAG, "User blocked")
                onComplete(true)
            }
            .addOnFailureListener {
                Log.e(TAG, "Failed to block user", it)
                onComplete(false)
            }
    }

    fun muteUser(reportedUid: String, onComplete: (Boolean) -> Unit) {
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: run {
            onComplete(false)
            return
        }
        val userRef = db.collection("users").document(currentUid)

        userRef.update("muted", FieldValue.arrayUnion(reportedUid))
            .addOnSuccessListener {
                Log.d(TAG, "User muted")
                onComplete(true)
            }
            .addOnFailureListener {
                Log.e(TAG, "Failed to mute user", it)
                onComplete(false)
            }
    }
}
