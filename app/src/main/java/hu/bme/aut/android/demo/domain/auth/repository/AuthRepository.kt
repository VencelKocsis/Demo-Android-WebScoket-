package hu.bme.aut.android.demo.domain.auth.repository

import com.google.firebase.auth.FirebaseUser

interface AuthRepository {

    suspend fun registerUser(email: String, password: String): Result<FirebaseUser>

    suspend fun signInUser(email: String, password: String): Result<FirebaseUser>

    fun signOutUser()

    fun getCurrentUser(): FirebaseUser?
}