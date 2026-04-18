package com.rudra.savingbuddy.ui.screens.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rudra.savingbuddy.domain.model.AppNotification
import com.rudra.savingbuddy.domain.model.NotificationFilter
import com.rudra.savingbuddy.domain.model.NotificationType
import com.rudra.savingbuddy.util.DateUtils

private val sampleNotifications = listOf(
    AppNotification(
        id = 1,
        title = "Budget Alert",
        message = "You've used 80% of your monthly budget",
        type = NotificationType.BUDGET_ALERT,
        timestamp = System.currentTimeMillis() - 3600000
    ),
    AppNotification(
        id = 2,
        title = "Bill Reminder",
        message = "Electricity bill due in 2 days",
        type = NotificationType.BILL_REMINDER,
        timestamp = System.currentTimeMillis() - 7200000
    ),
    AppNotification(
        id = 3,
        title = "Savings Streak",
        message = "Great job! 5-day savings streak continued",
        type = NotificationType.SAVINGS_STREAK,
        timestamp = System.currentTimeMillis() - 86400000
    ),
    AppNotification(
        id = 4,
        title = "Recurring Income",
        message = "Salary of $3,000 has been added",
        type = NotificationType.RECURRING_TRANSACTION,
        timestamp = System.currentTimeMillis() - 172800000
    ),
    AppNotification(
        id = 5,
        title = "Achievement Unlocked",
        message = "First $1,000 saved!",
        type = NotificationType.ACHIEVEMENT,
        timestamp = System.currentTimeMillis() - 259200000
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen() {
    var notifications by remember { mutableStateOf(sampleNotifications) }
    var selectedFilter by remember { mutableStateOf<NotificationType?>(null) }
    var showFilterSheet by remember { mutableStateOf(false) }
    var showUnreadOnly by remember { mutableStateOf(false) }
    
    val filteredNotifications = notifications.filter { notification ->
        val typeFilter = selectedFilter == null || notification.type == selectedFilter
        val readFilter = !showUnreadOnly || !notification.isRead
        typeFilter && readFilter
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Notifications",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { showUnreadOnly = !showUnreadOnly }) {
                        Icon(
                            Icons.Default.FilterList,
                            contentDescription = "Filter unread",
                            tint = if (showUnreadOnly) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = { showFilterSheet = true }) {
                        Icon(Icons.Default.FilterAlt, contentDescription = "Filter by type")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Filter status
            if (selectedFilter != null || showUnreadOnly) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${filteredNotifications.size} notifications",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(onClick = {
                        selectedFilter = null
                        showUnreadOnly = false
                    }) {
                        Text("Clear filters")
                    }
                }
            }

            if (filteredNotifications.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.NotificationsNone,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No notifications",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredNotifications, key = { it.id }) { notification ->
                        NotificationCard(
                            notification = notification,
                            onMarkAsRead = {
                                notifications = notifications.map {
                                    if (it.id == notification.id) it.copy(isRead = !it.isRead) else it
                                }
                            },
                            onDelete = {
                                notifications = notifications.filter { it.id != notification.id }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Filter by Type",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                NotificationType.entries.forEach { type ->
                    ListItem(
                        headlineContent = { Text(type.name.replace("_", " ")) },
                        leadingContent = {
                            Icon(
                                getNotificationIcon(type),
                                contentDescription = null,
                                tint = getNotificationColor(type)
                            )
                        },
                        trailingContent = {
                            RadioButton(
                                selected = selectedFilter == type,
                                onClick = {
                                    selectedFilter = if (selectedFilter == type) null else type
                                    showFilterSheet = false
                                }
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        selectedFilter = null
                        showFilterSheet = false
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Clear Filter")
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun NotificationCard(
    notification: AppNotification,
    onMarkAsRead: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) 
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else 
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        getNotificationColor(notification.type).copy(alpha = 0.2f),
                        androidx.compose.foundation.shape.CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    getNotificationIcon(notification.type),
                    contentDescription = null,
                    tint = getNotificationColor(notification.type),
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (!notification.isRead) FontWeight.Bold else FontWeight.Normal
                    )
                    if (!notification.isRead) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    MaterialTheme.colorScheme.primary,
                                    androidx.compose.foundation.shape.CircleShape
                                )
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = DateUtils.formatDate(notification.timestamp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Row {
                        IconButton(
                            onClick = onMarkAsRead,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                if (notification.isRead) Icons.Default.MarkEmailUnread 
                                else Icons.Default.MarkEmailRead,
                                contentDescription = "Toggle read",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

fun getNotificationIcon(type: NotificationType) = when (type) {
    NotificationType.BUDGET_ALERT -> Icons.Default.Warning
    NotificationType.BILL_REMINDER -> Icons.Default.Receipt
    NotificationType.GOAL_COMPLETE -> Icons.Default.Flag
    NotificationType.SAVINGS_STREAK -> Icons.Default.LocalFireDepartment
    NotificationType.ACHIEVEMENT -> Icons.Default.EmojiEvents
    NotificationType.RECURRING_TRANSACTION -> Icons.Default.Repeat
    NotificationType.SYSTEM -> Icons.Default.Info
}

fun getNotificationColor(type: NotificationType): Color = when (type) {
    NotificationType.BUDGET_ALERT -> Color(0xFFFF9800)
    NotificationType.BILL_REMINDER -> Color(0xFFF44336)
    NotificationType.GOAL_COMPLETE -> Color(0xFF4CAF50)
    NotificationType.SAVINGS_STREAK -> Color(0xFFFF5722)
    NotificationType.ACHIEVEMENT -> Color(0xFFFFD700)
    NotificationType.RECURRING_TRANSACTION -> Color(0xFF2196F3)
    NotificationType.SYSTEM -> Color(0xFF9E9E9E)
}