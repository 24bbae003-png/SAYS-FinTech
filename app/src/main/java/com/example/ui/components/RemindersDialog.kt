package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.example.data.local.entity.NotificationEntity
import com.example.ui.theme.*
import com.example.util.AppLanguage
import com.example.util.AppStrings

@Composable
fun RemindersDialog(
    notifications: List<NotificationEntity>,
    language: AppLanguage,
    onDismiss: () -> Unit,
    onMarkRead: (Long) -> Unit,
    onMarkAllRead: () -> Unit
) {
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            shadowElevation = 16.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                // Header
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
                                .background(AmberContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.Notifications, contentDescription = null, tint = AmberWarning, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = AppStrings.getString("notifications", language),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Navy900
                            )
                            Text(
                                text = "30d, 15d, 7d, 3d & Due Date Alerts",
                                fontSize = 11.sp,
                                color = Slate600
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Slate600)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${notifications.count { !it.isRead }} Unread Alerts",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Slate700
                    )
                    TextButton(
                        onClick = onMarkAllRead,
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text("Mark all read", fontSize = 11.sp, color = Teal600)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = Slate100)
                Spacer(modifier = Modifier.height(8.dp))

                if (notifications.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("No notifications scheduled.", fontSize = 12.sp, color = Slate600)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(notifications) { notification ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (notification.isRead) Slate50 else Teal50.copy(alpha = 0.5f),
                                border = if (!notification.isRead) androidx.compose.foundation.BorderStroke(1.dp, Teal100) else null,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = notification.title,
                                            fontSize = 13.sp,
                                            fontWeight = if (notification.isRead) FontWeight.Medium else FontWeight.Bold,
                                            color = Navy900
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = when (notification.daysBefore) {
                                                7, 3 -> AmberContainer
                                                0 -> RoseContainer
                                                else -> Slate200
                                            }
                                        ) {
                                            Text(
                                                text = if (notification.daysBefore == 0) "DUE TODAY" else "${notification.daysBefore}d left",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = when (notification.daysBefore) {
                                                    7, 3 -> AmberWarning
                                                    0 -> RoseError
                                                    else -> Slate700
                                                },
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = notification.message,
                                        fontSize = 11.sp,
                                        color = Slate700,
                                        lineHeight = 15.sp
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(notification.dateString, fontSize = 10.sp, color = Slate600)

                                        Row {
                                            TextButton(
                                                onClick = {
                                                    com.example.util.NotificationHelper.showReminderNotification(
                                                        context = context,
                                                        title = notification.title,
                                                        message = notification.message
                                                    )
                                                    Toast.makeText(context, "System notification alert posted & synced!", Toast.LENGTH_SHORT).show()
                                                },
                                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Icon(Icons.Outlined.Alarm, contentDescription = null, modifier = Modifier.size(13.dp), tint = Teal600)
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text("Set Alarm", fontSize = 10.sp, color = Teal600)
                                            }

                                            if (!notification.isRead) {
                                                TextButton(
                                                    onClick = { onMarkRead(notification.id) },
                                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Icon(Icons.Outlined.Check, contentDescription = null, modifier = Modifier.size(13.dp), tint = Slate600)
                                                    Spacer(modifier = Modifier.width(3.dp))
                                                    Text("Dismiss", fontSize = 10.sp, color = Slate600)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Navy800),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close", fontSize = 13.sp)
                }
            }
        }
    }
}
