package com.example.proyectofinaldms.ui.notifications

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyectofinaldms.data.model.EventNotification
import com.example.proyectofinaldms.ui.events.EventViewModel
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    viewModel: EventViewModel,
    onBack: () -> Unit,
    onEventClick: (String) -> Unit
) {
    val notifications by viewModel.notifications.collectAsState()
    val currentUid = FirebaseAuth.getInstance().currentUser?.uid
    val sdf = remember { SimpleDateFormat("dd MMM, HH:mm", Locale("es", "SV")) }

    // Marcar todas como leídas al entrar
    LaunchedEffect(Unit) {
        viewModel.markAllNotificationsRead()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notificaciones") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                },
                actions = {
                    if (notifications.isNotEmpty()) {
                        IconButton(onClick = { viewModel.markAllNotificationsRead() }) {
                            Icon(Icons.Default.DoneAll, "Marcar todas como leídas")
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (notifications.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.NotificationsNone,
                        null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "No hay notificaciones aún",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(notifications, key = { it.id }) { notif ->
                    val isRead = currentUid != null && notif.readBy.contains(currentUid)
                    NotificationCard(
                        notification = notif,
                        isRead = isRead,
                        timeText = sdf.format(notif.createdAt.toDate()),
                        onClick = { onEventClick(notif.eventId) }
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationCard(
    notification: EventNotification,
    isRead: Boolean,
    timeText: String,
    onClick: () -> Unit
) {
    val containerColor = if (isRead)
        MaterialTheme.colorScheme.surface
    else
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (isRead) Icons.Default.NotificationsNone else Icons.Default.Notifications,
                null,
                tint = if (isRead) MaterialTheme.colorScheme.onSurfaceVariant
                       else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notification.eventTitle,
                    fontWeight = if (isRead) FontWeight.Normal else FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    text = notification.message,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = timeText,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (!isRead) {
                Spacer(Modifier.width(8.dp))
                Badge { Text("") }
            }
        }
    }
}
