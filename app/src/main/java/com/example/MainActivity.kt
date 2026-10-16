package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.remote.FirestoreManager
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.EduFeeTheme
import com.example.ui.viewmodel.FeeViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EduFeeTheme {
                AppRoot()
            }
        }
    }
}

@Composable
fun AppRoot() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }
    var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose {
            Firebase.auth.removeAuthStateListener(listener)
        }
    }

    if (currentUser == null) {
        AuthScreen(
            onAuthSuccess = {
                currentUser = Firebase.auth.currentUser
            }
        )
    } else {
        MainApp(
            onSignOut = {
                signOutGoogle(
                    context = context,
                    credentialManager = credentialManager,
                    onSignOutComplete = { currentUser = null },
                    scope = coroutineScope
                )
            }
        )
    }
}

@Composable
fun MainApp(
    viewModel: FeeViewModel = viewModel(),
    onSignOut: () -> Unit
) {
    val context = LocalContext.current
    val language by viewModel.language.collectAsState()
    val isAdminMode by viewModel.isAdminMode.collectAsState()
    val students by viewModel.students.collectAsState()
    val selectedStudentId by viewModel.selectedStudentId.collectAsState()
    val unreadNotifications by viewModel.unreadNotificationsCount.collectAsState()
    val notifications by viewModel.allNotifications.collectAsState()
    val schedules by viewModel.activeSchedules.collectAsState()
    val payments by viewModel.activePayments.collectAsState()

    var currentTab by remember { mutableStateOf(NavigationTab.DASHBOARD) }
    var showAddSiblingDialog by remember { mutableStateOf(false) }
    var showRemindersDialog by remember { mutableStateOf(false) }
    var servicesInitialTab by remember { mutableStateOf(ServiceSubTab.BUDGET) }

    // Sync data to Firestore in the background for authenticated user
    val firestoreManager = remember { FirestoreManager(context) }
    LaunchedEffect(students) {
        firestoreManager.syncUserProfile()
        students.forEach { student ->
            firestoreManager.syncStudent(student)
        }
    }
    LaunchedEffect(schedules) {
        schedules.forEach { schedule ->
            firestoreManager.syncFeeSchedule(schedule)
        }
    }
    LaunchedEffect(payments) {
        payments.forEach { payment ->
            firestoreManager.syncPayment(payment)
        }
    }

    // Handle system back navigation
    BackHandler(enabled = isAdminMode || currentTab != NavigationTab.DASHBOARD) {
        if (isAdminMode) {
            viewModel.toggleAdminMode()
        } else {
            currentTab = NavigationTab.DASHBOARD
        }
    }

    androidx.compose.foundation.layout.BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isExpandedScreen = maxWidth >= 600.dp

        androidx.compose.foundation.layout.Row(modifier = Modifier.fillMaxSize()) {
            if (isExpandedScreen && !isAdminMode) {
                EduFeeNavigationRail(
                    currentTab = currentTab,
                    onTabSelected = { currentTab = it },
                    language = language
                )
            }

            Scaffold(
                modifier = Modifier.weight(1f),
                topBar = {
                    EduFeeTopBar(
                        language = language,
                        isAdminMode = isAdminMode,
                        students = students,
                        selectedStudentId = selectedStudentId,
                        unreadNotifications = unreadNotifications,
                        onLanguageToggle = { viewModel.toggleLanguage() },
                        onAdminToggle = { viewModel.toggleAdminMode() },
                        onStudentSelected = { viewModel.setSelectedStudentId(it) },
                        onAddSiblingClick = { showAddSiblingDialog = true },
                        onNotificationsClick = { showRemindersDialog = true },
                        onSignOutClick = onSignOut
                    )
                },
                bottomBar = {
                    if (!isExpandedScreen && !isAdminMode) {
                        EduFeeBottomNav(
                            currentTab = currentTab,
                            onTabSelected = { currentTab = it },
                            language = language
                        )
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (isAdminMode) {
                        AdminDashboardScreen(viewModel = viewModel)
                    } else {
                        when (currentTab) {
                            NavigationTab.DASHBOARD -> {
                                ParentDashboardScreen(
                                    viewModel = viewModel,
                                    onNavigateToTab = { currentTab = it },
                                    onRequestExtensionClick = {
                                        servicesInitialTab = ServiceSubTab.HARDSHIP
                                        currentTab = NavigationTab.SERVICES
                                    }
                                )
                            }
                            NavigationTab.SCHEDULE -> {
                                FeeScheduleScreen(viewModel = viewModel)
                            }
                            NavigationTab.SAVINGS -> {
                                SavingsPlannerScreen(viewModel = viewModel)
                            }
                            NavigationTab.HISTORY -> {
                                PaymentHistoryScreen(viewModel = viewModel)
                            }
                            NavigationTab.SERVICES -> {
                                ServicesAndBudgetScreen(
                                    viewModel = viewModel,
                                    initialTab = servicesInitialTab
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddSiblingDialog) {
        AddSiblingDialog(
            language = language,
            onDismiss = { showAddSiblingDialog = false },
            onAdd = { name, grade, rollNo ->
                viewModel.addSibling(name, grade, rollNo)
            }
        )
    }

    if (showRemindersDialog) {
        RemindersDialog(
            notifications = notifications,
            language = language,
            onDismiss = { showRemindersDialog = false },
            onMarkRead = { viewModel.markNotificationAsRead(it) },
            onMarkAllRead = { viewModel.markAllNotificationsAsRead() }
        )
    }
}
