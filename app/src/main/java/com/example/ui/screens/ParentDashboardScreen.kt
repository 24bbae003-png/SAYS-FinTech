package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.entity.FeeScheduleEntity
import com.example.data.local.entity.PaymentEntity
import com.example.data.local.entity.StudentEntity
import com.example.ui.components.DigitalReceiptDialog
import com.example.ui.components.NavigationTab
import com.example.ui.components.PaymentDialog
import com.example.ui.theme.*
import com.example.ui.viewmodel.FeeViewModel
import com.example.util.AppLanguage
import com.example.util.AppStrings

@Composable
fun ParentDashboardScreen(
    viewModel: FeeViewModel,
    onNavigateToTab: (NavigationTab) -> Unit,
    onRequestExtensionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val language by viewModel.language.collectAsState()
    val totalAnnual by viewModel.totalAnnualFee.collectAsState()
    val totalPaid by viewModel.totalPaidFees.collectAsState()
    val totalPending by viewModel.totalPendingFees.collectAsState()
    val nextSchedule by viewModel.nextUpcomingSchedule.collectAsState()
    val daysUntilDue by viewModel.daysUntilNextDue.collectAsState()
    val recommendedSavings by viewModel.recommendedMonthlySavings.collectAsState()
    val currentSaved by viewModel.currentSavingsPot.collectAsState()
    val schedules by viewModel.activeSchedules.collectAsState()
    val payments by viewModel.activePayments.collectAsState()
    val announcements by viewModel.announcements.collectAsState()
    val students by viewModel.students.collectAsState()
    val selectedStudentId by viewModel.selectedStudentId.collectAsState()

    val isFirestoreConnected by viewModel.isFirestoreConnected.collectAsState()

    var showPaymentDialog by remember { mutableStateOf(false) }
    var selectedScheduleForPay by remember { mutableStateOf<Long?>(null) }
    var selectedPaymentForReceipt by remember { mutableStateOf<PaymentEntity?>(null) }

    val activeStudent = students.find { it.id == selectedStudentId }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Slate50),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // Hero Visual Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.school_fees_banner),
                    contentDescription = "Education Savings Banner",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                // Gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Navy900.copy(alpha = 0.85f))
                            )
                        )
                )
                // Banner text
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            color = Teal500,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "ACADEMIC YEAR 2026-2027",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Navy900,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Surface(
                            color = if (isFirestoreConnected) EmeraldGreenContainer else AmberContainer,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (isFirestoreConnected) EmeraldGreen else AmberWarning)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isFirestoreConnected) "Cloud Synced (Firestore)" else "Offline Cache",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isFirestoreConnected) EmeraldGreen else Slate800
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (activeStudent != null) "${activeStudent.name}'s Fee Overview" else "All Children Family Fee Plan",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Real-time fee schedule, payment status & upcoming deadlines",
                        fontSize = 12.sp,
                        color = Slate200
                    )
                }
            }
        }

        // Summary Metric Cards
        item {
            Column(modifier = Modifier.padding(16.dp)) {
                // Top Row: Total Annual & Paid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = AppStrings.getString("total_annual_fee", language),
                        amount = "₹${totalAnnual.toInt()}",
                        subtitle = "3 Terms (₹6,000 each)",
                        icon = Icons.Default.AccountBalance,
                        iconTint = Navy800,
                        backgroundColor = Color.White,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = AppStrings.getString("paid_fees", language),
                        amount = "₹${totalPaid.toInt()}",
                        subtitle = "${((totalPaid / totalAnnual.coerceAtLeast(1.0)) * 100).toInt()}% Cleared",
                        icon = Icons.Default.CheckCircle,
                        iconTint = EmeraldGreen,
                        backgroundColor = Color.White,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom Row: Pending & Next Due
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = AppStrings.getString("pending_fees", language),
                        amount = "₹${totalPending.toInt()}",
                        subtitle = if (totalPending > 0) "Next Term Pending" else "Fully Settled",
                        icon = Icons.Default.PendingActions,
                        iconTint = if (totalPending > 0) AmberWarning else EmeraldGreen,
                        backgroundColor = Color.White,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = AppStrings.getString("next_due", language),
                        amount = nextSchedule?.dueDate ?: "All Paid",
                        subtitle = if (nextSchedule != null) "${AppStrings.getString("due_in", language)} $daysUntilDue ${AppStrings.getString("days_left", language)}" else "No Pending Dues",
                        icon = Icons.Default.Event,
                        iconTint = RoseError,
                        backgroundColor = if (daysUntilDue <= 7 && nextSchedule != null) RoseContainer.copy(alpha = 0.5f) else Color.White,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Primary Action Bar
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        selectedScheduleForPay = nextSchedule?.id
                        showPaymentDialog = true
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("dashboard_pay_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Teal600),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = AppStrings.getString("pay_now", language),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                OutlinedButton(
                    onClick = onRequestExtensionClick,
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("dashboard_extension_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Navy800)
                ) {
                    Icon(Icons.Outlined.HelpOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Hardship Help", fontSize = 13.sp)
                }
            }
        }

        // Monthly Savings Planner Box
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .testTag("savings_planner_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Navy800),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Teal500),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Savings, contentDescription = null, tint = Navy900, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = AppStrings.getString("savings", language),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = AppStrings.getString("recommended_savings", language),
                                    fontSize = 11.sp,
                                    color = Teal100
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Teal500
                        ) {
                            Text(
                                text = "₹${recommendedSavings.toInt()}/mo",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Navy900,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Progress of savings pot
                    val savingsProgress = (currentSaved / totalAnnual.coerceAtLeast(1.0)).coerceIn(0.0, 1.0).toFloat()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${AppStrings.getString("current_savings", language)}: ₹${currentSaved.toInt()}",
                            fontSize = 12.sp,
                            color = Slate200
                        )
                        Text(
                            text = "Target: ₹${totalAnnual.toInt()}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Teal100
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { savingsProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = Teal500,
                        trackColor = Navy600
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Deposit / Withdraw actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilledTonalButton(
                            onClick = {
                                viewModel.depositSavings(500.0)
                                Toast.makeText(context, "Added ₹500 to savings pot!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = Navy700,
                                contentColor = Teal100
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("+ ₹500", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }

                        FilledTonalButton(
                            onClick = {
                                viewModel.depositSavings(1500.0)
                                Toast.makeText(context, "Added ₹1,500 monthly target to savings pot!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = Navy700,
                                contentColor = Teal100
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1.2f)
                        ) {
                            Text("+ ₹1,500 (Rec)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        TextButton(
                            onClick = { onNavigateToTab(NavigationTab.SAVINGS) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Planner →", color = Teal500, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Installment Terms Section
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Academic Fee Installments",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Navy900
                    )
                    TextButton(onClick = { onNavigateToTab(NavigationTab.SCHEDULE) }) {
                        Text("View Details", color = Teal600, fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                schedules.forEach { schedule ->
                    InstallmentCard(
                        schedule = schedule,
                        language = language,
                        onPayClick = {
                            selectedScheduleForPay = schedule.id
                            showPaymentDialog = true
                        }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }

        // Recent Payments
        item {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Payments & Receipts",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Navy900
                    )
                    TextButton(onClick = { onNavigateToTab(NavigationTab.HISTORY) }) {
                        Text("All Receipts", color = Teal600, fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (payments.isEmpty()) {
                    Text("No payment transactions recorded yet.", fontSize = 13.sp, color = Slate600)
                } else {
                    payments.take(2).forEach { payment ->
                        PaymentItemCard(
                            payment = payment,
                            language = language,
                            onViewReceipt = { selectedPaymentForReceipt = payment }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }

        // School Announcements & Fee Revision Notice
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text(
                    text = AppStrings.getString("announcements", language),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Navy900
                )
                Spacer(modifier = Modifier.height(8.dp))

                announcements.take(2).forEach { announcement ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = if (announcement.isImportant) AmberContainer.copy(alpha = 0.5f) else Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (announcement.isImportant) Icons.Default.Campaign else Icons.Default.Info,
                                        contentDescription = null,
                                        tint = if (announcement.isImportant) AmberWarning else Teal600,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (language == AppLanguage.TAMIL) announcement.titleTamil else announcement.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Navy900
                                    )
                                }
                                Text(announcement.date, fontSize = 10.sp, color = Slate600)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (language == AppLanguage.TAMIL) announcement.contentTamil else announcement.content,
                                fontSize = 12.sp,
                                color = Slate700,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }

    // Payment Dialog
    if (showPaymentDialog) {
        PaymentDialog(
            schedules = schedules,
            selectedScheduleId = selectedScheduleForPay,
            language = language,
            onDismiss = { showPaymentDialog = false },
            onConfirmPayment = { schedId, amount, mode, txnRef ->
                viewModel.recordPayment(schedId, amount, mode, txnRef)
            }
        )
    }

    // Receipt Dialog
    selectedPaymentForReceipt?.let { payment ->
        DigitalReceiptDialog(
            payment = payment,
            studentName = activeStudent?.name ?: "Aarav Sharma",
            grade = activeStudent?.grade ?: "Grade 7-A",
            rollNo = activeStudent?.rollNo ?: "SEC-2026-0742",
            language = language,
            onDismiss = { selectedPaymentForReceipt = null }
        )
    }
}

@Composable
fun MetricCard(
    title: String,
    amount: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Slate600
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(iconTint.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = amount,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Navy900
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = Slate600
            )
        }
    }
}

@Composable
fun InstallmentCard(
    schedule: FeeScheduleEntity,
    language: AppLanguage,
    onPayClick: () -> Unit
) {
    val remaining = (schedule.baseAmount + schedule.latePenaltyAmount) - schedule.paidAmount
    val isPaidInFull = remaining <= 0
    val isPartiallyPaid = schedule.paidAmount > 0 && !isPaidInFull

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = schedule.termName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Navy900
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    // Status Badge
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = when {
                            isPaidInFull -> EmeraldGreenContainer
                            isPartiallyPaid -> AmberContainer
                            schedule.isOverdue -> RoseContainer
                            else -> Slate100
                        }
                    ) {
                        Text(
                            text = when {
                                isPaidInFull -> AppStrings.getString("status_paid", language)
                                isPartiallyPaid -> AppStrings.getString("status_partial", language)
                                schedule.isOverdue -> AppStrings.getString("status_overdue", language)
                                else -> AppStrings.getString("status_pending", language)
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                isPaidInFull -> EmeraldGreen
                                isPartiallyPaid -> AmberWarning
                                schedule.isOverdue -> RoseError
                                else -> Slate700
                            },
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Due: ${schedule.dueDate} | Fee: ₹${schedule.baseAmount.toInt()}",
                    fontSize = 12.sp,
                    color = Slate600
                )

                if (isPartiallyPaid) {
                    Text(
                        text = "Paid: ₹${schedule.paidAmount.toInt()} | Balance: ₹${remaining.toInt()}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AmberWarning
                    )
                }
            }

            if (!isPaidInFull) {
                Button(
                    onClick = onPayClick,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Teal600),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("Pay ₹${remaining.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Icon(Icons.Default.CheckCircle, contentDescription = "Paid", tint = EmeraldGreen, modifier = Modifier.size(24.dp))
            }
        }
    }
}

@Composable
fun PaymentItemCard(
    payment: PaymentEntity,
    language: AppLanguage,
    onViewReceipt: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Teal50),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = Teal600, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(payment.termName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Navy900)
                    Text("${payment.paymentDate} • ${payment.paymentMode}", fontSize = 11.sp, color = Slate600)
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text("₹${payment.amount.toInt()}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = EmeraldGreen)
                Text(
                    text = AppStrings.getString("view_receipt", language),
                    fontSize = 11.sp,
                    color = Teal600,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clickable(onClick = onViewReceipt)
                        .padding(top = 2.dp)
                )
            }
        }
    }
}
