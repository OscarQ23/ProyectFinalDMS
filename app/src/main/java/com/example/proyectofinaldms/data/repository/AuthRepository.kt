package com.example.proyectofinaldms.data.repository

import com.example.proyectofinaldms.data.model.UserProfile
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Repositorio de autenticación.
 * Maneja login/registro con email+contraseña y Google Sign-In.
 */
class AuthRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")

    val currentUser: FirebaseUser? get() = auth.currentUser

    /** Registro con email y contraseña */
    suspend fun registerWithEmail(name: String, email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user!!
            // Actualizar displayName en Firebase Auth para que sea visible en toda la app
            val profileUpdate = UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build()
            user.updateProfile(profileUpdate).await()
            // Guardar perfil en Firestore sin bloquear — se sincroniza cuando haya conexión
            val profile = UserProfile(
                uid = user.uid,
                name = name,
                email = email,
                photoUrl = "",
                isOrganizer = false
            )
            usersCollection.document(user.uid).set(profile) // sin await
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Login con email y contraseña */
    suspend fun loginWithEmail(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            Result.success(result.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Login con cuenta de Google */
    suspend fun loginWithGoogle(account: GoogleSignInAccount): Result<FirebaseUser> {
        return try {
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            val result = auth.signInWithCredential(credential).await()
            val user = result.user!!
            // Guardar perfil en Firestore sin bloquear — fire and forget
            val profile = UserProfile(
                uid = user.uid,
                name = user.displayName ?: "",
                email = user.email ?: "",
                photoUrl = user.photoUrl?.toString() ?: "",
                isOrganizer = false
            )
            usersCollection.document(user.uid).set(profile) // sin await
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Obtener perfil del usuario actual */
    suspend fun getCurrentUserProfile(): Result<UserProfile> {
        return try {
            val uid = auth.currentUser?.uid ?: return Result.failure(Exception("No autenticado"))
            val doc = usersCollection.document(uid).get().await()
            val profile = doc.toObject(UserProfile::class.java)
                ?: return Result.failure(Exception("Perfil no encontrado"))
            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Cerrar sesión */
    fun signOut() {
        auth.signOut()
    }
}
