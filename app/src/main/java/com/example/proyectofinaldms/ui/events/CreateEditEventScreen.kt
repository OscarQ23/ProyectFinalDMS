package com.example.proyectofinaldms.ui.events

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.proyectofinaldms.data.model.Event
import com.example.proyectofinaldms.ui.auth.AuthViewModel
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEditEventScreen(
    eventId: String?,
    viewModel: EventViewModel,
    authViewModel: AuthViewModel,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val existingEvent by viewModel.selectedEvent.collectAsState()
    val currentUser = authViewModel.currentUser
    val isEditing = eventId != null

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }

    // Calendario: fecha y hora seleccionadas
    val calendar = remember { Calendar.getInstance() }
    var selectedDateMillis by remember { mutableLongStateOf(calendar.timeInMillis) }
    var selectedHour by remember { mutableIntStateOf(calendar.get(Calendar.HOUR_OF_DAY)) }
    var selectedMinute by remember { mutableIntStateOf(calendar.get(Calendar.MINUTE)) }

    // Dialogs
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val sdfDate = remember { SimpleDateFormat("dd/MM/yyyy", Locale("es", "SV")) }
    val sdfFull = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "SV")) }

    // Texto legible de fecha y hora seleccionada
    val dateLabel = remember(selectedDateMillis) { sdfDate.format(Date(selectedDateMillis)) }
    val timeLabel = remember(selectedHour, selectedMinute) {
        "%02d:%02d".format(selectedHour, selectedMinute)
    }

    // DatePicker state
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDateMillis)

    // TimePicker state
    val timePickerState = rememberTimePickerState(
        initialHour = selectedHour,
        initialMinute = selectedMinute,
        is24Hour = true
    )

    // Cargar datos si es edición
    LaunchedEffect(eventId) {
        if (isEditing && eventId != null) viewModel.loadEvent(eventId)
    }
    LaunchedEffect(existingEvent) {
        if (isEditing && existingEvent != null) {
            val e = existingEvent!!
            title = e.title
            description = e.description
            location = e.location
            val cal = Calendar.getInstance().apply { time = e.date.toDate() }
            selectedDateMillis = cal.timeInMillis
            selectedHour = cal.get(Calendar.HOUR_OF_DAY)
            selectedMinute = cal.get(Calendar.MINUTE)
        }
    }

    // Navegar al guardar
    LaunchedEffect(uiState) {
        if (uiState is EventUiState.Success) {
            viewModel.resetState()
            onSaved()
        }
    }

    // ─── DatePickerDialog ───────────────────────────────────────────────────────
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { selectedDateMillis = it }
                    showDatePicker = false
                    showTimePicker = true  // abrir hora justo después
                }) { Text("Continuar →") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // ─── TimePickerDialog ───────────────────────────────────────────────────────
    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Seleccionar hora") },
            text = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TimePicker(state = timePickerState)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    selectedHour = timePickerState.hour
                    selectedMinute = timePickerState.minute
                    showTimePicker = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancelar") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Editar Evento" else "Crear Evento") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Volver") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Título del evento *") },
                leadingIcon = { Icon(Icons.Default.Title, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripción *") },
                leadingIcon = { Icon(Icons.Default.Description, null) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 6
            )

            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Ubicación *") },
                leadingIcon = { Icon(Icons.Default.LocationOn, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // ─── Selector de Fecha ──────────────────────────────────────────
            OutlinedTextField(
                value = "$dateLabel  $timeLabel",
                onValueChange = {},
                label = { Text("Fecha y hora del evento *") },
                leadingIcon = { Icon(Icons.Default.CalendarToday, null) },
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.EditCalendar, "Seleccionar fecha")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                singleLine = true
            )

            // Chips de acceso rápido a fecha / hora
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(
                    onClick = { showDatePicker = true },
                    label = { Text("📅 $dateLabel") },
                    leadingIcon = { Icon(Icons.Default.DateRange, null, Modifier.size(16.dp)) }
                )
                AssistChip(
                    onClick = { showTimePicker = true },
                    label = { Text("⏰ $timeLabel") },
                    leadingIcon = { Icon(Icons.Default.Schedule, null, Modifier.size(16.dp)) }
                )
            }

            if (uiState is EventUiState.Error) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Text(
                        (uiState as EventUiState.Error).message,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    // Combinar fecha seleccionada + hora seleccionada
                    val cal = Calendar.getInstance().apply {
                        timeInMillis = selectedDateMillis
                        set(Calendar.HOUR_OF_DAY, selectedHour)
                        set(Calendar.MINUTE, selectedMinute)
                        set(Calendar.SECOND, 0)
                    }
                    val event = Event(
                        id = existingEvent?.id ?: "",
                        title = title.trim(),
                        description = description.trim(),
                        location = location.trim(),
                        date = Timestamp(cal.time),
                        organizerId = currentUser?.uid ?: "",
                        organizerName = currentUser?.displayName ?: "Organizador",
                        attendeesCount = existingEvent?.attendeesCount ?: 0,
                        averageRating = existingEvent?.averageRating ?: 0.0,
                        ratingsCount = existingEvent?.ratingsCount ?: 0
                    )
                    if (isEditing) viewModel.updateEvent(event)
                    else viewModel.createEvent(event)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = uiState !is EventUiState.Loading &&
                        title.isNotBlank() && description.isNotBlank() && location.isNotBlank()
            ) {
                if (uiState is EventUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(if (isEditing) Icons.Default.Save else Icons.Default.Add, null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (isEditing) "Guardar cambios" else "Crear evento")
                }
            }
        }
    }
}
