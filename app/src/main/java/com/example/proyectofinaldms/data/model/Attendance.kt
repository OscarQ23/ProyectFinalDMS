package com.example.proyectofinaldms.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

/**
 * Registro de asistencia (RSVP) de un usuario a un evento.
 * Colección: attendances — documentId: "{eventId}_{userId}"
 */
data class Attendance(
    @DocumentId
    val id: String = "",
    val eventId: String = "",
    val userId: String = "",
    val userName: String = "",
    val eventTitle: String = "",
    val confirmedAt: Timestamp = Timestamp.now(),
    val attended: Boolean = false   // true cuando el evento ya pasó
)
