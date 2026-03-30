package hu.bme.aut.android.demo.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.bme.aut.android.demo.data.auth.model.UserDTO
import hu.bme.aut.android.demo.data.network.api.ApiService
import hu.bme.aut.android.demo.domain.auth.usecases.GetCurrentUserUseCase
import hu.bme.aut.android.demo.domain.team.usecase.GetTeamsUseCase
import hu.bme.aut.android.demo.domain.teammatch.usecase.GetTeamMatchesUseCase
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
    val userTeamNames: List<String> = emptyList(),
    val matchesPlayed: Int = 0,
    val matchesWon: Int = 0,
    val winRate: Int = 0
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val apiService: ApiService,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getTeamsUseCase: GetTeamsUseCase,
    private val getTeamMatchesUseCase: GetTeamMatchesUseCase
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
                e.printStackTrace()
            }
        }
    }

    fun initUser(userDTO: UserDTO?) {
        if (userDTO != null && _uiState.value.user?.id != userDTO.id) {
            _uiState.update { it.copy(user = userDTO, isLoading = false) }
            // Ha megvan a felhasználó (és a neve), betöltjük a statisztikát ---
            loadUserStats(userDTO)
        }
    }

    private fun loadUserStats(user: UserDTO) {
        viewModelScope.launch {
            try {
                val allMatches = getTeamMatchesUseCase()
                // A magyar névkonvenció alapján összerakjuk a teljes nevet
                val fullName = "${user.lastName} ${user.firstName}"

                var played = 0
                var won = 0

                allMatches.forEach { teamMatch ->
                    // Csak a már befejezett egyéni meccseket nézzük
                    teamMatch.individualMatches.forEach { im ->
                        if (im.status == "finished") {
                            val isHome = im.homePlayerName == fullName
                            val isGuest = im.guestPlayerName == fullName

                            // Ha játszott ezen a meccsen
                            if (isHome || isGuest) {
                                played++
                                val myScore = if (isHome) im.homeScore else im.guestScore
                                val oppScore = if (isHome) im.guestScore else im.homeScore

                                if (myScore > oppScore) {
                                    won++
                                }
                            }
                        }
                    }
                }

                val rate = if (played > 0) (won * 100) / played else 0

                _uiState.update { it.copy(
                    matchesPlayed = played,
                    matchesWon = won,
                    winRate = rate
                )}
            } catch (e: Exception) {
                e.printStackTrace()
            }
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