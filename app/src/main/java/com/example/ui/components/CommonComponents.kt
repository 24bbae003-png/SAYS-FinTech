package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.StudentEntity
import com.example.ui.theme.*
import com.example.util.AppLanguage
import com.example.util.AppStrings

enum class NavigationTab(val titleKey: String) {
    DASHBOARD("dashboard"),
    SCHEDULE("schedule"),
    SAVINGS("savings"),
    HISTORY("history"),
    SERVICES("more")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EduFeeTopBar(
    language: AppLanguage,
    isAdminMode: Boolean,
    students: List<StudentEntity>,
    selectedStudentId: Long,
    unreadNotifications: Int,
    onLanguageToggle: () -> Unit,
    onAdminToggle: () -> Unit,
    onStudentSelected: (Long) -> Unit,
    onAddSiblingClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onSignOutClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.primary,
        contentColor = Color.White,
        modifier = modifier.fillMaxWidth(),
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Row 1: Logo & Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Teal500),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = "EduFee Logo",
                            tint = Navy900,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = AppStrings.getString("app_title", language),
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color.White
                        )
                        Text(
                            text = if (isAdminMode) AppStrings.getString("admin_portal", language)
                            else AppStrings.getString("app_subtitle", language),
                            fontSize = 11.sp,
                            color = Teal100
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Language Switcher Button
                    FilledTonalButton(
                        onClick = onLanguageToggle,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Navy700,
                            contentColor = Teal100
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("language_toggle_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Translate,
                            contentDescription = "Language Switcher",
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (language == AppLanguage.ENGLISH) "தமிழ்" else "EN",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Notifications Bell with Badge
                    IconButton(
                        onClick = onNotificationsClick,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("notifications_bell_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (unreadNotifications > 0) {
                                    Badge(containerColor = AmberWarning) {
                                        Text("$unreadNotifications", color = Navy900, fontSize = 10.sp)
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Notifications,
                                contentDescription = "Fee Reminders",
                                tint = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Mode Toggle (Parent / Admin)
                    FilterChip(
                        selected = isAdminMode,
                        onClick = onAdminToggle,
                        label = {
                            Text(
                                text = if (isAdminMode) "Admin" else "Parent",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = if (isAdminMode) Icons.Default.AdminPanelSettings else Icons.Default.FamilyRestroom,
                                contentDescription = "Role Mode",
                                modifier = Modifier.size(14.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Teal500,
                            selectedLabelColor = Navy900,
                            selectedLeadingIconColor = Navy900,
                            containerColor = Navy700,
                            labelColor = Color.White,
                            iconColor = Color.White
                        ),
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("role_mode_chip")
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = onSignOutClick,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("sign_out_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Sign Out",
                            tint = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Row 2: Sibling Switcher Bar (only when in Parent mode)
            AnimatedVisibility(visible = !isAdminMode) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // All siblings combined option
                        item {
                            StudentFilterPill(
                                label = AppStrings.getString("all_children", language),
                                initials = "ALL",
                                isSelected = selectedStudentId == 0L,
                                onClick = { onStudentSelected(0L) }
                            )
                        }

                        // Each sibling
                        items(students) { student ->
                            StudentFilterPill(
                                label = "${student.name} (${student.grade})",
                                initials = student.avatarInitials,
                                isSelected = selectedStudentId == student.id,
                                onClick = { onStudentSelected(student.id) }
                            )
                        }

                        // Add Sibling Button
                        item {
                            AssistChip(
                                onClick = onAddSiblingClick,
                                label = { Text(AppStrings.getString("add_child", language), fontSize = 11.sp) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Add Sibling",
                                        modifier = Modifier.size(14.dp)
                                    )
                                },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = Navy700.copy(alpha = 0.6f),
                                    labelColor = Teal100,
                                    leadingIconContentColor = Teal100
                                ),
                                modifier = Modifier.testTag("add_sibling_chip")
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StudentFilterPill(
    label: String,
    initials: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = if (isSelected) Teal500 else Navy700,
        contentColor = if (isSelected) Navy900 else Color.White,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .clickable(onClick = onClick)
            .testTag("student_filter_${initials}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) Navy900 else Teal500),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Teal500 else Navy900
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

@Composable
fun EduFeeBottomNav(
    currentTab: NavigationTab,
    onTabSelected: (NavigationTab) -> Unit,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        containerColor = Color.White,
        contentColor = Navy800,
        tonalElevation = 8.dp,
        windowInsets = WindowInsets.navigationBars,
        modifier = modifier
    ) {
        NavigationBarItem(
            selected = currentTab == NavigationTab.DASHBOARD,
            onClick = { onTabSelected(NavigationTab.DASHBOARD) },
            icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
            label = { Text(AppStrings.getString("dashboard", language), fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Navy900,
                selectedTextColor = Navy900,
                indicatorColor = Teal100
            ),
            modifier = Modifier.testTag("nav_dashboard")
        )

        NavigationBarItem(
            selected = currentTab == NavigationTab.SCHEDULE,
            onClick = { onTabSelected(NavigationTab.SCHEDULE) },
            icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Fee Schedule") },
            label = { Text(AppStrings.getString("schedule", language), fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Navy900,
                selectedTextColor = Navy900,
                indicatorColor = Teal100
            ),
            modifier = Modifier.testTag("nav_schedule")
        )

        NavigationBarItem(
            selected = currentTab == NavigationTab.SAVINGS,
            onClick = { onTabSelected(NavigationTab.SAVINGS) },
            icon = { Icon(Icons.Default.Savings, contentDescription = "Savings Planner") },
            label = { Text(AppStrings.getString("savings", language), fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Navy900,
                selectedTextColor = Navy900,
                indicatorColor = Teal100
            ),
            modifier = Modifier.testTag("nav_savings")
        )

        NavigationBarItem(
            selected = currentTab == NavigationTab.HISTORY,
            onClick = { onTabSelected(NavigationTab.HISTORY) },
            icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "Payments") },
            label = { Text(AppStrings.getString("history", language), fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Navy900,
                selectedTextColor = Navy900,
                indicatorColor = Teal100
            ),
            modifier = Modifier.testTag("nav_history")
        )

        NavigationBarItem(
            selected = currentTab == NavigationTab.SERVICES,
            onClick = { onTabSelected(NavigationTab.SERVICES) },
            icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Services") },
            label = { Text(AppStrings.getString("more", language), fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Navy900,
                selectedTextColor = Navy900,
                indicatorColor = Teal100
            ),
            modifier = Modifier.testTag("nav_services")
        )
    }
}

@Composable
fun EduFeeNavigationRail(
    currentTab: NavigationTab,
    onTabSelected: (NavigationTab) -> Unit,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    NavigationRail(
        containerColor = Color.White,
        contentColor = Navy800,
        header = {
            Box(
                modifier = Modifier
                    .padding(top = 16.dp, bottom = 12.dp)
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Teal500),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = "SAYS FinTech Logo",
                    tint = Navy900,
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        modifier = modifier
    ) {
        NavigationRailItem(
            selected = currentTab == NavigationTab.DASHBOARD,
            onClick = { onTabSelected(NavigationTab.DASHBOARD) },
            icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
            label = { Text(AppStrings.getString("dashboard", language), fontSize = 11.sp) },
            colors = NavigationRailItemDefaults.colors(
                selectedIconColor = Navy900,
                selectedTextColor = Navy900,
                indicatorColor = Teal100
            ),
            modifier = Modifier.testTag("rail_dashboard")
        )
        NavigationRailItem(
            selected = currentTab == NavigationTab.SCHEDULE,
            onClick = { onTabSelected(NavigationTab.SCHEDULE) },
            icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Fee Schedule") },
            label = { Text(AppStrings.getString("schedule", language), fontSize = 11.sp) },
            colors = NavigationRailItemDefaults.colors(
                selectedIconColor = Navy900,
                selectedTextColor = Navy900,
                indicatorColor = Teal100
            ),
            modifier = Modifier.testTag("rail_schedule")
        )
        NavigationRailItem(
            selected = currentTab == NavigationTab.SAVINGS,
            onClick = { onTabSelected(NavigationTab.SAVINGS) },
            icon = { Icon(Icons.Default.Savings, contentDescription = "Savings Planner") },
            label = { Text(AppStrings.getString("savings", language), fontSize = 11.sp) },
            colors = NavigationRailItemDefaults.colors(
                selectedIconColor = Navy900,
                selectedTextColor = Navy900,
                indicatorColor = Teal100
            ),
            modifier = Modifier.testTag("rail_savings")
        )
        NavigationRailItem(
            selected = currentTab == NavigationTab.HISTORY,
            onClick = { onTabSelected(NavigationTab.HISTORY) },
            icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "Payments") },
            label = { Text(AppStrings.getString("history", language), fontSize = 11.sp) },
            colors = NavigationRailItemDefaults.colors(
                selectedIconColor = Navy900,
                selectedTextColor = Navy900,
                indicatorColor = Teal100
            ),
            modifier = Modifier.testTag("rail_history")
        )
        NavigationRailItem(
            selected = currentTab == NavigationTab.SERVICES,
            onClick = { onTabSelected(NavigationTab.SERVICES) },
            icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Services") },
            label = { Text(AppStrings.getString("more", language), fontSize = 11.sp) },
            colors = NavigationRailItemDefaults.colors(
                selectedIconColor = Navy900,
                selectedTextColor = Navy900,
                indicatorColor = Teal100
            ),
            modifier = Modifier.testTag("rail_services")
        )
    }
}
