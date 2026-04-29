package hu.bme.aut.android.demo.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.bme.aut.android.demo.domain.auth.usecases.SignInUserUseCase
import hu.bme.aut.android.demo.domain.auth.usecases.SignOutUserUseCase
import hu.bme.aut.android.demo.domain.auth.usecases.ForgotPasswordUseCase
import hu.bme.aut.android.demo.domain.auth.usecases.RegisterUserUseCase
import hu.bme.aut.android.demo.data.fcm.service.FcmTokenManager
import hu.bme.aut.android.demo.domain.auth.model.User
import hu.bme.aut.android.demo.domain.auth.usecases.GetCurrentUserUseCase
import hu.bme.aut.android.demo.domain.auth.usecases.SyncUserUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * A hitelesítési folyamatokért felelős ViewModel.
 * * Feladata: A [LoginScreen] felől érkező események (kattintások, gépelés) feldolgozása,
 * a megfelelő UseCase-ek (üzleti logika) meghívása, és az [AuthUiState] frissítése.
 * * A UI közvetlenül nem hívhatja a Repository-t, csak a ViewModelen (és a UseCase-eken) keresztül.
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val syncUserUseCase: SyncUserUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val registerUserUseCase: RegisterUserUseCase,
    private val signInUserUseCase: SignInUserUseCase,
    private val signOutUserUseCase: SignOutUserUseCase,
    private val forgotPasswordUseCase: ForgotPasswordUseCase,
    private val fcmTokenManager: FcmTokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState

    /**
     * Egy magasabb szintű, redukált állapot a navigáció (AppNavHost) számára.
     * Csak azt mondja meg, hogy a felhasználó bent van-e, vagy kint.
     */
    val authState: StateFlow<AuthState> = _uiState.map { uiState ->
        when {
            uiState.isAuthenticated -> AuthState.AUTHENTICATED
            uiState.isLoading -> AuthState.UNKNOWN
            else -> AuthState.UNAUTHENTICATED
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AuthState.UNKNOWN
    )

    init {
        // App indulásakor, ha be van jelentkezve, szinkronizáljuk a Usert és a Tokent is
        val currentUser = getCurrentUserUseCase()
        if (currentUser != null) {
            _uiState.update { it.copy(isAuthenticated = true, currentUser = currentUser) }

            viewModelScope.launch {
                try {
                    val userDomain = User(id = 0, email = currentUser.email ?: "", firstName = "", lastName = "")
                    val backendUser = syncUserUseCase(userDomain)

                    _uiState.update { it.copy(backendUser = backendUser) }

                    // Token szinkronizáció minden induláskor
                    currentUser.email?.let { email ->
                        fcmTokenManager.syncTokenWithServer(email)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun getCurrentUser() = getCurrentUserUseCase()

    fun updateEmail(email: String) {
        _uiState.update { it.copy(emailInput = email, error = null, successMessage = null) }
    }

    fun updatePassword(password: String) {
        _uiState.update { it.copy(passwordInput = password, error = null, successMessage = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }

    fun forgotPassword() {
        _uiState.update { it.copy(isLoading = true, error = null, successMessage = null) }

        viewModelScope.launch {
            val result = forgotPasswordUseCase(uiState.value.emailInput)
            result.onSuccess {
                _uiState.update { it.copy(isLoading = false, successMessage = "Jelszó-visszaállító e-mail elküldve! Kérjük, ellenőrizd a fiókodat.") }
            }.onFailure { exception ->
                _uiState.update { it.copy(isLoading = false, error = exception.message ?: "Ismeretlen hiba történt a küldéskor.") }
            }
        }
    }

    fun register() {
        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            val result = registerUserUseCase(email = uiState.value.emailInput, password = uiState.value.passwordInput)

            result.onSuccess { firebaseUser ->
                try {
                    val userDomain = User(id = 0, email = firebaseUser.email ?: "", firstName = "Új", lastName = "Játékos")
                    val backendUser = syncUserUseCase(userDomain)

                    _uiState.update {
                        it.copy(isLoading = false, isAuthenticated = true, currentUser = firebaseUser, backendUser = backendUser)
                    }

                    firebaseUser.email?.let { email ->
                        fcmTokenManager.syncTokenWithServer(email)
                    }

                } catch (e: Exception) {
                    _uiState.update { it.copy(isLoading = false, isAuthenticated = false, error = "Backend szinkronizációs hiba: ${e.message}") }
                }
            }.onFailure { exception ->
                _uiState.update { it.copy(isLoading = false, isAuthenticated = false, error = exception.message) }
            }
        }
    }

    fun signIn() {
        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            val result = signInUserUseCase(email = uiState.value.emailInput, password = uiState.value.passwordInput)

            result.onSuccess { firebaseUser ->
                try {
                    val userDomain = User(id = 0, email = firebaseUser.email ?: "", firstName = "", lastName = "")
                    val backendUser = syncUserUseCase(userDomain)

                    _uiState.update {
                        it.copy(isLoading = false, isAuthenticated = true, currentUser = firebaseUser, backendUser = backendUser)
                    }

                    firebaseUser.email?.let { email ->
                        fcmTokenManager.syncTokenWithServer(email)
                    }

                } catch (e: Exception) {
                    _uiState.update { it.copy(isLoading = false, isAuthenticated = false, error = "Backend szinkronizációs hiba: ${e.message}") }
                }

            }.onFailure { exception ->
                _uiState.update { it.copy(isLoading = false, isAuthenticated = false, error = exception.message) }
            }
        }
    }

    fun signOut() {
        signOutUserUseCase()
        _uiState.update {
            it.copy(isAuthenticated = false, currentUser = null, backendUser = null, emailInput = "", passwordInput = "")
        }
    }
}