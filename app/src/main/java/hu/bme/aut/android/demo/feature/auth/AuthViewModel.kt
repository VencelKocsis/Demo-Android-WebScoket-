package hu.bme.aut.android.demo.feature.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.bme.aut.android.demo.data.auth.model.UserDTO
import hu.bme.aut.android.demo.domain.auth.repository.AuthRepository
import hu.bme.aut.android.demo.data.network.api.ApiService
import hu.bme.aut.android.demo.domain.auth.usecase.SignInUserUseCase
import hu.bme.aut.android.demo.domain.auth.usecase.SignOutUserUseCase
import hu.bme.aut.android.demo.domain.auth.usecases.ForgotPasswordUseCase
import hu.bme.aut.android.demo.domain.auth.usecases.RegisterUserUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// Adatmodell a bejelentkezési képernyő állapotához
data class AuthUiState(
    val emailInput: String = "",
    val passwordInput: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val isAuthenticated: Boolean = false,
    val currentUser: FirebaseUser? = null,
    val backendUser: UserDTO? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    // Megtartjuk az AuthRepository-t a getCurrentUser() hívása miatt (ami a ViewModel inicializálása)
    private val authRepository: AuthRepository,
    // Use Case-ek injektálása
    private val registerUserUseCase: RegisterUserUseCase,
    private val signInUserUseCase: SignInUserUseCase,
    private val signOutUserUseCase: SignOutUserUseCase,
    private val apiService: ApiService,
    private val registerFcmTokenUseCase: RegisterUserUseCase,
    private val forgotPasswordUseCase: ForgotPasswordUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState

    /**
     * Ezt a StateFlow-t használja a navigációs host (AppNavHost) a kezdőképernyő eldöntéséhez.
     */
    val authState: StateFlow<AuthState> = _uiState.map { uiState ->
        when {
            uiState.isAuthenticated -> AuthState.AUTHENTICATED
            uiState.isLoading -> AuthState.UNKNOWN // Ha tölt, még nem tudjuk a végső állapotot
            else -> AuthState.UNAUTHENTICATED
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000), // Megtartja az állapotot, amíg az UI látható
        initialValue = AuthState.UNKNOWN // Kezdeti állapot, amíg az ellenőrzés le nem fut
    )


    init {
        // 1. Kezdeti állapot ellenőrzése: Be van már jelentkezve a felhasználó?
        val currentUser = authRepository.getCurrentUser()
        if (currentUser != null) {
            _uiState.update { it.copy(isAuthenticated = true, currentUser = currentUser) }

            viewModelScope.launch {
                try {
                    val dto = UserDTO(id = 0, email = currentUser.email ?: "", firstName = "", lastName = "")
                    val backendUser = apiService.syncUser(dto)
                    _uiState.update { it.copy(backendUser = backendUser) }
                } catch (e: Exception) {
                    e.printStackTrace() // Kisebb hiba esetén nem léptetjük ki, de logolhatjuk
                }
            }
        }
    }

    fun getCurrentUser() = authRepository.getCurrentUser()

    fun registerFcmToken(email: String, token: String) {
        viewModelScope.launch {
            try {
                registerFcmTokenUseCase(email, token)
                Log.d("AuthViewModel", "✅ FCM Token sikeresen szinkronizálva a Ktor szerverrel!")
            } catch (e: Exception) {
                Log.e("AuthViewModel", "❌ FCM Token szinkronizálás SIKERTELEN: ${e.message}")
            }
        }
    }

    // Frissíti az e-mail beviteli mezőt
    fun updateEmail(email: String) {
        _uiState.update { it.copy(emailInput = email, error = null, successMessage = null) }
    }

    // Frissíti a jelszó beviteli mezőt
    fun updatePassword(password: String) {
        _uiState.update { it.copy(passwordInput = password, error = null, successMessage = null) }
    }

    /**
     * Törli az aktuális hibaüzenetet az UI állapotból.
     * Hasznos módváltáskor.
     */
    fun clearError() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }

    /**
     * ÚJ: Jelszó visszaállító e-mail küldése.
     */
    fun forgotPassword() {
        _uiState.update { it.copy(isLoading = true, error = null, successMessage = null) }

        viewModelScope.launch {
            val result = forgotPasswordUseCase(uiState.value.emailInput)

            result.onSuccess {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        successMessage = "Jelszó-visszaállító e-mail elküldve! Kérjük, ellenőrizd a fiókodat."
                    )
                }
            }.onFailure { exception ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = exception.message ?: "Ismeretlen hiba történt a küldéskor."
                    )
                }
            }
        }
    }

    /**
     * Regisztráció kezelése.
     * Csak meghívja a RegisterUserUseCase-t és kezeli a Result-ot.
     */
    fun register() {
        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            // A Use Case hívása
            val result = registerUserUseCase(
                email = uiState.value.emailInput,
                password = uiState.value.passwordInput
            )

            result.onSuccess { firebaseUser ->
                // 1. Firebase sikeres! Szinkronizáljunk a Ktorral!
                try {
                    val dto = UserDTO(
                        id = 0,
                        email = firebaseUser.email ?: "",
                        firstName = "Új",
                        lastName = "Játékos"
                    )

                    // 2. Ktor hívás (Az Interceptor itt már hozzáteszi a friss tokent!)
                    val backendUser = apiService.syncUser(dto)

                    // 3. Mentjük a teljes sikert
                    _uiState.update {
                        it.copy(
                            isLoading = false, isAuthenticated = true,
                            currentUser = firebaseUser, backendUser = backendUser
                        )
                    }

                } catch (e: Exception) {
                    _uiState.update { it.copy(isLoading = false, isAuthenticated = false, error = "Backend szinkronizációs hiba: ${e.message}") }
                }
            }.onFailure { exception ->
                _uiState.update { it.copy(isLoading = false, isAuthenticated = false, error = exception.message) }
            }
        }
    }

    /**
     * Bejelentkezés kezelése.
     * Csak meghívja a SignInUserUseCase-t és kezeli a Result-ot.
     */
    fun signIn() {
        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            // A Use Case hívása
            val result = signInUserUseCase(
                email = uiState.value.emailInput,
                password = uiState.value.passwordInput
            )

            result.onSuccess { firebaseUser ->
                // 1. Firebase sikeres! Szinkronizáljunk a Ktorral!
                try {
                    val dto = UserDTO(
                        id = 0,
                        email = firebaseUser.email ?: "",
                        firstName = "",
                        lastName = ""
                    )

                    // 2. Ktor hívás (Az Interceptor itt már hozzáteszi a friss tokent!)
                    val backendUser = apiService.syncUser(dto)

                    // 3. Mentjük a teljes sikert
                    _uiState.update {
                        it.copy(
                            isLoading = false, isAuthenticated = true,
                            currentUser = firebaseUser, backendUser = backendUser
                        )
                    }

                } catch (e: Exception) {
                    _uiState.update { it.copy(isLoading = false, isAuthenticated = false, error = "Backend szinkronizációs hiba: ${e.message}") }
                }

            }.onFailure { exception ->
                _uiState.update { it.copy(isLoading = false, isAuthenticated = false, error = exception.message) }
            }
        }
    }

    /**
     * Kijelentkezés.
     * Csak meghívja a SignOutUserUseCase-t és frissíti az UI állapotot.
     */
    fun signOut() {
        // A Use Case hívása
        signOutUserUseCase()

        _uiState.update {
            it.copy(
                isAuthenticated = false,
                currentUser = null,
                backendUser = null,
                emailInput = "",
                passwordInput = ""
            )
        }
    }
}
