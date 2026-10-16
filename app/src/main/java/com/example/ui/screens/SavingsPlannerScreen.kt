package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.FeeViewModel
import com.example.util.AppLanguage
import com.example.util.AppStrings
import kotlin.math.max

@Composable
fun SavingsPlannerScreen(
    viewModel: FeeViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val language by viewModel.language.collectAsState()
    val totalAnnual by viewModel.totalAnnualFee.collectAsState()
    val totalPending by viewModel.totalPendingFees.collectAsState()
    val currentSaved by viewModel.currentSavingsPot.collectAsState()
    val students by viewModel.students.collectAsState()
    val selectedStudentId by viewModel.selectedStudentId.collectAsState()

    var monthsRemaining by remember { mutableStateOf(8f) }
    var customDepositAmount by remember { mutableStateOf("1500") }

    // Dynamic calculated monthly target:
    // (Total Pending Fees - Current Savings) / Months Remaining
    val calculatedMonthly = remember(totalPending, currentSaved, monthsRemaining) {
        val needed = max(0.0, totalPending - currentSaved)
        val monthly = needed / monthsRemaining.coerceAtLeast(1f)
        ((monthly / 50).toInt() * 50).coerceAtLeast(500)
    }

    val activeStudent = students.find { it.id == selectedStudentId }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Slate50),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Hero Recommendation Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Navy800),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = AppStrings.getString("recommended_savings", language),
                                fontSize = 13.sp,
                                color = Teal100
                            )
                            Text(
                                text = "₹${calculatedMonthly}/month",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Teal500),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Savings, contentDescription = null, tint = Navy900, modifier = Modifier.size(28.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Standard benchmark: ₹18,000 annual fee ÷ 12 months = ₹1,500/month. Based on your remaining pending fees of ₹${totalPending.toInt()} and current savings of ₹${currentSaved.toInt()}, saving ₹${calculatedMonthly}/mo will fully cover upcoming terms.",
                        fontSize = 12.sp,
                        color = Slate200,
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Navy700)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Savings pot status
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Current Savings Pot", fontSize = 12.sp, color = Teal100)
                        Text("₹${currentSaved.toInt()} of ₹${totalAnnual.toInt()}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    val progress = (currentSaved / totalAnnual.coerceAtLeast(1.0)).coerceIn(0.0, 1.0).toFloat()
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = Teal500,
                        trackColor = Navy700
                    )
                }
            }
        }

        // Quick Deposit & Withdraw Section
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Manage Savings Pot",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Navy900
                    )
                    Text(
                        text = "Set aside monthly money before school fee due dates",
                        fontSize = 12.sp,
                        color = Slate600
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.depositSavings(500.0)
                                Toast.makeText(context, "Added ₹500 to savings pot", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Teal600),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("+ ₹500", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                viewModel.depositSavings(1000.0)
                                Toast.makeText(context, "Added ₹1,000 to savings pot", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Teal600),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("+ ₹1,000", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                viewModel.depositSavings(1500.0)
                                Toast.makeText(context, "Added ₹1,500 to savings pot", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Navy800),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1.2f)
                        ) {
                            Text("+ ₹1,500 (Rec)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Custom Deposit / Withdraw input
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = customDepositAmount,
                            onValueChange = { customDepositAmount = it },
                            label = { Text("Custom Amount (₹)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("custom_deposit_input"),
                            shape = RoundedCornerShape(10.dp)
                        )

                        FilledTonalButton(
                            onClick = {
                                val amt = customDepositAmount.toDoubleOrNull() ?: 0.0
                                if (amt > 0) {
                                    viewModel.depositSavings(amt)
                                    Toast.makeText(context, "Deposited ₹${amt.toInt()}!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(containerColor = Teal100, contentColor = Teal700)
                        ) {
                            Text("Deposit")
                        }

                        OutlinedButton(
                            onClick = {
                                val amt = customDepositAmount.toDoubleOrNull() ?: 0.0
                                if (amt > 0) {
                                    viewModel.withdrawSavings(amt)
                                    Toast.makeText(context, "Withdrawn ₹${amt.toInt()} for school fees", Toast.LENGTH_SHORT).show()
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = RoseError)
                        ) {
                            Text("Withdraw")
                        }
                    }
                }
            }
        }

        // Interactive Adjustment Simulator
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Dynamic Timeline Adjustment",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Navy900
                    )
                    Text(
                        text = "Adjust months remaining to recalculate required monthly contribution",
                        fontSize = 12.sp,
                        color = Slate600
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Months Remaining:", fontSize = 13.sp, color = Slate700)
                        Text("${monthsRemaining.toInt()} months", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Teal700)
                    }

                    Slider(
                        value = monthsRemaining,
                        onValueChange = { monthsRemaining = it },
                        valueRange = 1f..12f,
                        steps = 10,
                        colors = SliderDefaults.colors(thumbColor = Teal600, activeTrackColor = Teal600)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Teal50,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Monthly Target at ${monthsRemaining.toInt()} months:", fontSize = 12.sp, color = Navy900)
                            Text("₹${calculatedMonthly}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Teal700)
                        }
                    }
                }
            }
        }

        // Installment Coverage Milestones
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Installment Coverage Readiness",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Navy900
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    MilestoneRow(
                        title = "Term 1 (June) - ₹6,000",
                        status = "Paid in full (Cleared)",
                        isComplete = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    MilestoneRow(
                        title = "Term 2 (October) - ₹4,000 due",
                        status = if (currentSaved >= 4000) "Ready to pay from savings pot" else "₹${(4000 - currentSaved).toInt().coerceAtLeast(0)} more needed",
                        isComplete = currentSaved >= 4000
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    MilestoneRow(
                        title = "Term 3 (January) - ₹6,000 due",
                        status = "Planned via ₹1,500/month recurring savings",
                        isComplete = false
                    )
                }
            }
        }
    }
}

@Composable
fun MilestoneRow(title: String, status: String, isComplete: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isComplete) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (isComplete) EmeraldGreen else AmberWarning,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Navy900)
            Text(status, fontSize = 11.sp, color = if (isComplete) EmeraldGreen else Slate600)
        }
    }
}
