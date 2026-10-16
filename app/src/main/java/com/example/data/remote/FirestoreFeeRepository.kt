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
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FirestoreFeeRepository(private val db: FirebaseFirestore) {

    // Convenience constructor resolving R.string.firestore_database_id
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    suspend fun seedInitialDataIfEmpty(userId: String, userEmail: String) {
        val userDoc = db.collection("users").document(userId)
        val studentsColl = userDoc.collection("students")

        try {
            val existing = studentsColl.get().await()
            if (existing.isEmpty) {
                // Register Parent Profile
                val userProfile = hashMapOf(
                    "userId" to userId,
                    "email" to userEmail,
                    "role" to "parent",
                    "createdAt" to FieldValue.serverTimestamp()
                )
                userDoc.set(userProfile, SetOptions.merge()).await()

                // Seed Aarav Sharma (Grade 7-A)
                val student1 = hashMapOf(
                    "id" to "1",
                    "userId" to userId,
                    "name" to "Aarav Sharma",
                    "grade" to "Grade 7-A",
                    "rollNo" to "SEC-2026-0742",
                    "annualFee" to 18000.0,
                    "avatarInitials" to "AS",
                    "createdAt" to FieldValue.serverTimestamp()
                )
                studentsColl.document("1").set(student1).await()

                val s1Schedules = studentsColl.document("1").collection("fee_schedules")
                s1Schedules.document("1").set(
                    hashMapOf(
                        "id" to "1",
                        "studentId" to "1",
                        "userId" to userId,
                        "termNumber" to 1,
                        "termName" to "Term 1 (June)",
                        "termMonth" to "June",
                        "dueDate" to "2026-06-15",
                        "baseAmount" to 6000.0,
                        "paidAmount" to 6000.0,
                        "latePenaltyAmount" to 0.0,
                        "isOverdue" to false
                    )
                ).await()

                s1Schedules.document("2").set(
                    hashMapOf(
                        "id" to "2",
                        "studentId" to "1",
                        "userId" to userId,
                        "termNumber" to 2,
                        "termName" to "Term 2 (October)",
                        "termMonth" to "October",
                        "dueDate" to "2026-10-15",
                        "baseAmount" to 6000.0,
                        "paidAmount" to 2000.0,
                        "latePenaltyAmount" to 0.0,
                        "isOverdue" to false
                    )
                ).await()

                s1Schedules.document("3").set(
                    hashMapOf(
                        "id" to "3",
                        "studentId" to "1",
                        "userId" to userId,
                        "termNumber" to 3,
                        "termName" to "Term 3 (January)",
                        "termMonth" to "January",
                        "dueDate" to "2027-01-15",
                        "baseAmount" to 6000.0,
                        "paidAmount" to 0.0,
                        "latePenaltyAmount" to 0.0,
                        "isOverdue" to false
                    )
                ).await()

                // Seed Diya Sharma (Grade 4-B)
                val student2 = hashMapOf(
                    "id" to "2",
                    "userId" to userId,
                    "name" to "Diya Sharma",
                    "grade" to "Grade 4-B",
                    "rollNo" to "PRI-2026-0418",
                    "annualFee" to 18000.0,
                    "avatarInitials" to "DS",
                    "createdAt" to FieldValue.serverTimestamp()
                )
                studentsColl.document("2").set(student2).await()

                val s2Schedules = studentsColl.document("2").collection("fee_schedules")
                s2Schedules.document("4").set(
                    hashMapOf(
                        "id" to "4",
                        "studentId" to "2",
                        "userId" to userId,
                        "termNumber" to 1,
                        "termName" to "Term 1 (June)",
                        "termMonth" to "June",
                        "dueDate" to "2026-06-15",
                        "baseAmount" to 6000.0,
                        "paidAmount" to 6000.0,
                        "latePenaltyAmount" to 0.0,
                        "isOverdue" to false
                    )
                ).await()

                s2Schedules.document("5").set(
                    hashMapOf(
                        "id" to "5",
                        "studentId" to "2",
                        "userId" to userId,
                        "termNumber" to 2,
                        "termName" to "Term 2 (October)",
                        "termMonth" to "October",
                        "dueDate" to "2026-10-15",
                        "baseAmount" to 6000.0,
                        "paidAmount" to 0.0,
                        "latePenaltyAmount" to 0.0,
                        "isOverdue" to false
                    )
                ).await()

                s2Schedules.document("6").set(
                    hashMapOf(
                        "id" to "6",
                        "studentId" to "2",
                        "userId" to userId,
                        "termNumber" to 3,
                        "termName" to "Term 3 (January)",
                        "termMonth" to "January",
                        "dueDate" to "2027-01-15",
                        "baseAmount" to 6000.0,
                        "paidAmount" to 0.0,
                        "latePenaltyAmount" to 0.0,
                        "isOverdue" to false
                    )
                ).await()

                // Seed initial payments
                val s1Payments = studentsColl.document("1").collection("payments")
                s1Payments.document("1").set(
                    hashMapOf(
                        "id" to "1",
                        "studentId" to "1",
                        "userId" to userId,
                        "scheduleId" to "1",
                        "termName" to "Term 1 (June)",
                        "amount" to 6000.0,
                        "paymentDate" to "2026-06-10",
                        "paymentMode" to "UPI (GPay)",
                        "transactionRef" to "UPI-984218763421",
                        "status" to "VERIFIED",
                        "receiptNumber" to "REC-2026-T1-0182"
                    )
                ).await()

                s1Payments.document("2").set(
                    hashMapOf(
                        "id" to "2",
                        "studentId" to "1",
                        "userId" to userId,
                        "scheduleId" to "2",
                        "termName" to "Term 2 (October)",
                        "amount" to 2000.0,
                        "paymentDate" to "2026-10-02",
                        "paymentMode" to "Net Banking (SBI)",
                        "transactionRef" to "SBI-NET-4512903",
                        "status" to "VERIFIED",
                        "receiptNumber" to "REC-2026-T2-0044"
                    )
                ).await()

                // Seed savings plan
                studentsColl.document("1").collection("savings_plans").document("1").set(
                    hashMapOf(
                        "id" to "1",
                        "studentId" to "1",
                        "userId" to userId,
                        "targetAmount" to 18000.0,
                        "currentSaved" to 3500.0,
                        "monthlyRecommendation" to 1500.0
                    )
                ).await()

                Log.d("FirestoreFeeRepository", "Initial Firestore fee schedules seeded successfully for user $userId")
            }
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "users/$userId/students")
        }
    }

    // Real-time observation of Students
    fun observeStudents(): Flow<List<StudentEntity>> = callbackFlow {
        val uid = try { requireUserId() } catch (e: Exception) {
            close(e)
            return@callbackFlow
        }
        val coll = db.collection("users").document(uid).collection("students")
        val registration: ListenerRegistration = coll.addSnapshotListener { snapshot, error ->
            if (error != null) {
                handleFirestoreError(error, OperationType.LIST, coll.path)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val list = snapshot.documents.mapNotNull { doc ->
                    try {
                        StudentEntity(
                            id = doc.getString("id")?.toLongOrNull() ?: doc.id.toLongOrNull() ?: 1L,
                            name = doc.getString("name") ?: "",
                            grade = doc.getString("grade") ?: "",
                            rollNo = doc.getString("rollNo") ?: "",
                            parentEmail = auth.currentUser?.email ?: "",
                            parentPhone = "+91 98450 12345",
                            annualFee = doc.getDouble("annualFee") ?: 18000.0,
                            avatarInitials = doc.getString("avatarInitials") ?: "ST"
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
                trySend(list)
            }
        }
        awaitClose { registration.remove() }
    }

    // Real-time observation of Fee Schedules for a specific student or all
    fun observeFeeSchedules(studentId: Long): Flow<List<FeeScheduleEntity>> = callbackFlow {
        val uid = try { requireUserId() } catch (e: Exception) {
            close(e)
            return@callbackFlow
        }
        val targetDoc = if (studentId == 0L) "1" else studentId.toString()
        val coll = db.collection("users").document(uid).collection("students").document(targetDoc).collection("fee_schedules")
        val registration = coll.addSnapshotListener { snapshot, error ->
            if (error != null) {
                handleFirestoreError(error, OperationType.LIST, coll.path)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val list = snapshot.documents.mapNotNull { doc ->
                    try {
                        FeeScheduleEntity(
                            id = doc.getString("id")?.toLongOrNull() ?: doc.id.toLongOrNull() ?: 1L,
                            studentId = doc.getString("studentId")?.toLongOrNull() ?: studentId,
                            termNumber = (doc.getLong("termNumber") ?: 1L).toInt(),
                            termName = doc.getString("termName") ?: "Term",
                            termMonth = doc.getString("termMonth") ?: "Month",
                            dueDate = doc.getString("dueDate") ?: "2026-10-15",
                            baseAmount = doc.getDouble("baseAmount") ?: 6000.0,
                            paidAmount = doc.getDouble("paidAmount") ?: 0.0,
                            latePenaltyAmount = doc.getDouble("latePenaltyAmount") ?: 0.0,
                            isOverdue = doc.getBoolean("isOverdue") ?: false
                        )
                    } catch (e: Exception) {
                        null
                    }
                }.sortedBy { it.termNumber }
                trySend(list)
            }
        }
        awaitClose { registration.remove() }
    }

    // Real-time observation of Payments
    fun observePayments(studentId: Long): Flow<List<PaymentEntity>> = callbackFlow {
        val uid = try { requireUserId() } catch (e: Exception) {
            close(e)
            return@callbackFlow
        }
        val targetDoc = if (studentId == 0L) "1" else studentId.toString()
        val coll = db.collection("users").document(uid).collection("students").document(targetDoc).collection("payments")
        val registration = coll.addSnapshotListener { snapshot, error ->
            if (error != null) {
                handleFirestoreError(error, OperationType.LIST, coll.path)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val list = snapshot.documents.mapNotNull { doc ->
                    try {
                        PaymentEntity(
                            id = doc.getString("id")?.toLongOrNull() ?: doc.id.toLongOrNull() ?: 1L,
                            studentId = doc.getString("studentId")?.toLongOrNull() ?: studentId,
                            scheduleId = doc.getString("scheduleId")?.toLongOrNull() ?: 1L,
                            termName = doc.getString("termName") ?: "Term",
                            amount = doc.getDouble("amount") ?: 0.0,
                            paymentDate = doc.getString("paymentDate") ?: "",
                            paymentMode = doc.getString("paymentMode") ?: "UPI",
                            transactionRef = doc.getString("transactionRef") ?: "",
                            status = doc.getString("status") ?: "VERIFIED",
                            receiptNumber = doc.getString("receiptNumber") ?: "REC-001"
                        )
                    } catch (e: Exception) {
                        null
                    }
                }.sortedByDescending { it.id }
                trySend(list)
            }
        }
        awaitClose { registration.remove() }
    }

    // Real-time observation of Savings Pot
    fun observeSavingsPot(studentId: Long): Flow<Double> = callbackFlow {
        val uid = try { requireUserId() } catch (e: Exception) {
            close(e)
            return@callbackFlow
        }
        val targetDoc = if (studentId == 0L) "1" else studentId.toString()
        val docRef = db.collection("users").document(uid).collection("students").document(targetDoc).collection("savings_plans").document("1")
        val registration = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                handleFirestoreError(error, OperationType.GET, docRef.path)
                return@addSnapshotListener
            }
            val amount = snapshot?.getDouble("currentSaved") ?: 3500.0
            trySend(amount)
        }
        awaitClose { registration.remove() }
    }

    // Record Payment in Firestore
    suspend fun recordPayment(
        studentId: Long,
        scheduleId: Long,
        termName: String,
        amount: Double,
        mode: String,
        txnRef: String,
        receiptNo: String
    ) {
        val uid = requireUserId()
        val sId = if (studentId == 0L) "1" else studentId.toString()
        val studentDoc = db.collection("users").document(uid).collection("students").document(sId)
        val paymentDoc = studentDoc.collection("payments").document()

        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val paymentData = hashMapOf(
            "id" to (System.currentTimeMillis() % 1000000).toString(),
            "studentId" to sId,
            "userId" to uid,
            "scheduleId" to scheduleId.toString(),
            "termName" to termName,
            "amount" to amount,
            "paymentDate" to dateStr,
            "paymentMode" to mode,
            "transactionRef" to txnRef,
            "status" to "VERIFIED",
            "receiptNumber" to receiptNo,
            "createdAt" to FieldValue.serverTimestamp()
        )

        try {
            paymentDoc.set(paymentData).await()
            // Increment paidAmount on the schedule document
            val schedDoc = studentDoc.collection("fee_schedules").document(scheduleId.toString())
            schedDoc.update("paidAmount", FieldValue.increment(amount)).await()
            Log.d("FirestoreFeeRepository", "Payment recorded in Firestore: $amount for term $termName")
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, paymentDoc.path)
            throw e
        }
    }

    // Update Savings in Firestore
    suspend fun updateSavings(studentId: Long, newAmount: Double) {
        val uid = requireUserId()
        val sId = if (studentId == 0L) "1" else studentId.toString()
        val planDoc = db.collection("users").document(uid).collection("students").document(sId).collection("savings_plans").document("1")
        try {
            val data = hashMapOf<String, Any>(
                "currentSaved" to newAmount,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            planDoc.set(data, SetOptions.merge()).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, planDoc.path)
        }
    }
}
