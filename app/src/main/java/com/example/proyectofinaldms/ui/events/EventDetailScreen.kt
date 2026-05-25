package com.example.proyectofinaldms.ui.events

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyectofinaldms.data.model.Comment
import com.example.proyectofinaldms.ui.auth.AuthViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    eventId: String,
    viewModel: EventViewModel,
    authViewModel: AuthViewModel,
    onBack: () -> Unit,
    onEditEvent: () -> Unit
) {
    val event by viewModel.selectedEvent.collectAsState()
    val isAttending by viewModel.isAttending.collectAsState()
    val comments by viewModel.comments.collectAsState()
    val currentUser = authViewModel.currentUser
    val context = LocalContext.current

    var showCommentDialog by remember { mutableStateOf(false) }
    var commentText by remember { mutableStateOf("") }
    var commentRating by remember { mutableIntStateOf(5) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val sdf = remember { SimpleDateFormat("dd 'de' MMMM yyyy, HH:mm", Locale("es", "SV")) }

    LaunchedEffect(eventId) { viewModel.loadEvent(eventId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(event?.title ?: "Evento", maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Volver") }
                },
                actions = {
                    // Compartir
                    IconButton(onClick = {
                        event?.let { e ->
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, e.title)
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "¡Te invito al evento: ${e.title}!\n📅 ${sdf.format(e.date.toDate())}\n📍 ${e.location}\n${e.description}"
                                )
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Compartir evento"))
                        }
                    }) { Icon(Icons.Default.Share, "Compartir") }

                    // Editar / Eliminar (solo organizador)
                    if (event?.organizerId == currentUser?.uid) {
                        IconButton(onClick = onEditEvent) { Icon(Icons.Default.Edit, "Editar") }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, "Eliminar")
                        }
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                event?.let { e ->
                    // Info principal
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(e.title, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                            Spacer(Modifier.height(12.dp))

                            InfoRow(Icons.Default.CalendarToday, sdf.format(e.date.toDate()))
                            Spacer(Modifier.height(6.dp))
                            InfoRow(Icons.Default.LocationOn, e.location)
                            Spacer(Modifier.height(6.dp))
                            InfoRow(Icons.Default.Person, "Organiza: ${e.organizerName}")
                            Spacer(Modifier.height(12.dp))

                            Text(e.description, fontSize = 15.sp)

                            Spacer(Modifier.height(12.dp))
                            Row {
                                AssistChip(onClick = {}, label = { Text("👥 ${e.attendeesCount} confirmados") })
                                if (e.ratingsCount > 0) {
                                    Spacer(Modifier.width(8.dp))
                                    AssistChip(onClick = {}, label = {
                                        Text("⭐ %.1f (%d)".format(e.averageRating, e.ratingsCount))
                                    })
                                }
                            }
                        }
                    }

                    // RSVP
                    if (!e.isPast()) {
                        Button(
                            onClick = {
                                if (isAttending) viewModel.cancelAttendance(e.id)
                                else viewModel.confirmAttendance(e, currentUser?.displayName ?: "Usuario")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = if (isAttending)
                                ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            else ButtonDefaults.buttonColors()
                        ) {
                            Icon(
                                if (isAttending) Icons.Default.EventBusy else Icons.Default.EventAvailable,
                                null
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(if (isAttending) "Cancelar asistencia" else "Confirmar asistencia")
                        }
                    }

                    // Botón agregar comentario (solo si asistió o el evento ya pasó)
                    if (e.isPast() || isAttending) {
                        OutlinedButton(
                            onClick = { showCommentDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.RateReview, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Dejar comentario y calificación")
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    Text("Comentarios (${comments.size})", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            // Lista de comentarios
            if (comments.isEmpty()) {
                item {
                    Text(
                        "Aún no hay comentarios.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            } else {
                items(comments, key = { it.id }) { comment ->
                    CommentCard(comment = comment)
                }
            }
        }
    }

    // Dialog comentario
    if (showCommentDialog) {
        AlertDialog(
            onDismissRequest = { showCommentDialog = false },
            title = { Text("Agregar comentario") },
            text = {
                Column {
                    Text("Calificación: $commentRating ⭐")
                    Slider(
                        value = commentRating.toFloat(),
                        onValueChange = { commentRating = it.toInt() },
                        valueRange = 1f..5f,
                        steps = 3
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = commentText,
                        onValueChange = { commentText = it },
                        label = { Text("Tu comentario") },
                        maxLines = 4,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.addComment(
                        eventId = eventId,
                        text = commentText.trim(),
                        rating = commentRating,
                        userName = currentUser?.displayName ?: "Usuario",
                        userPhoto = currentUser?.photoUrl?.toString() ?: ""
                    )
                    showCommentDialog = false
                    commentText = ""
                }) { Text("Publicar") }
            },
            dismissButton = {
                TextButton(onClick = { showCommentDialog = false }) { Text("Cancelar") }
            }
        )
    }

    // Dialog confirmar eliminación
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("¿Eliminar evento?") },
            text = { Text("Esta acción no se puede deshacer.") },
            confirmButton = {
                Button(
                    onClick = { viewModel.deleteEvent(eventId); onBack() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(8.dp))
        Text(text, fontSize = 14.sp)
    }
}

@Composable
fun CommentCard(comment: Comment) {
    val sdf = remember { SimpleDateFormat("dd MMM yyyy", Locale("es", "SV")) }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(comment.userName, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Spacer(Modifier.weight(1f))
                Text(
                    "⭐".repeat(comment.rating.coerceIn(1, 5)),
                    fontSize = 12.sp
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(comment.text, fontSize = 14.sp)
            Spacer(Modifier.height(4.dp))
            Text(
                sdf.format(comment.createdAt.toDate()),
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
