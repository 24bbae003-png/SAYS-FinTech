package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.FeeScheduleEntity
import com.example.data.local.entity.PaymentEntity
import com.example.ui.components.DigitalReceiptDialog
import com.example.ui.components.PaymentDialog
import com.example.ui.theme.*
import com.example.ui.viewmodel.FeeViewModel
import com.example.util.AppLanguage
import com.example.util.AppStrings

@Composable
fun FeeScheduleScreen(
    viewModel: FeeViewModel,
    modifier: Modifier = Modifier
) {
    val language by viewModel.language.collectAsState()
    val schedules by viewModel.activeSchedules.collectAsState()
    val payments by viewModel.activePayments.collectAsState()
    val students by viewModel.students.collectAsState()
    val selectedStudentId by viewModel.selectedStudentId.collectAsState()

    var showPaymentDialog by remember { mutableStateOf(false) }
    var selectedScheduleId by remember { mutableStateOf<Long?>(null) }
    var selectedPaymentForReceipt by remember { mutableStateOf<PaymentEntity?>(null) }

    val activeStudent = students.find { it.id == selectedStudentId }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Slate50),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Info Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Navy800),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Academic Fee Structure (2026-27)",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = if (activeStudent != null) "${activeStudent.name} • ${activeStudent.grade}" else "Combined Family Schedule",
                                fontSize = 12.sp,
                                color = Teal100
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Teal500
                        ) {
                            Text(
                                text = "Total: ₹18,000",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Navy900,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Standard 3-installment plan: Term 1 in June (₹6,000), Term 2 in October (₹6,000), and Term 3 in January (₹6,000).",
                        fontSize = 12.sp,
                        color = Slate200,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Installment Cards
        items(schedules) { schedule ->
            val remaining = (schedule.baseAmount + schedule.latePenaltyAmount) - schedule.paidAmount
            val isPaidInFull = remaining <= 0
            val isPartiallyPaid = schedule.paidAmount > 0 && !isPaidInFull

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (isPaidInFull) EmeraldGreenContainer else if (isPartiallyPaid) AmberContainer else Teal50),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "T${schedule.termNumber}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isPaidInFull) EmeraldGreen else if (isPartiallyPaid) AmberWarning else Teal600
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = schedule.termName,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Navy900
                                )
                                Text(
                                    text = "Due Date: ${schedule.dueDate}",
                                    fontSize = 12.sp,
                                    color = Slate600
                                )
                            }
                        }

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
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    isPaidInFull -> EmeraldGreen
                                    isPartiallyPaid -> AmberWarning
                                    schedule.isOverdue -> RoseError
                                    else -> Slate700
                                },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = Slate100)
                    Spacer(modifier = Modifier.height(10.dp))

                    // Component Breakdown
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        FeeComponentRow("Tuition & Classroom Instruction", "₹4,200")
                        FeeComponentRow("Computer Lab & Smart Class", "₹800")
                        FeeComponentRow("Library, Sports & Co-Curricular", "₹500")
                        FeeComponentRow("Term Examination & Materials", "₹500")
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = Slate100)
                    Spacer(modifier = Modifier.height(10.dp))

                    // Payment summary & Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Total Installment: ₹${schedule.baseAmount.toInt()}", fontSize = 12.sp, color = Slate600)
                            if (schedule.paidAmount > 0) {
                                Text(
                                    text = "Paid: ₹${schedule.paidAmount.toInt()} • Due: ₹${remaining.toInt()}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isPaidInFull) EmeraldGreen else AmberWarning
                                )
                            } else {
                                Text("Remaining Due: ₹${remaining.toInt()}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = RoseError)
                            }
                        }

                        if (!isPaidInFull) {
                            Button(
                                onClick = {
                                    selectedScheduleId = schedule.id
                                    showPaymentDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Teal600),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("pay_term_${schedule.termNumber}_button")
                            ) {
                                Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Pay Now", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            // Find corresponding payment to view receipt
                            val matchingPayment = payments.find { it.scheduleId == schedule.id }
                            OutlinedButton(
                                onClick = {
                                    if (matchingPayment != null) {
                                        selectedPaymentForReceipt = matchingPayment
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Teal600)
                            ) {
                                Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Receipt", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // Late Fee Rules Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AmberContainer.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Warning,
                        contentDescription = "Late Fee Rules",
                        tint = AmberWarning,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = AppStrings.getString("late_fee_alert", language) + " & Grace Period Policy",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "• 7 days grace period is provided from the due date.\n• ₹50/day late fee penalty is automatically added after grace period.\n• Financial hardship requests submitted before due date waive all penalty fees.",
                            fontSize = 11.sp,
                            color = Slate800,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }

    // Payment Dialog
    if (showPaymentDialog) {
        PaymentDialog(
            schedules = schedules,
            selectedScheduleId = selectedScheduleId,
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
fun FeeComponentRow(label: String, amount: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 12.sp, color = Slate600)
        Text(amount, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Slate800)
    }
}
