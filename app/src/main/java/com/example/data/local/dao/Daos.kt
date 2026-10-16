package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.AnnouncementEntity
import com.example.data.local.entity.BudgetEntity
import com.example.data.local.entity.FeeScheduleEntity
import com.example.data.local.entity.NotificationEntity
import com.example.data.local.entity.PaymentEntity
import com.example.data.local.entity.SavingsPlanEntity
import com.example.data.local.entity.ScholarshipEntity
import com.example.data.local.entity.StudentEntity
import com.example.data.local.entity.SupportRequestEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {
    @Query("SELECT * FROM students ORDER BY id ASC")
    fun getAllStudents(): Flow<List<StudentEntity>>

    @Query("SELECT * FROM students WHERE id = :id LIMIT 1")
    suspend fun getStudentById(id: Long): StudentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudents(students: List<StudentEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: StudentEntity): Long
}

@Dao
interface FeeScheduleDao {
    @Query("SELECT * FROM fee_schedules WHERE studentId = :studentId ORDER BY termNumber ASC")
    fun getSchedulesForStudent(studentId: Long): Flow<List<FeeScheduleEntity>>

    @Query("SELECT * FROM fee_schedules ORDER BY termNumber ASC")
    fun getAllSchedules(): Flow<List<FeeScheduleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedules(schedules: List<FeeScheduleEntity>)

    @Update
    suspend fun updateSchedule(schedule: FeeScheduleEntity)

    @Query("UPDATE fee_schedules SET paidAmount = paidAmount + :amount WHERE id = :scheduleId")
    suspend fun addPaidAmount(scheduleId: Long, amount: Double)
}

@Dao
interface PaymentDao {
    @Query("SELECT * FROM payments WHERE studentId = :studentId ORDER BY id DESC")
    fun getPaymentsForStudent(studentId: Long): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments ORDER BY id DESC")
    fun getAllPayments(): Flow<List<PaymentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayments(payments: List<PaymentEntity>)

    @Query("UPDATE payments SET status = :status, adminNotes = :adminNotes WHERE id = :paymentId")
    suspend fun updatePaymentStatus(paymentId: Long, status: String, adminNotes: String)
}

@Dao
interface SavingsPlanDao {
    @Query("SELECT * FROM savings_plans WHERE studentId = :studentId LIMIT 1")
    fun getSavingsPlan(studentId: Long): Flow<SavingsPlanEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSavingsPlan(plan: SavingsPlanEntity)

    @Query("UPDATE savings_plans SET currentSaved = :newAmount WHERE studentId = :studentId")
    suspend fun updateSavedAmount(studentId: Long, newAmount: Double)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications WHERE studentId = :studentId OR studentId = 0 ORDER BY id DESC")
    fun getNotificationsForStudent(studentId: Long): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications ORDER BY id DESC")
    fun getAllNotifications(): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<NotificationEntity>)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Long)

    @Query("UPDATE notifications SET isRead = 1")
    suspend fun markAllAsRead()
}

@Dao
interface ScholarshipDao {
    @Query("SELECT * FROM scholarships ORDER BY awardAmount DESC")
    fun getAllScholarships(): Flow<List<ScholarshipEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScholarships(scholarships: List<ScholarshipEntity>)

    @Query("UPDATE scholarships SET applicationStatus = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String)
}

@Dao
interface SupportRequestDao {
    @Query("SELECT * FROM support_requests ORDER BY id DESC")
    fun getAllSupportRequests(): Flow<List<SupportRequestEntity>>

    @Query("SELECT * FROM support_requests WHERE studentId = :studentId ORDER BY id DESC")
    fun getSupportRequestsForStudent(studentId: Long): Flow<List<SupportRequestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequest(request: SupportRequestEntity): Long

    @Query("UPDATE support_requests SET status = :status, adminRemarks = :remarks WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String, remarks: String)
}

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budget_entries LIMIT 1")
    fun getBudget(): Flow<BudgetEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateBudget(budget: BudgetEntity)
}

@Dao
interface AnnouncementDao {
    @Query("SELECT * FROM announcements ORDER BY id DESC")
    fun getAllAnnouncements(): Flow<List<AnnouncementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnouncements(announcements: List<AnnouncementEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnouncement(announcement: AnnouncementEntity): Long
}
