package hu.bme.aut.android.demo.data.auth.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import hu.bme.aut.android.demo.domain.auth.repository.AuthRepository
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth
) : AuthRepository {
    override suspend fun registerUser(email: String, password: String): Result<FirebaseUser> {
        return try {
            val userCredential = auth.createUserWithEmailAndPassword(email, password).await()
            userCredential.user?.let { Result.success(it) }
                ?: Result.failure(Exception("User registration failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signInUser(email: String, password: String): Result<FirebaseUser> {
        return try {
            val userCredential = auth.signInWithEmailAndPassword(email, password).await()
            userCredential.user?.let { Result.success(it) }
                ?: Result.failure(Exception("User login failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun signOutUser() {
        auth.signOut()
    }

    override fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }
}