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

    // Alap statisztikák
    val matchesPlayed: Int = 0,
    val matchesWon: Int = 0,
    val winRate: Int = 0,

    // 1. Forma és Élő-pont (Rating)
    val recentForm: List<Boolean> = emptyList(), // Utolsó 5 meccs (true = győzelem)
    val ratingHistory: List<Float> = emptyList(), // Grafikonhoz

    // 2. Szett Mutatók (Best of 5 alapján)
    val sweeps: Int = 0, // 3-0-ás győzelmek
    val decidingSetWins: Int = 0, // 3-2-es győzelmek (Clutch)

    // 3. Egymás elleni (H2H)
    val favoriteOpponent: Pair<String, Int>? = null, // Név és Győzelmek száma
    val nemesis: Pair<String, Int>? = null // Név és Vereségek száma
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
                val allTeams = getTeamsUseCase()
                val myTeams = allTeams.filter { team ->
                    team.members.any { member -> member.uid == firebaseUid }
                }.map { it.name }
                _uiState.update { it.copy(userTeamNames = myTeams) }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun initUser(userDTO: UserDTO?) {
        if (userDTO != null && _uiState.value.user?.id != userDTO.id) {
            _uiState.update { it.copy(user = userDTO, isLoading = false) }
            loadUserStats(userDTO)
        }
    }

    private fun loadUserStats(user: UserDTO) {
        viewModelScope.launch {
            try {
                val allMatches = getTeamMatchesUseCase()
                val fullName = "${user.lastName} ${user.firstName}"

                // Összegyűjtjük az egyéni meccseit dátummal együtt, majd időrendbe (növekvő) rakjuk
                val userMatches = allMatches.flatMap { teamMatch ->
                    teamMatch.individualMatches
                        .filter { it.status == "finished" && (it.homePlayerName == fullName || it.guestPlayerName == fullName) }
                        .map { it to (teamMatch.matchDate ?: "") }
                }.sortedBy { it.second } // Növekvő sorrend a grafikon miatt!

                var played = 0
                var won = 0
                var sweepsCount = 0
                var decidingWinsCount = 0

                val oppStats = mutableMapOf<String, Pair<Int, Int>>() // Név -> (Győzelem, Vereség)
                val formList = mutableListOf<Boolean>()

                var currentRating = 1000f // Kezdő Élő-pont
                val historyList = mutableListOf<Float>(currentRating)

                for ((im, _) in userMatches) {
                    val isHome = im.homePlayerName == fullName
                    val oppName = if (isHome) im.guestPlayerName else im.homePlayerName
                    val myScore = if (isHome) im.homeScore else im.guestScore
                    val oppScore = if (isHome) im.guestScore else im.homeScore
                    val isWin = myScore > oppScore

                    played++
                    if (isWin) won++

                    // Szett mutatók
                    if (isWin && oppScore == 0) sweepsCount++
                    if (isWin && oppScore == 2) decidingWinsCount++

                    // H2H Statisztika
                    val currentStats = oppStats.getOrDefault(oppName, Pair(0, 0))
                    oppStats[oppName] = if (isWin) Pair(currentStats.first + 1, currentStats.second) else Pair(currentStats.first, currentStats.second + 1)

                    // Forma
                    formList.add(isWin)

                    // Rating változás (+10 győzelemért, -10 vereségért egyszerűsítve)
                    currentRating += if (isWin) 10f else -10f
                    historyList.add(currentRating)
                }

                val rate = if (played > 0) (won * 100) / played else 0
                val recentForm = formList.takeLast(5) // Csak az utolsó 5

                // Legtöbbször legyőzött és akitől a legtöbbször kikapott
                val favOpponent = oppStats.filter { it.value.first > 0 }.maxByOrNull { it.value.first }
                val nemesisOpponent = oppStats.filter { it.value.second > 0 }.maxByOrNull { it.value.second }

                _uiState.update { it.copy(
                    matchesPlayed = played,
                    matchesWon = won,
                    winRate = rate,
                    recentForm = recentForm,
                    ratingHistory = historyList,
                    sweeps = sweepsCount,
                    decidingSetWins = decidingWinsCount,
                    favoriteOpponent = favOpponent?.let { Pair(it.key, it.value.first) },
                    nemesis = nemesisOpponent?.let { Pair(it.key, it.value.second) }
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