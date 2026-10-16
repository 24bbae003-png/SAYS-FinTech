package com.example.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entity.*
import com.example.data.remote.FirestoreFeeRepository
import com.example.data.repository.SchoolFeeRepository
import com.example.util.AppLanguage
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.max

class FeeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SchoolFeeRepository
    private val firestoreRepository: FirestoreFeeRepository

    init {
        val db = AppDatabase.getDatabase(application)
        repository = SchoolFeeRepository(db)
        firestoreRepository = FirestoreFeeRepository(application)
    }

    private val _language = MutableStateFlow(AppLanguage.ENGLISH)
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    private val _isAdminMode = MutableStateFlow(false)
    val isAdminMode: StateFlow<Boolean> = _isAdminMode.asStateFlow()

    private val _selectedStudentId = MutableStateFlow<Long>(1L) // Default to first child Aarav
    val selectedStudentId: StateFlow<Long> = _selectedStudentId.asStateFlow()

    private val _isFirestoreConnected = MutableStateFlow(Firebase.auth.currentUser != null)
    val isFirestoreConnected: StateFlow<Boolean> = _isFirestoreConnected.asStateFlow()

    // Firestore streams state
    private val _firestoreStudents = MutableStateFlow<List<StudentEntity>>(emptyList())
    private val _firestoreSchedules = MutableStateFlow<List<FeeScheduleEntity>>(emptyList())
    private val _firestorePayments = MutableStateFlow<List<PaymentEntity>>(emptyList())
    private val _firestoreSavings = MutableStateFlow<Double>(3500.0)

    init {
        // Observe auth state changes to connect/disconnect Firestore real-time streams
        Firebase.auth.addAuthStateListener { auth ->
            val user = auth.currentUser
            _isFirestoreConnected.value = user != null
            if (user != null) {
                viewModelScope.launch {
                    try {
                        firestoreRepository.seedInitialDataIfEmpty(user.uid, user.email ?: "")
                        // Connect real-time Firestore observation
                        firestoreRepository.observeStudents().collect { list ->
                            if (list.isNotEmpty()) _firestoreStudents.value = list
                        }
                    } catch (e: Exception) {
                        Log.e("FeeViewModel", "Error connecting to Firestore students", e)
                    }
                }
            }
        }

        // Real-time schedules from Firestore based on active child
        viewModelScope.launch {
            combine(_selectedStudentId, _isFirestoreConnected) { id, connected ->
                Pair(id, connected)
            }.collect { (id, connected) ->
                if (connected && Firebase.auth.currentUser != null) {
                    try {
                        firestoreRepository.observeFeeSchedules(id).collect { list ->
                            if (list.isNotEmpty()) _firestoreSchedules.value = list
                        }
                    } catch (e: Exception) {
                        Log.e("FeeViewModel", "Error observing Firestore schedules", e)
                    }
                }
            }
        }

        // Real-time payments from Firestore based on active child
        viewModelScope.launch {
            combine(_selectedStudentId, _isFirestoreConnected) { id, connected ->
                Pair(id, connected)
            }.collect { (id, connected) ->
                if (connected && Firebase.auth.currentUser != null) {
                    try {
                        firestoreRepository.observePayments(id).collect { list ->
                            _firestorePayments.value = list
                        }
                    } catch (e: Exception) {
                        Log.e("FeeViewModel", "Error observing Firestore payments", e)
                    }
                }
            }
        }

        // Real-time savings from Firestore
        viewModelScope.launch {
            combine(_selectedStudentId, _isFirestoreConnected) { id, connected ->
                Pair(id, connected)
            }.collect { (id, connected) ->
                if (connected && Firebase.auth.currentUser != null) {
                    try {
                        firestoreRepository.observeSavingsPot(id).collect { amount ->
                            _firestoreSavings.value = amount
                        }
                    } catch (e: Exception) {
                        Log.e("FeeViewModel", "Error observing Firestore savings", e)
                    }
                }
            }
        }
    }

    // Students: prefer live Firestore students if available, fallback to local Room
    val students: StateFlow<List<StudentEntity>> = combine(
        repository.allStudents,
        _firestoreStudents
    ) { local, remote ->
        if (remote.isNotEmpty()) remote else local
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSchedules: StateFlow<List<FeeScheduleEntity>> = combine(
        repository.allSchedules,
        _firestoreSchedules
    ) { local, remote ->
        if (remote.isNotEmpty()) remote else local
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPayments: StateFlow<List<PaymentEntity>> = combine(
        repository.allPayments,
        _firestorePayments
    ) { local, remote ->
        if (remote.isNotEmpty()) remote else local
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allNotifications: StateFlow<List<NotificationEntity>> = repository.allNotifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val scholarships: StateFlow<List<ScholarshipEntity>> = repository.allScholarships
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val supportRequests: StateFlow<List<SupportRequestEntity>> = repository.allSupportRequests
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val budget: StateFlow<BudgetEntity?> = repository.currentBudget
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val announcements: StateFlow<List<AnnouncementEntity>> = repository.allAnnouncements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active student's schedules or combined if 0L
    val activeSchedules: StateFlow<List<FeeScheduleEntity>> = combine(
        allSchedules,
        _selectedStudentId
    ) { schedules, studentId ->
        if (studentId == 0L) schedules else schedules.filter { it.studentId == studentId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active student's payments
    val activePayments: StateFlow<List<PaymentEntity>> = combine(
        allPayments,
        _selectedStudentId
    ) { payments, studentId ->
        if (studentId == 0L) payments else payments.filter { it.studentId == studentId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Total Annual Fee fetched from students
    val totalAnnualFee: StateFlow<Double> = combine(
        students,
        _selectedStudentId
    ) { studentList, studentId ->
        if (studentId == 0L) {
            studentList.sumOf { it.annualFee }.coerceAtLeast(18000.0)
        } else {
            studentList.find { it.id == studentId }?.annualFee ?: 18000.0
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 18000.0)

    // Total Paid Fees calculated from live fee schedules
    val totalPaidFees: StateFlow<Double> = activeSchedules.map { schedules ->
        schedules.sumOf { it.paidAmount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 8000.0)

    // Total Pending Fees: Total Annual - Total Paid
    val totalPendingFees: StateFlow<Double> = combine(
        totalAnnualFee,
        totalPaidFees
    ) { annual, paid ->
        max(0.0, annual - paid)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 10000.0)

    // Next upcoming unpaid deadline from Firestore fee schedule
    val nextUpcomingSchedule: StateFlow<FeeScheduleEntity?> = activeSchedules.map { schedules ->
        schedules.firstOrNull { it.paidAmount < it.baseAmount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Live savings pot from Firestore
    val currentSavingsPot: StateFlow<Double> = _firestoreSavings.asStateFlow()

    // Dynamic Monthly Savings Recommendation:
    // (Total Pending Fees - Current Savings) / Months Remaining (e.g. 8 months)
    val recommendedMonthlySavings: StateFlow<Double> = combine(
        totalPendingFees,
        currentSavingsPot,
        _selectedStudentId
    ) { pending, saved, studentId ->
        val effectivePending = max(0.0, pending - saved)
        val monthsLeft = 8.0 // October to May
        val target = if (studentId == 0L) {
            (effectivePending / monthsLeft)
        } else {
            (effectivePending / monthsLeft).coerceAtLeast(1500.0)
        }
        (target / 100).toInt() * 100.0
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1500.0)

    // Days until next due date
    val daysUntilNextDue: StateFlow<Long> = nextUpcomingSchedule.map { schedule ->
        if (schedule == null) return@map 0L
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dueDate = sdf.parse(schedule.dueDate) ?: Date()
            val today = Date()
            val diff = dueDate.time - today.time
            TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS).coerceAtLeast(0L)
        } catch (e: Exception) {
            7L
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 7L)

    val unreadNotificationsCount: StateFlow<Int> = allNotifications.map { list ->
        list.count { !it.isRead }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun toggleLanguage() {
        _language.value = if (_language.value == AppLanguage.ENGLISH) {
            AppLanguage.TAMIL
        } else {
            AppLanguage.ENGLISH
        }
    }

    fun toggleAdminMode() {
        _isAdminMode.value = !_isAdminMode.value
    }

    fun setSelectedStudentId(id: Long) {
        _selectedStudentId.value = id
    }

    fun recordPayment(
        scheduleId: Long,
        amount: Double,
        mode: String,
        txnRef: String
    ) {
        viewModelScope.launch {
            val schedule = allSchedules.value.find { it.id == scheduleId }
            val studentId = schedule?.studentId ?: _selectedStudentId.value
            val termName = schedule?.termName ?: "Installment"
            val receiptNo = "REC-${System.currentTimeMillis() % 100000}"

            // 1. Record in local DB
            repository.recordPayment(
                studentId = studentId,
                scheduleId = scheduleId,
                termName = termName,
                amount = amount,
                paymentMode = mode,
                transactionRef = txnRef,
                receiptNumber = receiptNo,
                status = "VERIFIED"
            )

            // 2. Persist to Firestore if authenticated
            if (Firebase.auth.currentUser != null) {
                try {
                    firestoreRepository.recordPayment(
                        studentId = studentId,
                        scheduleId = scheduleId,
                        termName = termName,
                        amount = amount,
                        mode = mode,
                        txnRef = txnRef,
                        receiptNo = receiptNo
                    )
                } catch (e: Exception) {
                    Log.e("FeeViewModel", "Failed to sync payment to Firestore", e)
                }
            }
        }
    }

    fun depositSavings(amount: Double) {
        viewModelScope.launch {
            val studentId = if (_selectedStudentId.value == 0L) 1L else _selectedStudentId.value
            val current = currentSavingsPot.value
            val newAmount = current + amount
            repository.updateSavings(studentId, newAmount)
            if (Firebase.auth.currentUser != null) {
                firestoreRepository.updateSavings(studentId, newAmount)
            }
        }
    }

    fun withdrawSavings(amount: Double) {
        viewModelScope.launch {
            val studentId = if (_selectedStudentId.value == 0L) 1L else _selectedStudentId.value
            val current = currentSavingsPot.value
            val newAmount = max(0.0, current - amount)
            repository.updateSavings(studentId, newAmount)
            if (Firebase.auth.currentUser != null) {
                firestoreRepository.updateSavings(studentId, newAmount)
            }
        }
    }

    fun submitSupportRequest(
        type: String,
        requestedDate: String,
        reason: String
    ) {
        viewModelScope.launch {
            val student = students.value.find { it.id == _selectedStudentId.value }
                ?: students.value.firstOrNull()
            repository.submitSupportRequest(
                studentId = student?.id ?: 1L,
                studentName = student?.name ?: "Student",
                parentName = "Ramesh Sharma",
                requestType = type,
                requestedExtensionDate = requestedDate,
                reason = reason
            )
        }
    }

    fun updateSupportRequestDecision(id: Long, status: String, remarks: String) {
        viewModelScope.launch {
            repository.updateSupportRequestDecision(id, status, remarks)
        }
    }

    fun verifyPayment(paymentId: Long, status: String, adminNotes: String) {
        viewModelScope.launch {
            repository.verifyPayment(paymentId, status, adminNotes)
        }
    }

    fun updateBudget(
        income: Double,
        rent: Double,
        groceries: Double,
        utilities: Double,
        transport: Double,
        other: Double,
        schoolSavings: Double
    ) {
        viewModelScope.launch {
            repository.updateBudget(income, rent, groceries, utilities, transport, other, schoolSavings)
        }
    }

    fun addAnnouncement(
        title: String,
        titleTamil: String,
        content: String,
        contentTamil: String,
        category: String,
        isImportant: Boolean
    ) {
        viewModelScope.launch {
            repository.addAnnouncement(title, titleTamil, content, contentTamil, category, isImportant)
        }
    }

    fun addSibling(name: String, grade: String, rollNo: String) {
        viewModelScope.launch {
            repository.addSibling(name, grade, rollNo)
        }
    }

    fun applyForScholarship(scholarshipId: Long) {
        viewModelScope.launch {
            repository.applyForScholarship(scholarshipId)
        }
    }

    fun markNotificationAsRead(id: Long) {
        viewModelScope.launch {
            repository.markNotificationAsRead(id)
        }
    }

    fun markAllNotificationsAsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsAsRead()
        }
    }
}
