package hu.bme.aut.android.demo.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.bme.aut.android.demo.domain.auth.repository.AuthRepository
import hu.bme.aut.android.demo.domain.auth.usecase.SignInUserUseCase
import hu.bme.aut.android.demo.domain.auth.usecase.SignOutUserUseCase
import hu.bme.aut.android.demo.domain.auth.usecases.RegisterUserUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// A navigációs események kezelésére szolgáló lambda (pl. navigálás a főképernyőre)
// Itt definiáljuk, hogy elkerüljük a Redeclaration hibát.
//typealias OnAuthSuccess = (FirebaseUser) -> Unit

// Adatmodell a bejelentkezési képernyő állapotához
data class AuthUiState(
    val emailInput: String = "",
    val passwordInput: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isAuthenticated: Boolean = false,
    val currentUser: FirebaseUser? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    // Megtartjuk az AuthRepository-t a getCurrentUser() hívása miatt (ami a ViewModel inicializálása)
    private val authRepository: AuthRepository,
    // Use Case-ek injektálása
    private val registerUserUseCase: RegisterUserUseCase,
    private val signInUserUseCase: SignInUserUseCase,
    private val signOutUserUseCase: SignOutUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState(currentUser = authRepository.getCurrentUser()))
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
        // Kezdeti állapot ellenőrzése (ha már be van jelentkezve)
        _uiState.update {
            it.copy(
                isAuthenticated = it.currentUser != null
            )
        }
    }

    // Frissíti az e-mail beviteli mezőt
    fun updateEmail(email: String) {
        _uiState.update { it.copy(emailInput = email, error = null) }
    }

    // Frissíti a jelszó beviteli mezőt
    fun updatePassword(password: String) {
        _uiState.update { it.copy(passwordInput = password, error = null) }
    }

    /**
     * Törli az aktuális hibaüzenetet az UI állapotból.
     * Hasznos módváltáskor.
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
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

            // Csak az állapotkezelés van itt
            _uiState.update { state ->
                result.fold(
                    onSuccess = { user ->
                        state.copy(
                            isLoading = false,
                            isAuthenticated = true,
                            currentUser = user,
                            error = null
                        )
                    },
                    onFailure = { exception ->
                        state.copy(
                            isLoading = false,
                            isAuthenticated = false,
                            error = exception.message
                        )
                    }
                )
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

            // Csak az állapotkezelés van itt
            _uiState.update { state ->
                result.fold(
                    onSuccess = { user ->
                        state.copy(
                            isLoading = false,
                            isAuthenticated = true,
                            currentUser = user,
                            error = null
                        )
                    },
                    onFailure = { exception ->
                        state.copy(
                            isLoading = false,
                            isAuthenticated = false,
                            error = exception.message
                        )
                    }
                )
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

        // Csak az állapotkezelés van itt
        _uiState.update {
            it.copy(
                isAuthenticated = false,
                currentUser = null,
                emailInput = "",
                passwordInput = ""
            )
        }
    }
}
