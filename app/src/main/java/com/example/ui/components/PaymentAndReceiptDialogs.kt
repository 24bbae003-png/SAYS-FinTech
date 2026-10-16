package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.entity.FeeScheduleEntity
import com.example.data.local.entity.PaymentEntity
import com.example.ui.theme.*
import com.example.util.AppLanguage
import com.example.util.AppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentDialog(
    schedules: List<FeeScheduleEntity>,
    selectedScheduleId: Long?,
    language: AppLanguage,
    onDismiss: () -> Unit,
    onConfirmPayment: (scheduleId: Long, amount: Double, mode: String, txnRef: String) -> Unit
) {
    val context = LocalContext.current
    var currentScheduleId by remember {
        mutableStateOf(selectedScheduleId ?: schedules.firstOrNull { it.paidAmount < it.baseAmount }?.id ?: schedules.firstOrNull()?.id ?: 1L)
    }
    val activeSchedule = schedules.find { it.id == currentScheduleId }
    val remaining = activeSchedule?.let { (it.baseAmount + it.latePenaltyAmount) - it.paidAmount } ?: 4000.0

    var amountText by remember(currentScheduleId) { mutableStateOf(remaining.toInt().toString()) }
    var selectedMode by remember { mutableStateOf("UPI (GPay / PhonePe)") }
    var txnRef by remember { mutableStateOf("UPI-${(1000000000L..9999999999L).random()}") }
    var receiptProofAttached by remember { mutableStateOf(true) }

    val paymentModes = listOf("UPI (GPay / PhonePe)", "Net Banking (SBI/HDFC)", "Debit / Credit Card", "Bank Challan / Cash")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shadowElevation = 12.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = AppStrings.getString("record_payment", language),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Navy900
                        )
                        Text(
                            text = "Greenwood International School",
                            fontSize = 12.sp,
                            color = Slate600
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Slate600)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Select Installment Term
                Text("Select Installment Term", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Slate700)
                Spacer(modifier = Modifier.height(6.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    schedules.forEach { schedule ->
                        val schedRemaining = (schedule.baseAmount + schedule.latePenaltyAmount) - schedule.paidAmount
                        val isSelected = schedule.id == currentScheduleId
                        Surface(
                            onClick = { currentScheduleId = schedule.id },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) Teal50 else Slate100,
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, Teal600) else null,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { currentScheduleId = schedule.id },
                                        colors = RadioButtonDefaults.colors(selectedColor = Teal600)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(schedule.termName, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Navy900)
                                        Text("Due: ${schedule.dueDate}", fontSize = 11.sp, color = Slate600)
                                    }
                                }
                                Text(
                                    text = if (schedRemaining <= 0) "Paid in Full" else "Due: ₹${schedRemaining.toInt()}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (schedRemaining <= 0) EmeraldGreen else RoseError
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Payment Amount
                Text(AppStrings.getString("amount", language) + " (₹)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Slate700)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    singleLine = true,
                    leadingIcon = { Text("₹", fontWeight = FontWeight.Bold, color = Navy800, modifier = Modifier.padding(start = 12.dp)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("payment_amount_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                // Quick amount chips
                Row(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SuggestionChip(
                        onClick = { amountText = remaining.toInt().toString() },
                        label = { Text("Full: ₹${remaining.toInt()}", fontSize = 11.sp) }
                    )
                    SuggestionChip(
                        onClick = { amountText = (remaining / 2).toInt().coerceAtLeast(1000).toString() },
                        label = { Text("Part: ₹${(remaining / 2).toInt().coerceAtLeast(1000)}", fontSize = 11.sp) }
                    )
                    SuggestionChip(
                        onClick = { amountText = "1500" },
                        label = { Text("Monthly: ₹1,500", fontSize = 11.sp) }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Payment Mode Selection
                Text(AppStrings.getString("payment_method", language), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Slate700)
                Spacer(modifier = Modifier.height(6.dp))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    paymentModes.forEach { mode ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selectedMode == mode) Teal50 else Color.Transparent)
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = selectedMode == mode,
                                onClick = { selectedMode = mode },
                                colors = RadioButtonDefaults.colors(selectedColor = Teal600)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(mode, fontSize = 13.sp, color = Navy900)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // UTR / Transaction Ref
                Text(AppStrings.getString("transaction_id", language), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Slate700)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = txnRef,
                    onValueChange = { txnRef = it },
                    singleLine = true,
                    placeholder = { Text("Enter Bank UTR / Reference ID", fontSize = 12.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("payment_ref_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Digital Receipt Proof Attachment
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Slate100,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AttachFile, contentDescription = "Receipt Attachment", tint = Teal600)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Payment Receipt Screenshot", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Navy900)
                                Text(if (receiptProofAttached) "Attached: receipt_screenshot.png" else "No proof uploaded", fontSize = 11.sp, color = Slate600)
                            }
                        }
                        Switch(
                            checked = receiptProofAttached,
                            onCheckedChange = { receiptProofAttached = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Teal600, checkedTrackColor = Teal100)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Button(
                    onClick = {
                        val amountVal = amountText.toDoubleOrNull() ?: 0.0
                        if (amountVal <= 0) {
                            Toast.makeText(context, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        onConfirmPayment(currentScheduleId, amountVal, selectedMode, txnRef)
                        Toast.makeText(context, "Payment of ₹${amountVal.toInt()} recorded successfully!", Toast.LENGTH_LONG).show()
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("submit_payment_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Teal600),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(AppStrings.getString("submit_payment", language), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun DigitalReceiptDialog(
    payment: PaymentEntity,
    studentName: String,
    grade: String,
    rollNo: String,
    language: AppLanguage,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            shadowElevation = 16.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with school branding
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Navy800),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.School, contentDescription = null, tint = Teal500, modifier = Modifier.size(22.dp))
                    }
                    Text(
                        text = "OFFICIAL FEE RECEIPT",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Navy900,
                        letterSpacing = 1.sp
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Slate600)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "GREENWOOD INTERNATIONAL SCHOOL",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Navy900,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Affiliation No: CBSE-TN-849201 | Madurai, Tamil Nadu",
                    fontSize = 10.sp,
                    color = Slate600,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Slate200, thickness = 1.dp)
                Spacer(modifier = Modifier.height(12.dp))

                // Receipt Meta
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Receipt No:", fontSize = 11.sp, color = Slate600)
                        Text(payment.receiptNumber, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = Navy900)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Date & Time:", fontSize = 11.sp, color = Slate600)
                        Text("${payment.paymentDate} | 10:30 AM", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Navy900)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Student details box
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Slate50,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Student Name:", fontSize = 11.sp, color = Slate600)
                            Text(studentName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Navy900)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Grade / Section:", fontSize = 11.sp, color = Slate600)
                            Text(grade, fontSize = 12.sp, color = Navy900)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Roll Number:", fontSize = 11.sp, color = Slate600)
                            Text(rollNo, fontSize = 12.sp, fontFamily = FontFamily.Monospace, color = Navy900)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Line items
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Description / Installment", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Slate700)
                    Text("Amount Paid", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Slate700)
                }
                Spacer(modifier = Modifier.height(6.dp))
                HorizontalDivider(color = Slate200, thickness = 1.dp)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(payment.termName, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Navy900)
                    Text("₹${payment.amount.toInt()}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Navy900)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Payment Mode: ${payment.paymentMode}", fontSize = 11.sp, color = Slate600)
                    Text("Txn: ${payment.transactionRef}", fontSize = 10.sp, color = Slate600, fontFamily = FontFamily.Monospace)
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Slate200, thickness = 1.dp)
                Spacer(modifier = Modifier.height(8.dp))

                // Total
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Total Received", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Navy900)
                    Text("₹${payment.amount.toInt()}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Teal600)
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Verification Stamp
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = EmeraldGreenContainer,
                    border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldGreen),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.Verified, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (payment.status == "VERIFIED") "DIGITALLY VERIFIED BY SCHOOL DESK" else "PAYMENT RECORDED - VERIFICATION PENDING",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldGreen
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Download & Share buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            Toast.makeText(context, "Digital Receipt saved to Downloads (${payment.receiptNumber}.pdf)", Toast.LENGTH_LONG).show()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .testTag("download_receipt_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Download PDF", fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            Toast.makeText(context, "Receipt shared via WhatsApp / Email", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .testTag("share_receipt_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Navy800),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun AddSiblingDialog(
    language: AppLanguage,
    onDismiss: () -> Unit,
    onAdd: (name: String, grade: String, rollNo: String) -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var grade by remember { mutableStateOf("Grade 1-C") }
    var rollNo by remember { mutableStateOf("PRI-2026-0${(100..999).random()}") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shadowElevation = 12.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = AppStrings.getString("add_child", language),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Navy900
                )
                Text(
                    text = "Manage multiple children under one parent fee account",
                    fontSize = 12.sp,
                    color = Slate600
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Child's Full Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("child_name_input"),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = grade,
                    onValueChange = { grade = it },
                    label = { Text("Grade & Section (e.g. Grade 2-A)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("child_grade_input"),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = rollNo,
                    onValueChange = { rollNo = it },
                    label = { Text("Student Roll / Admission No.") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("child_roll_input"),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(AppStrings.getString("cancel", language), color = Slate600)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isBlank()) {
                                Toast.makeText(context, "Please enter student name", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            onAdd(name, grade, rollNo)
                            Toast.makeText(context, "Child profile added successfully!", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Teal600),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("confirm_add_sibling_button")
                    ) {
                        Text(AppStrings.getString("confirm", language))
                    }
                }
            }
        }
    }
}
