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
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.entity.ScholarshipEntity
import com.example.data.local.entity.SupportRequestEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.FeeViewModel
import com.example.util.AppLanguage
import com.example.util.AppStrings
import kotlin.math.max

enum class ServiceSubTab {
    BUDGET,
    PREDICTOR,
    SCHOLARSHIPS,
    HARDSHIP
}

@Composable
fun ServicesAndBudgetScreen(
    viewModel: FeeViewModel,
    initialTab: ServiceSubTab = ServiceSubTab.BUDGET,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val language by viewModel.language.collectAsState()
    val budget by viewModel.budget.collectAsState()
    val scholarships by viewModel.scholarships.collectAsState()
    val supportRequests by viewModel.supportRequests.collectAsState()

    var activeSubTab by remember { mutableStateOf(initialTab) }
    var showRequestDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Slate50),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Tab Selector Row
        item {
            ScrollableTabRow(
                selectedTabIndex = activeSubTab.ordinal,
                containerColor = Color.White,
                contentColor = Navy900,
                edgePadding = 0.dp,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .fillMaxWidth()
            ) {
                Tab(
                    selected = activeSubTab == ServiceSubTab.BUDGET,
                    onClick = { activeSubTab = ServiceSubTab.BUDGET },
                    text = { Text("Family Budget", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = activeSubTab == ServiceSubTab.PREDICTOR,
                    onClick = { activeSubTab = ServiceSubTab.PREDICTOR },
                    text = { Text("Cost Estimator", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = activeSubTab == ServiceSubTab.SCHOLARSHIPS,
                    onClick = { activeSubTab = ServiceSubTab.SCHOLARSHIPS },
                    text = { Text("Scholarships", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = activeSubTab == ServiceSubTab.HARDSHIP,
                    onClick = { activeSubTab = ServiceSubTab.HARDSHIP },
                    text = { Text("Fee Assistance", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
            }
        }

        // Subtab 1: Family Budget Tracker
        if (activeSubTab == ServiceSubTab.BUDGET) {
            item {
                BudgetTrackerView(
                    language = language,
                    currentIncome = budget?.monthlyIncome ?: 50000.0,
                    currentRent = budget?.rentExpense ?: 14000.0,
                    currentGroceries = budget?.groceriesExpense ?: 10000.0,
                    currentUtilities = budget?.utilitiesExpense ?: 4500.0,
                    currentTransport = budget?.transportExpense ?: 4000.0,
                    currentOther = budget?.otherExpense ?: 3500.0,
                    currentSchoolSavings = budget?.schoolSavingsTarget ?: 3000.0,
                    onSaveBudget = { inc, rent, groc, util, trans, oth, sch ->
                        viewModel.updateBudget(inc, rent, groc, util, trans, oth, sch)
                        Toast.makeText(context, "Monthly Budget updated successfully!", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }

        // Subtab 2: Expense Prediction
        if (activeSubTab == ServiceSubTab.PREDICTOR) {
            item {
                ExpensePredictorView(language = language)
            }
        }

        // Subtab 3: Scholarships Information
        if (activeSubTab == ServiceSubTab.SCHOLARSHIPS) {
            items(scholarships) { scholarship ->
                ScholarshipItemCard(
                    scholarship = scholarship,
                    language = language,
                    onApply = {
                        viewModel.applyForScholarship(scholarship.id)
                        Toast.makeText(context, "Application submitted for ${scholarship.title}!", Toast.LENGTH_LONG).show()
                    }
                )
            }
        }

        // Subtab 4: Financial Hardship Support Requests
        if (activeSubTab == ServiceSubTab.HARDSHIP) {
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
                                    text = "Fee Extension & Relief Desk",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Facing difficulties? Request grace period or split terms",
                                    fontSize = 12.sp,
                                    color = Teal100
                                )
                            }
                            Button(
                                onClick = { showRequestDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Teal500),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = Navy900, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("New Request", color = Navy900, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            items(supportRequests) { request ->
                SupportRequestItemCard(request = request, language = language)
            }
        }
    }

    if (showRequestDialog) {
        SupportRequestDialog(
            language = language,
            onDismiss = { showRequestDialog = false },
            onSubmit = { type, date, reason ->
                viewModel.submitSupportRequest(type, date, reason)
                Toast.makeText(context, "Hardship request submitted to school committee!", Toast.LENGTH_LONG).show()
                showRequestDialog = false
            }
        )
    }
}

@Composable
fun BudgetTrackerView(
    language: AppLanguage,
    currentIncome: Double,
    currentRent: Double,
    currentGroceries: Double,
    currentUtilities: Double,
    currentTransport: Double,
    currentOther: Double,
    currentSchoolSavings: Double,
    onSaveBudget: (Double, Double, Double, Double, Double, Double, Double) -> Unit
) {
    var income by remember { mutableStateOf(currentIncome.toInt().toString()) }
    var rent by remember { mutableStateOf(currentRent.toInt().toString()) }
    var groceries by remember { mutableStateOf(currentGroceries.toInt().toString()) }
    var utilities by remember { mutableStateOf(currentUtilities.toInt().toString()) }
    var transport by remember { mutableStateOf(currentTransport.toInt().toString()) }
    var other by remember { mutableStateOf(currentOther.toInt().toString()) }
    var schoolSavings by remember { mutableStateOf(currentSchoolSavings.toInt().toString()) }

    val incVal = income.toDoubleOrNull() ?: 0.0
    val totalExpense = (rent.toDoubleOrNull() ?: 0.0) +
            (groceries.toDoubleOrNull() ?: 0.0) +
            (utilities.toDoubleOrNull() ?: 0.0) +
            (transport.toDoubleOrNull() ?: 0.0) +
            (other.toDoubleOrNull() ?: 0.0) +
            (schoolSavings.toDoubleOrNull() ?: 0.0)
    val remainingBuffer = max(0.0, incVal - totalExpense)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Monthly Family Financial Budget", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Navy900)
            Text("Ensure comfortable fee savings without straining basic household expenses", fontSize = 12.sp, color = Slate600)

            Spacer(modifier = Modifier.height(14.dp))

            // Summary Progress
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Income: ₹${incVal.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Slate700)
                Text("Remaining: ₹${remainingBuffer.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (remainingBuffer > 5000) EmeraldGreen else AmberWarning)
            }
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { (totalExpense / incVal.coerceAtLeast(1.0)).coerceIn(0.0, 1.0).toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = Teal600,
                trackColor = Slate200
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = income,
                onValueChange = { income = it },
                label = { Text(AppStrings.getString("monthly_income", language)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(10.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = schoolSavings,
                onValueChange = { schoolSavings = it },
                label = { Text(AppStrings.getString("school_savings", language) + " (Target ₹1,500/child)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(10.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = rent,
                    onValueChange = { rent = it },
                    label = { Text("Rent / Home") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )
                OutlinedTextField(
                    value = groceries,
                    onValueChange = { groceries = it },
                    label = { Text("Groceries") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = utilities,
                    onValueChange = { utilities = it },
                    label = { Text("Utilities / Power") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )
                OutlinedTextField(
                    value = transport,
                    onValueChange = { transport = it },
                    label = { Text("Transport") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = {
                    onSaveBudget(
                        incVal,
                        rent.toDoubleOrNull() ?: 0.0,
                        groceries.toDoubleOrNull() ?: 0.0,
                        utilities.toDoubleOrNull() ?: 0.0,
                        transport.toDoubleOrNull() ?: 0.0,
                        other.toDoubleOrNull() ?: 0.0,
                        schoolSavings.toDoubleOrNull() ?: 0.0
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = Teal600),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Budget Plan", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ExpensePredictorView(language: AppLanguage) {
    var books by remember { mutableStateOf(3500) }
    var uniforms by remember { mutableStateOf(4200) }
    var bus by remember { mutableStateOf(8000) }
    var exams by remember { mutableStateOf(1500) }
    var trips by remember { mutableStateOf(2500) }

    val totalAncillary = books + uniforms + bus + exams + trips

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(AppStrings.getString("expense_prediction", language), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Navy900)
            Text("Forecast non-tuition school expenditures for the academic session", fontSize = 12.sp, color = Slate600)

            Spacer(modifier = Modifier.height(14.dp))

            PredictorRow(
                label = AppStrings.getString("books_stationery", language),
                amount = books,
                onAdd = { books += 500 },
                onMinus = { if (books >= 500) books -= 500 }
            )
            PredictorRow(
                label = AppStrings.getString("uniform_kit", language),
                amount = uniforms,
                onAdd = { uniforms += 500 },
                onMinus = { if (uniforms >= 500) uniforms -= 500 }
            )
            PredictorRow(
                label = AppStrings.getString("school_bus", language),
                amount = bus,
                onAdd = { bus += 1000 },
                onMinus = { if (bus >= 1000) bus -= 1000 }
            )
            PredictorRow(
                label = AppStrings.getString("exam_fees", language),
                amount = exams,
                onAdd = { exams += 250 },
                onMinus = { if (exams >= 250) exams -= 250 }
            )
            PredictorRow(
                label = AppStrings.getString("school_trips", language),
                amount = trips,
                onAdd = { trips += 500 },
                onMinus = { if (trips >= 500) trips -= 500 }
            )

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = Slate200)
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(AppStrings.getString("total_estimated_extras", language), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Navy900)
                    Text("Tuition (₹18,000) + Ancillary (₹${totalAncillary}) = Total ₹${18000 + totalAncillary}", fontSize = 11.sp, color = Slate600)
                }
                Text("₹${totalAncillary}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Teal600)
            }
        }
    }
}

@Composable
fun PredictorRow(label: String, amount: Int, onAdd: () -> Unit, onMinus: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 12.sp, color = Slate700, modifier = Modifier.weight(1f))
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onMinus, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.RemoveCircleOutline, contentDescription = null, tint = Slate600, modifier = Modifier.size(18.dp))
            }
            Text("₹${amount}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Navy900, modifier = Modifier.width(60.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            IconButton(onClick = onAdd, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.AddCircleOutline, contentDescription = null, tint = Teal600, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
fun ScholarshipItemCard(
    scholarship: ScholarshipEntity,
    language: AppLanguage,
    onApply: () -> Unit
) {
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (language == AppLanguage.TAMIL) scholarship.titleTamil else scholarship.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Navy900
                    )
                    Text("Provider: ${scholarship.provider}", fontSize = 11.sp, color = Slate600)
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Teal100
                ) {
                    Text(
                        text = "₹${scholarship.awardAmount.toInt()}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Teal700,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Eligibility: " + if (language == AppLanguage.TAMIL) scholarship.eligibilityTamil else scholarship.eligibility,
                fontSize = 11.sp,
                color = Slate700,
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Slate100)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Deadline: ${scholarship.deadline}", fontSize = 11.sp, color = AmberWarning, fontWeight = FontWeight.Medium)

                if (scholarship.applicationStatus == "AVAILABLE") {
                    Button(
                        onClick = onApply,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Teal600),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text(AppStrings.getString("apply", language), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = EmeraldGreenContainer
                    ) {
                        Text(
                            text = scholarship.applicationStatus,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldGreen,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SupportRequestItemCard(
    request: SupportRequestEntity,
    language: AppLanguage
) {
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
                Text(
                    text = "Request: ${request.requestType}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Navy900
                )
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = when (request.status) {
                        "APPROVED" -> EmeraldGreenContainer
                        "REJECTED" -> RoseContainer
                        else -> AmberContainer
                    }
                ) {
                    Text(
                        text = request.status,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (request.status) {
                            "APPROVED" -> EmeraldGreen
                            "REJECTED" -> RoseError
                            else -> AmberWarning
                        },
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text("Student: ${request.studentName} | Requested Date: ${request.requestedExtensionDate}", fontSize = 11.sp, color = Slate600)
            Text("Reason: ${request.reason}", fontSize = 12.sp, color = Slate800)

            if (request.adminRemarks.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Slate100,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "School Desk: ${request.adminRemarks}",
                        fontSize = 11.sp,
                        color = Slate700,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SupportRequestDialog(
    language: AppLanguage,
    onDismiss: () -> Unit,
    onSubmit: (type: String, date: String, reason: String) -> Unit
) {
    var requestType by remember { mutableStateOf("EXTENSION") }
    var requestedDate by remember { mutableStateOf("2026-11-15") }
    var reason by remember { mutableStateOf("") }

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
                    text = AppStrings.getString("financial_support", language),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Navy900
                )
                Text(
                    text = "Submit a formal request to the school financial welfare desk",
                    fontSize = 11.sp,
                    color = Slate600
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text("Assistance Type", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Slate700)
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = requestType == "EXTENSION",
                        onClick = { requestType = "EXTENSION" },
                        label = { Text("Fee Extension", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = requestType == "CUSTOM_INSTALLMENT",
                        onClick = { requestType = "CUSTOM_INSTALLMENT" },
                        label = { Text("Split Installment", fontSize = 11.sp) }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = requestedDate,
                    onValueChange = { requestedDate = it },
                    label = { Text("Requested Payment Date (yyyy-mm-dd)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Explanation / Reason for Hardship") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) {
                        Text(AppStrings.getString("cancel", language), color = Slate600)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (reason.isBlank()) {
                                reason = "Temporary financial delay in family income."
                            }
                            onSubmit(requestType, requestedDate, reason)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Teal600),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(AppStrings.getString("confirm", language))
                    }
                }
            }
        }
    }
}
