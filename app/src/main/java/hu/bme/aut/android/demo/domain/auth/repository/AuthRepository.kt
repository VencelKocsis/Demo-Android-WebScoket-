package hu.bme.aut.android.demo.domain.auth.repository

import com.google.firebase.auth.FirebaseUser
import hu.bme.aut.android.demo.domain.auth.model.User

interface AuthRepository {

    suspend fun registerUser(email: String, password: String): Result<FirebaseUser>
    suspend fun signInUser(email: String, password: String): Result<FirebaseUser>
    fun signOutUser()
    fun getCurrentUser(): FirebaseUser?
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>
    suspend fun getUserById(uid: String): User
    suspend fun updateUser(user: User): User
    suspend fun syncUser(user: User): User
}