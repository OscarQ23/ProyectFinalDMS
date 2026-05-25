package com.example.proyectofinaldms.data.repository

import com.example.proyectofinaldms.data.model.Comment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Repositorio de Comentarios y Calificaciones.
 * Los comentarios son subcolección de cada evento: events/{eventId}/comments
 */
class CommentRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    /** Obtener comentarios de un evento en tiempo real */
    fun getComments(eventId: String): Flow<List<Comment>> = callbackFlow {
        val listener = db.collection("events").document(eventId)
            .collection("comments")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val comments = snapshot?.toObjects(Comment::class.java)
                    ?.sortedByDescending { it.createdAt.seconds } ?: emptyList()
                trySend(comments)
            }
        awaitClose { listener.remove() }
    }

    /** Agregar un comentario con calificación */
    suspend fun addComment(
        eventId: String,
        text: String,
        rating: Int,
        userName: String,
        userPhotoUrl: String
    ): Result<Unit> {
        return try {
            val uid = auth.currentUser?.uid ?: return Result.failure(Exception("No autenticado"))
            val comment = Comment(
                userId = uid,
                userName = userName,
                userPhotoUrl = userPhotoUrl,
                text = text,
                rating = rating.coerceIn(1, 5)
            )
            val commentsRef = db.collection("events").document(eventId).collection("comments")
            commentsRef.add(comment) // sin await

            // Actualizar promedio de calificación en el evento
            updateEventRating(eventId, rating)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Recalcular promedio de calificación del evento */
    private suspend fun updateEventRating(eventId: String, newRating: Int) {
        try {
            val eventRef = db.collection("events").document(eventId)
            db.runTransaction { transaction ->
                val snapshot = transaction.get(eventRef)
                val currentAvg = snapshot.getDouble("averageRating") ?: 0.0
                val currentCount = snapshot.getLong("ratingsCount")?.toInt() ?: 0
                val newCount = currentCount + 1
                val newAvg = ((currentAvg * currentCount) + newRating) / newCount
                transaction.update(eventRef, "averageRating", newAvg)
                transaction.update(eventRef, "ratingsCount", newCount)
            }.await()
        } catch (_: Exception) { /* No crítico */ }
    }
}
