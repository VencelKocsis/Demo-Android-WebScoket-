package hu.bme.aut.android.demo.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.bme.aut.android.demo.data.auth.model.UserDTO
import hu.bme.aut.android.demo.data.network.api.ApiService
import hu.bme.aut.android.demo.domain.auth.usecases.GetCurrentUserUseCase
import hu.bme.aut.android.demo.domain.team.usecase.GetTeamsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val user: UserDTO? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val userTeamNames: List<String> = emptyList()
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val apiService: ApiService,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getTeamsUseCase: GetTeamsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState(isLoading = true))
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadUserTeams()
    }

    private fun loadUserTeams() {
        viewModelScope.launch {
            val firebaseUid = getCurrentUserUseCase()?.uid ?: return@launch

            try {
                // Lekérjük az összes csapatot
                val allTeams = getTeamsUseCase()

                // Kiszűrjük azokat, ahol a members listában van olyan, akinek a uid-ja a miénk
                val myTeams = allTeams.filter { team ->
                    team.members.any { member -> member.uid == firebaseUid }
                }.map { it.name } // Csak a nevekre van szükségünk

                _uiState.update { it.copy(userTeamNames = myTeams) }
            } catch (e: Exception) {
                // Ha nem sikerül betölteni a csapatokat, azt halkan kezeljük,
                // hogy a profil többi része működjön
                e.printStackTrace()
            }
        }
    }

    fun initUser(userDTO: UserDTO?) {
        if (userDTO != null && _uiState.value.user?.id != userDTO.id) {
            _uiState.update { it.copy(user = userDTO, isLoading = false) }
        }
    }

    fun updateUser(firstName: String, lastName: String) {
        val currentUser = _uiState.value.user ?: return

        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            try {
                val optimisticUser = currentUser.copy(firstName = firstName, lastName = lastName)
                _uiState.update { it.copy(user = optimisticUser) }

                val savedUser = apiService.updateUser(optimisticUser)
                _uiState.update { it.copy(isLoading = false, user = savedUser) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Ismeretlen hiba történt",
                        user = currentUser
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}