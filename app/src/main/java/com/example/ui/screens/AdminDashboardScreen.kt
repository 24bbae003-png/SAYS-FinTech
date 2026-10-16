package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.entity.PaymentEntity
import com.example.data.local.entity.SupportRequestEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.FeeViewModel
import com.example.util.AppLanguage
import com.example.util.AppStrings
import kotlin.math.max

@Composable
fun AdminDashboardScreen(
    viewModel: FeeViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val language by viewModel.language.collectAsState()
    val allPayments by viewModel.allPayments.collectAsState()
    val allSchedules by viewModel.allSchedules.collectAsState()
    val students by viewModel.students.collectAsState()
    val supportRequests by viewModel.supportRequests.collectAsState()
    val announcements by viewModel.announcements.collectAsState()

    var activeAdminTab by remember { mutableStateOf(0) } // 0: Verify Payments, 1: Review Hardships, 2: Announcements, 3: Students
    var showAddAnnouncementDialog by remember { mutableStateOf(false) }

    val totalCollected = remember(allPayments) { allPayments.sumOf { it.amount } }
    val totalExpected = remember(students) { students.sumOf { it.annualFee } }
    val pendingVerificationCount = remember(allPayments) { allPayments.count { it.status != "VERIFIED" } }
    val pendingRequestsCount = remember(supportRequests) { supportRequests.count { it.status == "PENDING" } }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Slate50),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Staff Header Card
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
                                text = "School Administration Fee Portal",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Accounts & Welfare Management Desk",
                                fontSize = 11.sp,
                                color = Teal100
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Teal500
                        ) {
                            Text(
                                text = "STAFF ACCESS",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Navy900,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total Collections", fontSize = 11.sp, color = Slate300)
                            Text("₹${totalCollected.toInt()}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = EmeraldGreen)
                        }
                        Column {
                            Text("Expected Fees", fontSize = 11.sp, color = Slate300)
                            Text("₹${totalExpected.toInt()}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Pending Actions", fontSize = 11.sp, color = Slate300)
                            Text("${pendingVerificationCount + pendingRequestsCount} Tasks", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AmberWarning)
                        }
                    }
                }
            }
        }

        // Admin Navigation Tabs
        item {
            ScrollableTabRow(
                selectedTabIndex = activeAdminTab,
                containerColor = Color.White,
                contentColor = Navy900,
                edgePadding = 0.dp,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .fillMaxWidth()
            ) {
                Tab(
                    selected = activeAdminTab == 0,
                    onClick = { activeAdminTab = 0 },
                    text = { Text("Verify Receipts ($pendingVerificationCount)", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = activeAdminTab == 1,
                    onClick = { activeAdminTab = 1 },
                    text = { Text("Hardship Requests ($pendingRequestsCount)", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = activeAdminTab == 2,
                    onClick = { activeAdminTab = 2 },
                    text = { Text("Announcements", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = activeAdminTab == 3,
                    onClick = { activeAdminTab = 3 },
                    text = { Text("All Students (${students.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
            }
        }

        // Tab 0: Verify Receipts
        if (activeAdminTab == 0) {
            item {
                Text("Parent Payment Receipts Verification Queue", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Navy900)
            }
            if (allPayments.isEmpty()) {
                item {
                    Text("No payment submissions.", fontSize = 12.sp, color = Slate600)
                }
            } else {
                items(allPayments) { payment ->
                    val student = students.find { it.id == payment.studentId }
                    Card(
                        shape = RoundedCornerShape(12.dp),
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
                                Column {
                                    Text("${student?.name ?: "Student"} (${student?.grade ?: ""})", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Navy900)
                                    Text(payment.termName, fontSize = 11.sp, color = Slate600)
                                }
                                Text("₹${payment.amount.toInt()}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = EmeraldGreen)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Txn Ref: ${payment.transactionRef} • Mode: ${payment.paymentMode}", fontSize = 10.sp, color = Slate600, fontFamily = FontFamily.Monospace)
                            Text("Status: ${payment.status}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (payment.status == "VERIFIED") EmeraldGreen else AmberWarning)

                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (payment.status != "VERIFIED") {
                                    OutlinedButton(
                                        onClick = {
                                            viewModel.verifyPayment(payment.id, "REJECTED", "Mismatch in transaction reference")
                                            Toast.makeText(context, "Payment marked as rejected", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = RoseError),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Text("Reject", fontSize = 11.sp)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        onClick = {
                                            viewModel.verifyPayment(payment.id, "VERIFIED", "Approved by accounts desk")
                                            Toast.makeText(context, "Payment verified & official seal applied!", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Teal600),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Verify & Approve", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    Surface(
                                        color = EmeraldGreenContainer,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text("VERIFIED & STAMPED", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = EmeraldGreen, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Tab 1: Review Hardships
        if (activeAdminTab == 1) {
            item {
                Text("Fee Hardship & Extension Review", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Navy900)
            }
            if (supportRequests.isEmpty()) {
                item {
                    Text("No hardship requests received.", fontSize = 12.sp, color = Slate600)
                }
            } else {
                items(supportRequests) { request ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(request.studentName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Navy900)
                                Text(request.status, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (request.status == "APPROVED") EmeraldGreen else AmberWarning)
                            }
                            Text("Type: ${request.requestType} • Requested Date: ${request.requestedExtensionDate}", fontSize = 11.sp, color = Slate600)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Parent Explanation: ${request.reason}", fontSize = 12.sp, color = Slate800)

                            if (request.status == "PENDING") {
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            viewModel.updateSupportRequestDecision(request.id, "REJECTED", "Does not meet policy guidelines")
                                            Toast.makeText(context, "Request rejected", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = RoseError),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Text("Decline", fontSize = 11.sp)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        onClick = {
                                            viewModel.updateSupportRequestDecision(request.id, "APPROVED", "Granted 30-day extension with zero late penalty fees.")
                                            Toast.makeText(context, "Approved! 30-day grace period granted.", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Teal600),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Text("Grant Extension", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Tab 2: Announcements
        if (activeAdminTab == 2) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("School Announcements & Fee Updates", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Navy900)
                    Button(
                        onClick = { showAddAnnouncementDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Teal600),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Publish", fontSize = 11.sp)
                    }
                }
            }

            items(announcements) { announcement ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(announcement.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Navy900)
                            Text(announcement.date, fontSize = 10.sp, color = Slate600)
                        }
                        Text(announcement.content, fontSize = 12.sp, color = Slate700, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }
        }

        // Tab 3: Students
        if (activeAdminTab == 3) {
            item {
                Text("Enrolled Students Fee Registry", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Navy900)
            }
            items(students) { student ->
                val studentSchedules = allSchedules.filter { it.studentId == student.id }
                val paid = studentSchedules.sumOf { it.paidAmount }
                val remaining = max(0.0, student.annualFee - paid)

                Card(
                    shape = RoundedCornerShape(12.dp),
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
                            Column {
                                Text(student.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Navy900)
                                Text("${student.grade} • Roll: ${student.rollNo}", fontSize = 11.sp, color = Slate600)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Annual: ₹${student.annualFee.toInt()}", fontSize = 12.sp, color = Slate700)
                                Text("Paid: ₹${paid.toInt()} • Due: ₹${remaining.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (remaining <= 0) EmeraldGreen else AmberWarning)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddAnnouncementDialog) {
        AddAnnouncementDialog(
            language = language,
            onDismiss = { showAddAnnouncementDialog = false },
            onPublish = { title, titleTa, content, contentTa, cat, isImp ->
                viewModel.addAnnouncement(title, titleTa, content, contentTa, cat, isImp)
                Toast.makeText(context, "Announcement published to parent portal!", Toast.LENGTH_SHORT).show()
                showAddAnnouncementDialog = false
            }
        )
    }
}

@Composable
fun AddAnnouncementDialog(
    language: AppLanguage,
    onDismiss: () -> Unit,
    onPublish: (String, String, String, String, String, Boolean) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var titleTa by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var contentTa by remember { mutableStateOf("") }
    var isImportant by remember { mutableStateOf(false) }

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
                Text("Publish School Announcement", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Navy900)
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title (English)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = titleTa,
                    onValueChange = { titleTa = it },
                    label = { Text("Title (தமிழ் - Tamil)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Content (English)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    shape = RoundedCornerShape(10.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = contentTa,
                    onValueChange = { contentTa = it },
                    label = { Text("Content (தமிழ் - Tamil)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = isImportant,
                        onCheckedChange = { isImportant = it },
                        colors = CheckboxDefaults.colors(checkedColor = AmberWarning)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Highlight as Fee Revision / Urgent Notice", fontSize = 11.sp, color = Slate800)
                }

                Spacer(modifier = Modifier.height(14.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel", color = Slate600) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                onPublish(
                                    title,
                                    if (titleTa.isBlank()) title else titleTa,
                                    content,
                                    if (contentTa.isBlank()) content else contentTa,
                                    "FEE_UPDATE",
                                    isImportant
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Teal600),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Publish Notice")
                    }
                }
            }
        }
    }
}
