package hu.bme.aut.android.demo.feature.auth

import com.google.firebase.auth.FirebaseUser
import hu.bme.aut.android.demo.domain.auth.model.User

/**
 * A bejelentkezési és regisztrációs képernyő (LoginScreen) teljes állapotát leíró adatmodell.
 * * Ez az egyetlen "igazságforrás" (Single Source of Truth) a UI számára.
 * * Külön fájlban tartása tisztán tartja a ViewModel-t és átláthatóbbá teszi az architektúrát.
 */
data class AuthUiState(
    val emailInput: String = "",
    val passwordInput: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val isAuthenticated: Boolean = false,
    val currentUser: FirebaseUser? = null,
    val backendUser: User? = null
)