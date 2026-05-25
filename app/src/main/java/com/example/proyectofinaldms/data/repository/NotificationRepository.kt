package com.example.proyectofinaldms.data.repository

import com.example.proyectofinaldms.data.model.EventNotification
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class NotificationRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val notificationsCollection = db.collection("notifications")

    /** Escuchar todas las notificaciones en tiempo real */
    fun getNotifications(): Flow<List<EventNotification>> = callbackFlow {
        val listener = notificationsCollection
            .addSnapshotListener { snapshot, error ->
                if (error != null) { trySend(emptyList()); return@addSnapshotListener }
                val list = snapshot?.toObjects(EventNotification::class.java)
                    ?.sortedByDescending { it.createdAt.seconds } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    /** Crear notificación cuando se crea un evento */
    fun createEventNotification(eventId: String, eventTitle: String, organizerName: String) {
        val notification = EventNotification(
            eventId = eventId,
            eventTitle = eventTitle,
            organizerName = organizerName,
            message = "$organizerName creó un nuevo evento: $eventTitle"
        )
        notificationsCollection.add(notification)
    }

    /** Marcar notificación como leída por el usuario actual */
    fun markAsRead(notificationId: String) {
        val uid = auth.currentUser?.uid ?: return
        notificationsCollection.document(notificationId)
            .update("readBy", com.google.firebase.firestore.FieldValue.arrayUnion(uid))
    }

    /** Marcar todas como leídas */
    suspend fun markAllAsRead(notifications: List<EventNotification>) {
        val uid = auth.currentUser?.uid ?: return
        val batch = db.batch()
        notifications
            .filter { !it.readBy.contains(uid) }
            .forEach { notif ->
                batch.update(
                    notificationsCollection.document(notif.id),
                    "readBy", com.google.firebase.firestore.FieldValue.arrayUnion(uid)
                )
            }
        try { batch.commit().await() } catch (_: Exception) { }
    }
}
