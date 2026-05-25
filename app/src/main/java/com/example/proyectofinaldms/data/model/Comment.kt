package com.example.proyectofinaldms.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

/**
 * Comentario dejado por un usuario en un evento.
 * Subcolección: events/{eventId}/comments
 */
data class Comment(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userPhotoUrl: String = "",
    val text: String = "",
    val rating: Int = 0,           // Calificación de 1 a 5
    val createdAt: Timestamp = Timestamp.now()
)
