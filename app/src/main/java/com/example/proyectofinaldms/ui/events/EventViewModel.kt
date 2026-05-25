package com.example.proyectofinaldms.ui.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectofinaldms.data.model.Attendance
import com.example.proyectofinaldms.data.model.Event
import com.example.proyectofinaldms.data.model.Comment
import com.example.proyectofinaldms.data.model.EventNotification
import com.example.proyectofinaldms.data.repository.CommentRepository
import com.example.proyectofinaldms.data.repository.EventRepository
import com.example.proyectofinaldms.data.repository.NotificationRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class EventUiState {
    object Idle : EventUiState()
    object Loading : EventUiState()
    object Success : EventUiState()
    data class Error(val message: String) : EventUiState()
}

class EventViewModel : ViewModel() {

    private val eventRepo = EventRepository()
    private val commentRepo = CommentRepository()
    private val notifRepo = NotificationRepository()

    // Trigger que se incrementa cada vez que un usuario inicia sesión
    // Esto hace que todos los flows se reinicien con listeners frescos de Firestore
    private val _authTrigger = MutableStateFlow(0)

    init {
        FirebaseAuth.getInstance().addAuthStateListener { auth ->
            if (auth.currentUser != null) {
                _authTrigger.value++
            }
        }
    }

    // ─── Listas de eventos (se reinician con cada login) ───────────────────────
    val upcomingEvents: StateFlow<List<Event>> = _authTrigger
        .flatMapLatest { eventRepo.getUpcomingEvents() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val pastEvents: StateFlow<List<Event>> = _authTrigger
        .flatMapLatest { eventRepo.getPastEvents() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val myEvents: StateFlow<List<Event>> = _authTrigger
        .flatMapLatest { eventRepo.getMyEvents() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val userHistory: StateFlow<List<Attendance>> = _authTrigger
        .flatMapLatest { eventRepo.getUserHistory() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // ─── Notificaciones ────────────────────────────────────────────────────────
    val notifications: StateFlow<List<EventNotification>> = _authTrigger
        .flatMapLatest { notifRepo.getNotifications() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /** Cantidad de notificaciones no leídas por el usuario actual */
    val unreadCount: StateFlow<Int> = combine(
        notifications,
        _authTrigger
    ) { notifs, _ ->
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@combine 0
        notifs.count { !it.readBy.contains(uid) }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    fun markAllNotificationsRead() {
        viewModelScope.launch {
            notifRepo.markAllAsRead(notifications.value)
        }
    }

    // ─── Estado de operaciones ─────────────────────────────────────────────────
    private val _uiState = MutableStateFlow<EventUiState>(EventUiState.Idle)
    val uiState: StateFlow<EventUiState> = _uiState

    // ─── Evento seleccionado ───────────────────────────────────────────────────
    private val _selectedEvent = MutableStateFlow<Event?>(null)
    val selectedEvent: StateFlow<Event?> = _selectedEvent

    private val _isAttending = MutableStateFlow(false)
    val isAttending: StateFlow<Boolean> = _isAttending

    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments: StateFlow<List<Comment>> = _comments

    // ─── Búsqueda ──────────────────────────────────────────────────────────────
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    val filteredEvents: StateFlow<List<Event>> = combine(upcomingEvents, _searchQuery) { events, query ->
        if (query.isBlank()) events
        else events.filter {
            it.title.contains(query, ignoreCase = true) ||
                    it.location.contains(query, ignoreCase = true) ||
                    it.description.contains(query, ignoreCase = true)
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun setSearchQuery(q: String) { _searchQuery.value = q }

    // ─── Cargar evento individual ──────────────────────────────────────────────
    fun loadEvent(eventId: String) {
        viewModelScope.launch {
            eventRepo.getEventByIdFlow(eventId).collect { event ->
                _selectedEvent.value = event
            }
        }
        viewModelScope.launch {
            eventRepo.isAttendingFlow(eventId).collect { attending ->
                _isAttending.value = attending
            }
        }
        viewModelScope.launch {
            commentRepo.getComments(eventId).collect { list ->
                _comments.value = list
            }
        }
    }

    // ─── CRUD ──────────────────────────────────────────────────────────────────
    fun createEvent(event: Event) {
        viewModelScope.launch {
            _uiState.value = EventUiState.Loading
            val result = eventRepo.createEvent(event)
            _uiState.value = if (result.isSuccess) EventUiState.Success
            else EventUiState.Error(result.exceptionOrNull()?.message ?: "Error al crear")
        }
    }

    fun updateEvent(event: Event) {
        viewModelScope.launch {
            _uiState.value = EventUiState.Loading
            val result = eventRepo.updateEvent(event)
            _uiState.value = if (result.isSuccess) EventUiState.Success
            else EventUiState.Error(result.exceptionOrNull()?.message ?: "Error al actualizar")
        }
    }

    fun deleteEvent(eventId: String) {
        viewModelScope.launch {
            _uiState.value = EventUiState.Loading
            val result = eventRepo.deleteEvent(eventId)
            _uiState.value = if (result.isSuccess) EventUiState.Success
            else EventUiState.Error(result.exceptionOrNull()?.message ?: "Error al eliminar")
        }
    }

    fun confirmAttendance(event: Event, userName: String) {
        viewModelScope.launch {
            eventRepo.confirmAttendance(event, userName)
        }
    }

    fun cancelAttendance(eventId: String) {
        viewModelScope.launch {
            eventRepo.cancelAttendance(eventId)
        }
    }

    fun addComment(eventId: String, text: String, rating: Int, userName: String, userPhoto: String) {
        viewModelScope.launch {
            commentRepo.addComment(eventId, text, rating, userName, userPhoto)
        }
    }

    fun resetState() { _uiState.value = EventUiState.Idle }
}
