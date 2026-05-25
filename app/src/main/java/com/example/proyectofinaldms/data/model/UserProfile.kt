package com.example.proyectofinaldms.data.model

import com.google.firebase.firestore.DocumentId

/**
 * Perfil de usuario almacenado en Firestore (colección "users").
 * El ID del documento coincide con el UID de Firebase Auth.
 */
data class UserProfile(
    @DocumentId
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val isOrganizer: Boolean = false
)
