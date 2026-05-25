package com.example.proyectofinaldms.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

/**
 * Notificación generada cuando se crea un evento nuevo.
 * Colección: "notifications"
 */
data class EventNotification(
    @DocumentId
    val id: String = "",
    val eventId: String = "",
    val eventTitle: String = "",
    val organizerName: String = "",
    val message: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val readBy: List<String> = emptyList()  // UIDs que ya la leyeron
)
