package com.example.data.remote

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.local.entity.FeeScheduleEntity
import com.example.data.local.entity.PaymentEntity
import com.example.data.local.entity.StudentEntity
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FirestoreManager(private val context: Context) {

    // Mandated: Always pass context.getString(R.string.firestore_database_id)
    private val firestore: FirebaseFirestore by lazy {
        val databaseId = context.getString(R.string.firestore_database_id)
        FirebaseFirestore.getInstance(databaseId)
    }

    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    suspend fun syncUserProfile() = withContext(Dispatchers.IO) {
        val user = auth.currentUser ?: return@withContext
        val userDoc = firestore.collection("users").document(user.uid)
        val data = hashMapOf(
            "userId" to user.uid,
            "email" to (user.email ?: ""),
            "displayName" to (user.displayName ?: "Parent"),
            "role" to "parent",
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            userDoc.set(data, SetOptions.merge()).await()
            Log.d("FirestoreManager", "User profile synced: ${user.uid}")
        } catch (e: Exception) {
            Log.e("FirestoreManager", "Failed to sync user profile", e)
        }
    }

    suspend fun syncStudent(student: StudentEntity) = withContext(Dispatchers.IO) {
        val uid = try { requireUserId() } catch (e: Exception) { return@withContext }
        val doc = firestore.collection("users")
            .document(uid)
            .collection("students")
            .document(student.id.toString())

        val data = hashMapOf(
            "id" to student.id.toString(),
            "userId" to uid,
            "name" to student.name,
            "grade" to student.grade,
            "rollNo" to student.rollNo,
            "annualFee" to student.annualFee,
            "avatarInitials" to student.avatarInitials,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            doc.set(data, SetOptions.merge()).await()
        } catch (e: Exception) {
            Log.e("FirestoreManager", "Failed to sync student to Firestore", e)
        }
    }

    suspend fun syncFeeSchedule(schedule: FeeScheduleEntity) = withContext(Dispatchers.IO) {
        val uid = try { requireUserId() } catch (e: Exception) { return@withContext }
        val doc = firestore.collection("users")
            .document(uid)
            .collection("students")
            .document(schedule.studentId.toString())
            .collection("fee_schedules")
            .document(schedule.id.toString())

        val data = hashMapOf(
            "id" to schedule.id.toString(),
            "studentId" to schedule.studentId.toString(),
            "userId" to uid,
            "termNumber" to schedule.termNumber,
            "termName" to schedule.termName,
            "termMonth" to schedule.termMonth,
            "dueDate" to schedule.dueDate,
            "baseAmount" to schedule.baseAmount,
            "paidAmount" to schedule.paidAmount,
            "latePenaltyAmount" to schedule.latePenaltyAmount,
            "isOverdue" to schedule.isOverdue,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            doc.set(data, SetOptions.merge()).await()
        } catch (e: Exception) {
            Log.e("FirestoreManager", "Failed to sync fee schedule to Firestore", e)
        }
    }

    suspend fun syncPayment(payment: PaymentEntity) = withContext(Dispatchers.IO) {
        val uid = try { requireUserId() } catch (e: Exception) { return@withContext }
        val doc = firestore.collection("users")
            .document(uid)
            .collection("students")
            .document(payment.studentId.toString())
            .collection("payments")
            .document(payment.id.toString())

        val data = hashMapOf(
            "id" to payment.id.toString(),
            "studentId" to payment.studentId.toString(),
            "userId" to uid,
            "scheduleId" to payment.scheduleId.toString(),
            "termName" to payment.termName,
            "amount" to payment.amount,
            "paymentDate" to payment.paymentDate,
            "paymentMode" to payment.paymentMode,
            "transactionRef" to payment.transactionRef,
            "status" to payment.status,
            "receiptNumber" to payment.receiptNumber,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            doc.set(data, SetOptions.merge()).await()
        } catch (e: Exception) {
            Log.e("FirestoreManager", "Failed to sync payment to Firestore", e)
        }
    }
}
