package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.PaymentEntity
import com.example.ui.components.DigitalReceiptDialog
import com.example.ui.theme.*
import com.example.ui.viewmodel.FeeViewModel
import com.example.util.AppLanguage
import com.example.util.AppStrings

@Composable
fun PaymentHistoryScreen(
    viewModel: FeeViewModel,
    modifier: Modifier = Modifier
) {
    val language by viewModel.language.collectAsState()
    val payments by viewModel.activePayments.collectAsState()
    val students by viewModel.students.collectAsState()
    val selectedStudentId by viewModel.selectedStudentId.collectAsState()

    var filterStatus by remember { mutableStateOf("ALL") }
    var selectedPaymentForReceipt by remember { mutableStateOf<PaymentEntity?>(null) }

    val activeStudent = students.find { it.id == selectedStudentId }

    val filteredPayments = remember(payments, filterStatus) {
        when (filterStatus) {
            "VERIFIED" -> payments.filter { it.status == "VERIFIED" }
            "PENDING" -> payments.filter { it.status != "VERIFIED" }
            else -> payments
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Slate50),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Navy800),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Fee Payment Records & Digital Receipts",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Review all verified transaction proofs and official school receipts",
                        fontSize = 12.sp,
                        color = Teal100
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total Transactions", fontSize = 11.sp, color = Slate300)
                            Text("${payments.size} Records", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Total Settled", fontSize = 11.sp, color = Slate300)
                            Text("₹${payments.sumOf { it.amount }.toInt()}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Teal500)
                        }
                    }
                }
            }
        }

        // Filter Chips
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = filterStatus == "ALL",
                        onClick = { filterStatus = "ALL" },
                        label = { Text("All Payments (${payments.size})", fontSize = 12.sp) }
                    )
                }
                item {
                    FilterChip(
                        selected = filterStatus == "VERIFIED",
                        onClick = { filterStatus = "VERIFIED" },
                        label = { Text("Verified (${payments.count { it.status == "VERIFIED" }})", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(14.dp), tint = EmeraldGreen) }
                    )
                }
                item {
                    FilterChip(
                        selected = filterStatus == "PENDING",
                        onClick = { filterStatus = "PENDING" },
                        label = { Text("Pending Review (${payments.count { it.status != "VERIFIED" }})", fontSize = 12.sp) }
                    )
                }
            }
        }

        // Payments List
        if (filteredPayments.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = Slate300, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No payment records found", fontSize = 14.sp, color = Slate600)
                    }
                }
            }
        } else {
            items(filteredPayments) { payment ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
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
                                        .background(Teal50),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Teal600, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(payment.termName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Navy900)
                                    Text("Paid on: ${payment.paymentDate}", fontSize = 11.sp, color = Slate600)
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text("₹${payment.amount.toInt()}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = EmeraldGreen)
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (payment.status == "VERIFIED") EmeraldGreenContainer else AmberContainer
                                ) {
                                    Text(
                                        text = if (payment.status == "VERIFIED") "VERIFIED" else "PENDING",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (payment.status == "VERIFIED") EmeraldGreen else AmberWarning,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = Slate100)
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Mode: ${payment.paymentMode}", fontSize = 11.sp, color = Slate700)
                                Text("Ref: ${payment.transactionRef}", fontSize = 10.sp, color = Slate600, fontFamily = FontFamily.Monospace)
                            }

                            Button(
                                onClick = { selectedPaymentForReceipt = payment },
                                colors = ButtonDefaults.buttonColors(containerColor = Navy800),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier
                                    .height(32.dp)
                                    .testTag("view_receipt_${payment.id}")
                            ) {
                                Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(AppStrings.getString("view_receipt", language), fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
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
