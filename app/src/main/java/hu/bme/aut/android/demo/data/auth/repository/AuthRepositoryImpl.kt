package hu.bme.aut.android.demo.data.auth.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import hu.bme.aut.android.demo.data.network.api.auth.AuthApiService
import hu.bme.aut.android.demo.domain.auth.model.User
import hu.bme.aut.android.demo.domain.auth.repository.AuthRepository
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Az [AuthRepository] interfész megvalósítása a Data rétegben.
 * * Feladata a hitelesítési folyamatok (Firebase) és a backend szerverrel
 * való felhasználói adatszinkronizáció (Ktor API) kezelése.
 * Ez az osztály fordít (tolmácsol) a hálózati DTO-k és a Domain modellek között.
 */
class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val authApiService: AuthApiService
) : AuthRepository {

    /**
     * Új felhasználó regisztrálása a Firebase Authentication segítségével.
     */
    override suspend fun registerUser(email: String, password: String): Result<FirebaseUser> {
        return try {
            val userCredential = auth.createUserWithEmailAndPassword(email, password).await()
            userCredential.user?.let { Result.success(it) }
                ?: Result.failure(Exception("Sikertelen regisztráció (Firebase null user)"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Meglévő felhasználó bejelentkeztetése.
     */
    override suspend fun signInUser(email: String, password: String): Result<FirebaseUser> {
        return try {
            val userCredential = auth.signInWithEmailAndPassword(email, password).await()
            userCredential.user?.let { Result.success(it) }
                ?: Result.failure(Exception("Sikertelen bejelentkezés (Firebase null user)"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Kijelentkezteti az aktuális Firebase felhasználót az eszközről.
     */
    override fun signOutUser() {
        auth.signOut()
    }

    /**
     * Visszaadja a jelenleg bejelentkezett Firebase felhasználót, vagy null-t, ha nincs.
     */
    override fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    /**
     * Jelszó-visszaállító e-mailt küld a Firebase beépített rendszerén keresztül.
     */
    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- BACKEND (API) HÍVÁSOK ---

    /**
     * Lekérdezi a felhasználót a saját backendünkről a Firebase UID alapján.
     */
    override suspend fun getUserById(uid: String): User {
        return authApiService.getUserById(uid)
    }

    /**
     * Frissíti a felhasználó adatait a backendünkön.
     * majd a választ visszakonvertálja.
     */
    override suspend fun updateUser(user: User): User {
        return authApiService.updateUser(user)
    }
    /**
     * Szinkronizálja a bejelentkezett felhasználó adatait az adatbázissal.
     * Ha a felhasználó még nem létezik a backend adatbázisban, akkor létrehozza.
     */
    override suspend fun syncUser(user: User): User {
        return authApiService.syncUser(user)
    }
}