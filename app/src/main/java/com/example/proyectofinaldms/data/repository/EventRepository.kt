package com.example.proyectofinaldms.data.repository

import com.example.proyectofinaldms.data.model.Attendance
import com.example.proyectofinaldms.data.model.Event
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map

/**
 * Repositorio de Eventos y Asistencias.
 * Usa Firestore en tiempo real con Flow.
 * Todos los errores se manejan silenciosamente para evitar crashes.
 */
class EventRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val eventsCollection = db.collection("events")
    private val attendancesCollection = db.collection("attendances")

    // ─── Eventos ───────────────────────────────────────────────────────────────

    /** Todos los eventos en tiempo real — sin orderBy para evitar requerir índices */
    private fun getAllEvents(): Flow<List<Event>> = callbackFlow {
        val listener = eventsCollection
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                // Ordenar del lado del cliente, no en Firestore
                val events = snapshot?.toObjects(Event::class.java)
                    ?.sortedBy { it.date.seconds } ?: emptyList()
                trySend(events)
            }
        awaitClose { listener.remove() }
    }

    fun getUpcomingEvents(): Flow<List<Event>> = getAllEvents().map { events ->
        events.filter { !it.isPast() }
    }

    fun getPastEvents(): Flow<List<Event>> = getAllEvents().map { events ->
        events.filter { it.isPast() }.sortedByDescending { it.date.seconds }
    }

    /** Evento individual en tiempo real */
    fun getEventByIdFlow(eventId: String): Flow<Event?> = callbackFlow {
        val listener = eventsCollection.document(eventId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(null)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObject(Event::class.java))
            }
        awaitClose { listener.remove() }
    }

    /** Asistencia del usuario actual en tiempo real */
    fun isAttendingFlow(eventId: String): Flow<Boolean> = callbackFlow {
        val uid = auth.currentUser?.uid ?: run { trySend(false); close(); return@callbackFlow }
        val listener = attendancesCollection.document("${eventId}_$uid")
            .addSnapshotListener { snapshot, _ ->
                trySend(snapshot?.exists() == true)
            }
        awaitClose { listener.remove() }
    }

    /** Historial del usuario actual */
    fun getUserHistory(): Flow<List<Attendance>> = callbackFlow {
        val uid = auth.currentUser?.uid ?: run { trySend(emptyList()); close(); return@callbackFlow }
        val listener = attendancesCollection
            .whereEqualTo("userId", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.toObjects(Attendance::class.java)
                    ?.sortedByDescending { it.confirmedAt.seconds } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    /** Eventos creados por el usuario actual */
    fun getMyEvents(): Flow<List<Event>> = callbackFlow {
        val uid = auth.currentUser?.uid ?: run { trySend(emptyList()); close(); return@callbackFlow }
        val listener = eventsCollection
            .whereEqualTo("organizerId", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val events = snapshot?.toObjects(Event::class.java)
                    ?.sortedByDescending { it.date.seconds } ?: emptyList()
                trySend(events)
            }
        awaitClose { listener.remove() }
    }

    // ─── CRUD ──────────────────────────────────────────────────────────────────

    suspend fun createEvent(event: Event): Result<String> {
        return try {
            val docRef = eventsCollection.document()
            val eventWithId = event.copy(id = docRef.id)
            docRef.set(eventWithId)
            // Crear notificación para todos los usuarios
            NotificationRepository().createEventNotification(
                eventId = docRef.id,
                eventTitle = event.title,
                organizerName = event.organizerName
            )
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateEvent(event: Event): Result<Unit> {
        return try {
            eventsCollection.document(event.id).set(event)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteEvent(eventId: String): Result<Unit> {
        return try {
            eventsCollection.document(eventId).delete()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ─── RSVP ──────────────────────────────────────────────────────────────────

    suspend fun confirmAttendance(event: Event, userName: String): Result<Unit> {
        return try {
            val uid = auth.currentUser?.uid ?: return Result.failure(Exception("No autenticado"))
            val attendanceId = "${event.id}_$uid"
            val attendance = Attendance(
                id = attendanceId,
                eventId = event.id,
                userId = uid,
                userName = userName,
                eventTitle = event.title
            )
            attendancesCollection.document(attendanceId).set(attendance)
            eventsCollection.document(event.id)
                .update("attendeesCount", event.attendeesCount + 1)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun cancelAttendance(eventId: String): Result<Unit> {
        return try {
            val uid = auth.currentUser?.uid ?: return Result.failure(Exception("No autenticado"))
            attendancesCollection.document("${eventId}_$uid").delete()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
