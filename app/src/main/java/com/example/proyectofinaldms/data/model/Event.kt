package com.example.proyectofinaldms.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

/**
 * Modelo de datos para un Evento Comunitario.
 * Se almacena en la colección "events" de Firestore.
 */
data class Event(
    @DocumentId
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val location: String = "",
    val date: Timestamp = Timestamp.now(),
    val organizerId: String = "",
    val organizerName: String = "",
    val imageUrl: String = "",
    val attendeesCount: Int = 0,
    val averageRating: Double = 0.0,
    val ratingsCount: Int = 0,
    val createdAt: Timestamp = Timestamp.now()
) {
    /** Retorna true si el evento ya ocurrió (comparación por día, no por hora exacta) */
    fun isPast(): Boolean {
        val cal = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        return date.toDate().before(cal.time)
    }
}
