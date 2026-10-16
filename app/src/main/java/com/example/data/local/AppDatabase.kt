package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.*
import com.example.data.local.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        StudentEntity::class,
        FeeScheduleEntity::class,
        PaymentEntity::class,
        SavingsPlanEntity::class,
        NotificationEntity::class,
        ScholarshipEntity::class,
        SupportRequestEntity::class,
        BudgetEntity::class,
        AnnouncementEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun studentDao(): StudentDao
    abstract fun feeScheduleDao(): FeeScheduleDao
    abstract fun paymentDao(): PaymentDao
    abstract fun savingsPlanDao(): SavingsPlanDao
    abstract fun notificationDao(): NotificationDao
    abstract fun scholarshipDao(): ScholarshipDao
    abstract fun supportRequestDao(): SupportRequestDao
    abstract fun budgetDao(): BudgetDao
    abstract fun announcementDao(): AnnouncementDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "edufee_school_db"
                )
                .fallbackToDestructiveMigration()
                .addCallback(DatabaseCallback())
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database)
                    }
                }
            }
        }

        suspend fun populateInitialData(db: AppDatabase) {
            // Sibling students
            val students = listOf(
                StudentEntity(
                    id = 1,
                    name = "Aarav Sharma",
                    grade = "Grade 7-A",
                    rollNo = "SEC-2026-0742",
                    parentEmail = "parent.sharma@example.com",
                    parentPhone = "+91 98450 12345",
                    annualFee = 18000.0,
                    avatarInitials = "AS"
                ),
                StudentEntity(
                    id = 2,
                    name = "Diya Sharma",
                    grade = "Grade 4-B",
                    rollNo = "PRI-2026-0418",
                    parentEmail = "parent.sharma@example.com",
                    parentPhone = "+91 98450 12345",
                    annualFee = 18000.0,
                    avatarInitials = "DS"
                )
            )
            db.studentDao().insertStudents(students)

            // Fee Schedules for Student 1 (Aarav): 3 Terms x ₹6,000 = ₹18,000
            // Term 1: Paid ₹6,000
            // Term 2: Partial Paid ₹2,000 (₹4,000 pending)
            // Term 3: Pending ₹6,000
            val schedules1 = listOf(
                FeeScheduleEntity(
                    id = 1,
                    studentId = 1,
                    termNumber = 1,
                    termName = "Term 1 (June)",
                    termMonth = "June",
                    dueDate = "2026-06-15",
                    baseAmount = 6000.0,
                    paidAmount = 6000.0,
                    latePenaltyAmount = 0.0,
                    isOverdue = false
                ),
                FeeScheduleEntity(
                    id = 2,
                    studentId = 1,
                    termNumber = 2,
                    termName = "Term 2 (October)",
                    termMonth = "October",
                    dueDate = "2026-10-15",
                    baseAmount = 6000.0,
                    paidAmount = 2000.0,
                    latePenaltyAmount = 0.0,
                    isOverdue = false
                ),
                FeeScheduleEntity(
                    id = 3,
                    studentId = 1,
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

            // Fee Schedules for Student 2 (Diya)
            val schedules2 = listOf(
                FeeScheduleEntity(
                    id = 4,
                    studentId = 2,
                    termNumber = 1,
                    termName = "Term 1 (June)",
                    termMonth = "June",
                    dueDate = "2026-06-15",
                    baseAmount = 6000.0,
                    paidAmount = 6000.0,
                    latePenaltyAmount = 0.0,
                    isOverdue = false
                ),
                FeeScheduleEntity(
                    id = 5,
                    studentId = 2,
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
                    id = 6,
                    studentId = 2,
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
            db.feeScheduleDao().insertSchedules(schedules1 + schedules2)

            // Payments
            val payments = listOf(
                PaymentEntity(
                    id = 1,
                    studentId = 1,
                    scheduleId = 1,
                    termName = "Term 1 (June)",
                    amount = 6000.0,
                    paymentDate = "2026-06-10",
                    paymentMode = "UPI (GPay)",
                    transactionRef = "UPI-984218763421",
                    status = "VERIFIED",
                    receiptNumber = "REC-2026-T1-0182",
                    adminNotes = "Verified by School Accounts Desk"
                ),
                PaymentEntity(
                    id = 2,
                    studentId = 1,
                    scheduleId = 2,
                    termName = "Term 2 (October)",
                    amount = 2000.0,
                    paymentDate = "2026-10-02",
                    paymentMode = "Net Banking (SBI)",
                    transactionRef = "SBI-NET-4512903",
                    status = "VERIFIED",
                    receiptNumber = "REC-2026-T2-0044",
                    adminNotes = "Part payment received"
                ),
                PaymentEntity(
                    id = 3,
                    studentId = 2,
                    scheduleId = 4,
                    termName = "Term 1 (June)",
                    amount = 6000.0,
                    paymentDate = "2026-06-12",
                    paymentMode = "Debit Card",
                    transactionRef = "CARD-AUTH-887123",
                    status = "VERIFIED",
                    receiptNumber = "REC-2026-T1-0205",
                    adminNotes = "Verified payment"
                )
            )
            db.paymentDao().insertPayments(payments)

            // Savings Plans
            db.savingsPlanDao().insertOrUpdateSavingsPlan(
                SavingsPlanEntity(
                    id = 1,
                    studentId = 1,
                    targetAmount = 18000.0,
                    currentSaved = 3500.0,
                    monthlyRecommendation = 1500.0,
                    notes = "Monthly target: ₹1,500 over 12 months"
                )
            )
            db.savingsPlanDao().insertOrUpdateSavingsPlan(
                SavingsPlanEntity(
                    id = 2,
                    studentId = 2,
                    targetAmount = 18000.0,
                    currentSaved = 2500.0,
                    monthlyRecommendation = 1500.0,
                    notes = "Monthly target: ₹1,500 over 12 months"
                )
            )

            // Reminders / Notifications (30, 15, 7, 3 days before, on due date, overdue)
            val notifications = listOf(
                NotificationEntity(
                    id = 1,
                    studentId = 1,
                    title = "Term 2 Fee Due in 7 Days",
                    message = "Reminder: Remaining balance of ₹4,000 for Term 2 is due on 15 Oct 2026.",
                    daysBefore = 7,
                    dateString = "2026-10-08",
                    isRead = false,
                    priority = "HIGH"
                ),
                NotificationEntity(
                    id = 2,
                    studentId = 1,
                    title = "Term 2 Reminder (15 Days)",
                    message = "Friendly notice: Term 2 installment due date is approaching (15 Oct 2026).",
                    daysBefore = 15,
                    dateString = "2026-10-01",
                    isRead = true,
                    priority = "MEDIUM"
                ),
                NotificationEntity(
                    id = 3,
                    studentId = 2,
                    title = "Term 2 Fee Due in 7 Days",
                    message = "Diya's Term 2 fee of ₹6,000 is due on 15 Oct 2026. Pay early to avoid late fees.",
                    daysBefore = 7,
                    dateString = "2026-10-08",
                    isRead = false,
                    priority = "HIGH"
                ),
                NotificationEntity(
                    id = 4,
                    studentId = 1,
                    title = "Term 3 Advance Alert (30 Days)",
                    message = "Plan ahead: Term 3 installment of ₹6,000 will be due in January 2027.",
                    daysBefore = 30,
                    dateString = "2026-12-15",
                    isRead = false,
                    priority = "LOW"
                )
            )
            db.notificationDao().insertNotifications(notifications)

            // Scholarships
            val scholarships = listOf(
                ScholarshipEntity(
                    id = 1,
                    title = "Merit-Cum-Means State Scholarship",
                    titleTamil = "தகுதி மற்றும் வருமான அடிப்படையிலான உதவித்தொகை",
                    provider = "Department of School Education",
                    awardAmount = 8000.0,
                    eligibility = "Annual family income < ₹2,50,000; Minimum 75% marks in previous year.",
                    eligibilityTamil = "ஆண்டு குடும்ப வருமானம் < ₹2,50,000; முந்தைய ஆண்டு 75% மதிப்பெண்.",
                    deadline = "2026-11-30",
                    applicationStatus = "AVAILABLE",
                    applicationUrl = "https://scholarships.gov.in"
                ),
                ScholarshipEntity(
                    id = 2,
                    title = "Single Girl Child Education Grant",
                    titleTamil = "ஒற்றைப் பெண் குழந்தை கல்வி உதவித்தொகை",
                    provider = "Tamil Nadu Social Welfare Board",
                    awardAmount = 10000.0,
                    eligibility = "Only girl child in family studying in recognized schools.",
                    eligibilityTamil = "அங்கீகரிக்கப்பட்ட பள்ளிகளில் படிக்கும் குடும்பத்தின் ஒரே பெண் குழந்தை.",
                    deadline = "2026-12-15",
                    applicationStatus = "AVAILABLE",
                    applicationUrl = "https://tnsocialwelfare.tn.gov.in"
                ),
                ScholarshipEntity(
                    id = 3,
                    title = "STEM & Sports Excellence Bursary",
                    titleTamil = "விளையாட்டு மற்றும் அறிவியல் சிறப்பு நிதியுதவி",
                    provider = "School Alumni Foundation",
                    awardAmount = 6000.0,
                    eligibility = "District or state level sports / science olympiad qualifiers.",
                    eligibilityTamil = "மாவட்ட அல்லது மாநில அளவிலான விளையாட்டு / அறிவியல் வெற்றியாளர்கள்.",
                    deadline = "2026-11-15",
                    applicationStatus = "APPLIED",
                    applicationUrl = "https://alumni.school.edu"
                )
            )
            db.scholarshipDao().insertScholarships(scholarships)

            // Support Requests
            db.supportRequestDao().insertRequest(
                SupportRequestEntity(
                    id = 1,
                    studentId = 1,
                    studentName = "Aarav Sharma",
                    parentName = "Ramesh Sharma",
                    requestType = "EXTENSION",
                    requestedExtensionDate = "2026-11-15",
                    reason = "Salary delay in current month due to company transition.",
                    status = "APPROVED",
                    adminRemarks = "Granted 30-day grace period with zero late fees.",
                    createdAt = "2026-10-05"
                )
            )

            // Budget
            db.budgetDao().insertOrUpdateBudget(
                BudgetEntity(
                    id = 1,
                    parentEmail = "parent.sharma@example.com",
                    monthlyIncome = 50000.0,
                    rentExpense = 14000.0,
                    groceriesExpense = 10000.0,
                    utilitiesExpense = 4500.0,
                    transportExpense = 4000.0,
                    otherExpense = 3500.0,
                    schoolSavingsTarget = 3000.0 // ₹1,500 x 2 kids
                )
            )

            // Announcements / Fee Increase Alerts
            val announcements = listOf(
                AnnouncementEntity(
                    id = 1,
                    title = "Fee Schedule & Academic Calendar 2026-27",
                    titleTamil = "கட்டண அட்டவணை மற்றும் கல்வி காலண்டர் 2026-27",
                    content = "The annual fee of ₹18,000 is divided into 3 installments (Term 1: ₹6,000 June, Term 2: ₹6,000 October, Term 3: ₹6,000 January). Late fee grace period is 7 days.",
                    contentTamil = "ஆண்டு கட்டணம் ₹18,000 மூன்று தவணைகளாக பிரிக்கப்பட்டுள்ளது (தவணை 1: ₹6,000 ஜூன், தவணை 2: ₹6,000 அக்டோபர், தவணை 3: ₹6,000 ஜனவரி). தாமத கட்டண சலுகை 7 நாட்கள்.",
                    date = "2026-06-01",
                    category = "FEE_UPDATE",
                    isImportant = true
                ),
                AnnouncementEntity(
                    id = 2,
                    title = "Notice on Digital Payment Receipts",
                    titleTamil = "டிஜிட்டல் கட்டண ரசீதுகள் பற்றிய அறிவிப்பு",
                    content = "Parents paying via UPI / Net Banking can upload transaction UTR details to receive instant school verified digital receipts.",
                    contentTamil = "யுபிஐ அல்லது நெட் பேங்கிங் மூலம் செலுத்தும் பெற்றோர்கள் டிஜிட்டல் ரசீது பெற பரிவர்த்தனை குறிப்பு எண்ணை உள்ளிடலாம்.",
                    date = "2026-08-15",
                    category = "CIRCULAR",
                    isImportant = false
                ),
                AnnouncementEntity(
                    id = 3,
                    title = "No Fee Increase for Term 2 & Term 3",
                    titleTamil = "தவணை 2 மற்றும் 3க்கு கட்டண உயர்வு இல்லை",
                    content = "The School Management Board has ratified that no interim fee hikes will be implemented for the current academic session.",
                    contentTamil = "நடப்பு கல்வியாண்டில் பள்ளி நிர்வாகக் குழு கட்டண உயர்வு ஏதும் செய்யப்போவதில்லை என்று உறுதி செய்துள்ளது.",
                    date = "2026-09-20",
                    category = "FEE_UPDATE",
                    isImportant = true
                )
            )
            db.announcementDao().insertAnnouncements(announcements)
        }
    }
}
