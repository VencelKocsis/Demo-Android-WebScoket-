package hu.bme.aut.android.demo.feature.team

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.bme.aut.android.demo.domain.team.usecase.GetTeamsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TeamUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val teamName: String = "",
    val members: List<TeamMember> = emptyList()
)

@HiltViewModel
class TeamViewModel @Inject constructor(
    private val getTeamsUseCase: GetTeamsUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(TeamUiState())
    val uiState: StateFlow<TeamUiState> = _uiState.asStateFlow()

    init {
        loadTeams()
    }

    private fun loadTeams() {
        viewModelScope.launch {
            try {
                Log.d("TeamViewModel", "⏳ Csapatok lekérdezése indult...")
                val teams = getTeamsUseCase()
                Log.d("TeamViewModel", "✅ Kaptunk ${teams.size} csapatot a backendtől.")

                // Egyenlőre kiválasztjuk az első csapatot (pl. BEAC I.) a megjelenítéshez
                val firstTeam = teams.firstOrNull()

                if (firstTeam != null) {
                    Log.d("TeamViewModel", "Kiválasztott csapat: ${firstTeam.teamName}, Tagok: ${firstTeam.members.size}")

                    // Átalakítjuk a DTO-t a UI által várt TeamMember listára
                    val mappedMembers = firstTeam.members.map { memberDTO ->
                        TeamMember(
                            name = memberDTO.name,
                            isCaptain = memberDTO.isCaptain,
                            stats = PlayerStats(wins = 0, losses = 0) // Egyenlőre 0 a statisztika
                        )
                    }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = null,
                            teamName = firstTeam.teamName,
                            members = mappedMembers
                        )
                    }

                } else {
                    Log.w("TeamViewModel", "Üres a csapatok listája!")
                    _uiState.update { it.copy(isLoading = false, error = "Nincsenek csapatok") }
                }

            } catch (e: Exception) {
                Log.e("TeamViewModel", "❌ Hiba a csapatok lekérdezésekor: ${e.message}", e)
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}