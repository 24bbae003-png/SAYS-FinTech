package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "students")
data class StudentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val grade: String,
    val rollNo: String,
    val parentEmail: String,
    val parentPhone: String,
    val annualFee: Double = 18000.0,
    val avatarInitials: String = "ST"
)

@Entity(tableName = "fee_schedules")
data class FeeScheduleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,
    val termNumber: Int, // 1, 2, 3
    val termName: String, // "Term 1 (June)", "Term 2 (October)", "Term 3 (January)"
    val termMonth: String, // "June", "October", "January"
    val dueDate: String, // "2026-06-15", "2026-10-15", "2027-01-15"
    val baseAmount: Double = 6000.0,
    val paidAmount: Double = 0.0,
    val latePenaltyAmount: Double = 0.0,
    val isOverdue: Boolean = false,
    val academicYear: String = "2026-2027"
)

@Entity(tableName = "payments")
data class PaymentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,
    val scheduleId: Long,
    val termName: String,
    val amount: Double,
    val paymentDate: String,
    val paymentMode: String, // "UPI", "Net Banking", "Debit Card", "Cash / DD"
    val transactionRef: String,
    val status: String, // "VERIFIED", "PENDING_VERIFICATION", "REJECTED"
    val receiptNumber: String,
    val receiptFileName: String = "",
    val adminNotes: String = ""
)

@Entity(tableName = "savings_plans")
data class SavingsPlanEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,
    val targetAmount: Double = 18000.0,
    val currentSaved: Double = 4500.0,
    val monthlyRecommendation: Double = 1500.0,
    val notes: String = "Auto-calculated monthly savings for 2026-2027 school fees"
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,
    val title: String,
    val message: String,
    val daysBefore: Int, // 30, 15, 7, 3, 0 (due date), -1 (overdue)
    val dateString: String,
    val isRead: Boolean = false,
    val priority: String = "HIGH" // "HIGH", "MEDIUM", "LOW"
)

@Entity(tableName = "scholarships")
data class ScholarshipEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val titleTamil: String,
    val provider: String,
    val awardAmount: Double,
    val eligibility: String,
    val eligibilityTamil: String,
    val deadline: String,
    val applicationStatus: String = "AVAILABLE", // "AVAILABLE", "APPLIED", "UNDER_REVIEW", "AWARDED"
    val applicationUrl: String = "https://scholarships.gov.in"
)

@Entity(tableName = "support_requests")
data class SupportRequestEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,
    val studentName: String,
    val parentName: String,
    val requestType: String, // "EXTENSION", "CUSTOM_INSTALLMENT", "HARDSHIP_DISCOUNT"
    val requestedExtensionDate: String,
    val reason: String,
    val status: String = "PENDING", // "PENDING", "APPROVED", "REJECTED"
    val adminRemarks: String = "",
    val createdAt: String
)

@Entity(tableName = "budget_entries")
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val parentEmail: String,
    val monthlyIncome: Double = 45000.0,
    val rentExpense: Double = 12000.0,
    val groceriesExpense: Double = 9000.0,
    val utilitiesExpense: Double = 4000.0,
    val transportExpense: Double = 3500.0,
    val otherExpense: Double = 3500.0,
    val schoolSavingsTarget: Double = 3000.0
)

@Entity(tableName = "announcements")
data class AnnouncementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val titleTamil: String,
    val content: String,
    val contentTamil: String,
    val date: String,
    val category: String, // "FEE_UPDATE", "CIRCULAR", "GENERAL"
    val isImportant: Boolean = false
)
