package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.entity.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SchoolFeeRepository(private val db: AppDatabase) {

    val allStudents: Flow<List<StudentEntity>> = db.studentDao().getAllStudents()
    val allSchedules: Flow<List<FeeScheduleEntity>> = db.feeScheduleDao().getAllSchedules()
    val allPayments: Flow<List<PaymentEntity>> = db.paymentDao().getAllPayments()
    val allNotifications: Flow<List<NotificationEntity>> = db.notificationDao().getAllNotifications()
    val allScholarships: Flow<List<ScholarshipEntity>> = db.scholarshipDao().getAllScholarships()
    val allSupportRequests: Flow<List<SupportRequestEntity>> = db.supportRequestDao().getAllSupportRequests()
    val currentBudget: Flow<BudgetEntity?> = db.budgetDao().getBudget()
    val allAnnouncements: Flow<List<AnnouncementEntity>> = db.announcementDao().getAllAnnouncements()

    fun getSchedulesForStudent(studentId: Long): Flow<List<FeeScheduleEntity>> =
        db.feeScheduleDao().getSchedulesForStudent(studentId)

    fun getPaymentsForStudent(studentId: Long): Flow<List<PaymentEntity>> =
        db.paymentDao().getPaymentsForStudent(studentId)

    fun getSavingsPlanForStudent(studentId: Long): Flow<SavingsPlanEntity?> =
        db.savingsPlanDao().getSavingsPlan(studentId)

    fun getNotificationsForStudent(studentId: Long): Flow<List<NotificationEntity>> =
        db.notificationDao().getNotificationsForStudent(studentId)

    fun getSupportRequestsForStudent(studentId: Long): Flow<List<SupportRequestEntity>> =
        db.supportRequestDao().getSupportRequestsForStudent(studentId)

    suspend fun recordPayment(
        studentId: Long,
        scheduleId: Long,
        termName: String,
        amount: Double,
        paymentMode: String,
        transactionRef: String,
        receiptNumber: String,
        status: String = "VERIFIED"
    ) = withContext(Dispatchers.IO) {
        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val payment = PaymentEntity(
            studentId = studentId,
            scheduleId = scheduleId,
            termName = termName,
            amount = amount,
            paymentDate = dateStr,
            paymentMode = paymentMode,
            transactionRef = transactionRef,
            status = status,
            receiptNumber = receiptNumber,
            adminNotes = "Recorded via EduFee Mobile App"
        )
        db.paymentDao().insertPayment(payment)
        db.feeScheduleDao().addPaidAmount(scheduleId, amount)
    }

    suspend fun verifyPayment(paymentId: Long, status: String, adminNotes: String) =
        withContext(Dispatchers.IO) {
            db.paymentDao().updatePaymentStatus(paymentId, status, adminNotes)
        }

    suspend fun updateSavings(studentId: Long, newAmount: Double) =
        withContext(Dispatchers.IO) {
            db.savingsPlanDao().updateSavedAmount(studentId, newAmount)
        }

    suspend fun submitSupportRequest(
        studentId: Long,
        studentName: String,
        parentName: String,
        requestType: String,
        requestedExtensionDate: String,
        reason: String
    ) = withContext(Dispatchers.IO) {
        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val request = SupportRequestEntity(
            studentId = studentId,
            studentName = studentName,
            parentName = parentName,
            requestType = requestType,
            requestedExtensionDate = requestedExtensionDate,
            reason = reason,
            status = "PENDING",
            adminRemarks = "Submitted - Awaiting school review",
            createdAt = dateStr
        )
        db.supportRequestDao().insertRequest(request)
    }

    suspend fun updateSupportRequestDecision(id: Long, status: String, adminRemarks: String) =
        withContext(Dispatchers.IO) {
            db.supportRequestDao().updateStatus(id, status, adminRemarks)
        }

    suspend fun updateBudget(
        income: Double,
        rent: Double,
        groceries: Double,
        utilities: Double,
        transport: Double,
        other: Double,
        savings: Double
    ) = withContext(Dispatchers.IO) {
        val entry = BudgetEntity(
            id = 1,
            parentEmail = "parent.sharma@example.com",
            monthlyIncome = income,
            rentExpense = rent,
            groceriesExpense = groceries,
            utilitiesExpense = utilities,
            transportExpense = transport,
            otherExpense = other,
            schoolSavingsTarget = savings
        )
        db.budgetDao().insertOrUpdateBudget(entry)
    }

    suspend fun addAnnouncement(
        title: String,
        titleTamil: String,
        content: String,
        contentTamil: String,
        category: String,
        isImportant: Boolean
    ) = withContext(Dispatchers.IO) {
        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val announcement = AnnouncementEntity(
            title = title,
            titleTamil = titleTamil,
            content = content,
            contentTamil = contentTamil,
            date = dateStr,
            category = category,
            isImportant = isImportant
        )
        db.announcementDao().insertAnnouncement(announcement)
    }

    suspend fun applyForScholarship(scholarshipId: Long) = withContext(Dispatchers.IO) {
        db.scholarshipDao().updateStatus(scholarshipId, "APPLIED")
    }

    suspend fun markNotificationAsRead(id: Long) = withContext(Dispatchers.IO) {
        db.notificationDao().markAsRead(id)
    }

    suspend fun markAllNotificationsAsRead() = withContext(Dispatchers.IO) {
        db.notificationDao().markAllAsRead()
    }

    suspend fun addSibling(name: String, grade: String, rollNo: String) = withContext(Dispatchers.IO) {
        val initials = name.split(" ")
            .mapNotNull { it.firstOrNull()?.toString() }
            .take(2)
            .joinToString("")
            .uppercase()
        val student = StudentEntity(
            name = name,
            grade = grade,
            rollNo = rollNo,
            parentEmail = "parent.sharma@example.com",
            parentPhone = "+91 98450 12345",
            annualFee = 18000.0,
            avatarInitials = if (initials.isNotEmpty()) initials else "ST"
        )
        val studentId = db.studentDao().insertStudent(student)
        val defaultSchedules = listOf(
            FeeScheduleEntity(
                studentId = studentId,
                termNumber = 1,
                termName = "Term 1 (June)",
                termMonth = "June",
                dueDate = "2026-06-15",
                baseAmount = 6000.0,
                paidAmount = 0.0,
                latePenaltyAmount = 0.0,
                isOverdue = false
            ),
            FeeScheduleEntity(
                studentId = studentId,
                termNumber = 2,
                termName = "Term 2 (October)",
                termMonth = "October",
                dueDate = "2026-10-15",
                baseAmount = 6000.0,
                paidAmount = 0.0,
                latePenaltyAmount = 0.0,
                isOverdue = false
            ),
            FeeScheduleEntity(
                studentId = studentId,
                termNumber = 3,
                termName = "Term 3 (January)",
                termMonth = "January",
                dueDate = "2027-01-15",
                baseAmount = 6000.0,
                paidAmount = 0.0,
                latePenaltyAmount = 0.0,
                isOverdue = false
            )
        )
        db.feeScheduleDao().insertSchedules(defaultSchedules)
        db.savingsPlanDao().insertOrUpdateSavingsPlan(
            SavingsPlanEntity(
                studentId = studentId,
                targetAmount = 18000.0,
                currentSaved = 0.0,
                monthlyRecommendation = 1500.0,
                notes = "Monthly target: ₹1,500 over 12 months"
            )
        )
    }
}
