package hu.bme.aut.android.demo.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.bme.aut.android.demo.data.auth.model.UserDTO
import hu.bme.aut.android.demo.data.network.api.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val user: UserDTO? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState

    // Ezt majd a képernyő indulásakor vagy az AuthViewModelből kapjuk meg
    fun setUser(userDTO: UserDTO) {
        _uiState.update { it.copy(user = userDTO) }
    }

    fun updateUser(firstName: String, lastName: String) {
        val currentUser = uiState.value.user ?: return
        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            try {
                // 1. Összerakjuk a módosított DTO-t
                val updatedUser = currentUser.copy(firstName = firstName, lastName = lastName)

                // 2. Elküldjük a Ktornak mentésre (az AuthInterceptor automatikusan rárakja a tokent!)
                val savedUser = apiService.updateUser(updatedUser)

                // 3. Frissítjük a felületet a szervertől visszakapott végleges adatokkal
                _uiState.update { it.copy(isLoading = false, user = savedUser) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}