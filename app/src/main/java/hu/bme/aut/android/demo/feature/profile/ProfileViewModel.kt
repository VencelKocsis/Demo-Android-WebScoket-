package hu.bme.aut.android.demo.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.bme.aut.android.demo.domain.auth.model.User
import hu.bme.aut.android.demo.domain.auth.usecases.GetCurrentUserUseCase
import hu.bme.aut.android.demo.domain.auth.usecases.GetUserByIdUseCase
import hu.bme.aut.android.demo.domain.auth.usecases.UpdateUserUseCase
import hu.bme.aut.android.demo.domain.team.usecase.GetTeamsUseCase
import hu.bme.aut.android.demo.domain.teammatch.model.TeamMatch
import hu.bme.aut.android.demo.domain.teammatch.usecase.GetTeamMatchesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * A Profil képernyő üzleti logikájáért és komplex statisztika számításaiért felelős ViewModel.
 * * Támogatja mind a saját profil megtekintését/szerkesztését, mind pedig a
 * publikus játékosok (ellenfelek) adatainak lekérdezését.
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getUserByIdUseCase: GetUserByIdUseCase,
    private val updateUserUseCase: UpdateUserUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getTeamsUseCase: GetTeamsUseCase,
    private val getTeamMatchesUseCase: GetTeamMatchesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState(isLoading = true))
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    // Eltároljuk a memóriában a már letöltött összes meccset a gyors kliensoldali szűréshez
    private var cachedFinishedMatches = listOf<TeamMatch>()

    init {
        loadUserTeams()
    }

    /**
     * MVI események (Intent) központi kezelője.
     */
    fun onEvent(event: ProfileEvent) {
        when (event) {
            is ProfileEvent.InitOwnUser -> initUser(event.user)
            is ProfileEvent.LoadPublicProfile -> loadPublicProfile(event.uid)
            is ProfileEvent.UpdateProfileData -> updateUser(event.firstName, event.lastName)
            is ProfileEvent.SelectSeason -> selectSeason(event.seasonId)
            is ProfileEvent.RefreshProfile -> refreshProfile()
            is ProfileEvent.ClearError -> clearError()
        }
    }

    private fun selectSeason(seasonId: Int?) {
        _uiState.update { it.copy(selectedSeasonId = seasonId) }
        _uiState.value.user?.let { user ->
            calculateStatsForSeason(user, seasonId)
        }
    }

    private fun loadUserStats(user: User) {
        viewModelScope.launch {
            try {
                val allMatches = getTeamMatchesUseCase()
                cachedFinishedMatches = allMatches.filter { it.status == "finished" }

                // Szezonok kinyerése a menühöz
                val seasonPairs = cachedFinishedMatches
                    .map { Pair(it.seasonId, it.seasonName ?: "Ismeretlen szezon") }
                    .distinctBy { it.first }
                    .sortedByDescending { it.first }

                val latestSeasonId = seasonPairs.firstOrNull()?.first

                _uiState.update { it.copy(
                    availableSeasons = seasonPairs,
                    selectedSeasonId = latestSeasonId // Alapból a legutóbbi szezont választjuk ki
                )}

                calculateStatsForSeason(user, latestSeasonId)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * A "varázslat" helye: Itt számolódnak ki a komplex statisztikák
     * (WinRate, Sweeps, Flawless Days, Nemesis, Forma) a kiválasztott szezon alapján.
     */
    private fun calculateStatsForSeason(user: User, seasonId: Int?) {
        val fullName = "${user.lastName} ${user.firstName}"

        val seasonMatches = if (seasonId == null) cachedFinishedMatches else cachedFinishedMatches.filter { it.seasonId == seasonId }

        val userMatches = seasonMatches.flatMap { teamMatch ->
            teamMatch.individualMatches
                .filter { it.status == "finished" && (it.homePlayerName == fullName || it.guestPlayerName == fullName) }
                .map { Triple(it, teamMatch.matchDate ?: "", teamMatch.id) }
        }.sortedBy { it.second }

        var played = 0; var won = 0; var sweepsCount = 0; var decidingWinsCount = 0
        val oppStats = mutableMapOf<String, Pair<Int, Int>>()
        val formList = mutableListOf<Boolean>()
        val winsPerTeamMatch = mutableMapOf<Int, Int>()

        var currentRating = 1000f // Minden szezon elején nullázódik 1000-ről!
        val historyList = mutableListOf<Float>(currentRating)

        for ((im, _, teamMatchId) in userMatches) {
            val isHome = im.homePlayerName == fullName
            val oppName = if (isHome) im.guestPlayerName else im.homePlayerName
            val myScore = if (isHome) im.homeScore else im.guestScore
            val oppScore = if (isHome) im.guestScore else im.homeScore
            val isWin = myScore > oppScore

            played++
            if (isWin) {
                won++
                winsPerTeamMatch[teamMatchId] = winsPerTeamMatch.getOrDefault(teamMatchId, 0) + 1
            }

            if (isWin && oppScore == 0) sweepsCount++
            if (isWin && oppScore == 2) decidingWinsCount++

            val currentStats = oppStats.getOrDefault(oppName, Pair(0, 0))
            oppStats[oppName] = if (isWin) Pair(currentStats.first + 1, currentStats.second) else Pair(currentStats.first, currentStats.second + 1)

            formList.add(isWin)
            currentRating += if (isWin) 10f else -10f
            historyList.add(currentRating)
        }

        val rate = if (played > 0) (won * 100) / played else 0
        val recentForm = formList.takeLast(5)
        val favOpponent = oppStats.filter { it.value.first > 0 }.maxByOrNull { it.value.first }
        val nemesisOpponent = oppStats.filter { it.value.second > 0 }.maxByOrNull { it.value.second }
        val flawlessDaysCount = winsPerTeamMatch.values.count { it >= 4 }

        _uiState.update { it.copy(
            matchesPlayed = played,
            matchesWon = won,
            winRate = rate,
            recentForm = recentForm,
            ratingHistory = historyList,
            sweeps = sweepsCount,
            decidingSetWins = decidingWinsCount,
            flawlessDays = flawlessDaysCount,
            favoriteOpponent = favOpponent?.let { Pair(it.key, it.value.first) },
            nemesis = nemesisOpponent?.let { Pair(it.key, it.value.second) }
        )}
    }

    private fun loadPublicProfile(uid: String) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val user = getUserByIdUseCase(uid)
                _uiState.update { it.copy(user = user, isLoading = false) }
                if (user != null) loadUserStats(user)
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Hiba a profil letöltésekor") }
            }
        }
    }

    private fun refreshProfile() {
        viewModelScope.launch {
            try {
                val firebaseUid = getCurrentUserUseCase()?.uid ?: return@launch
                val freshUser = getUserByIdUseCase(firebaseUid)

                if (freshUser != null) {
                    _uiState.update { it.copy(user = freshUser) }
                    loadUserStats(freshUser)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
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

    private fun initUser(user: User?) {
        if (user != null && _uiState.value.user?.id != user.id) {
            _uiState.update { it.copy(user = user, isLoading = false) }
            loadUserStats(user)
        }
    }

    private fun updateUser(firstName: String, lastName: String) {
        val currentUser = _uiState.value.user ?: return

        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            try {
                val optimisticUser = currentUser.copy(firstName = firstName, lastName = lastName)
                _uiState.update { it.copy(user = optimisticUser) }

                val savedUser = updateUserUseCase(optimisticUser)
                _uiState.update { it.copy(isLoading = false, user = savedUser) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = e.message ?: "Ismeretlen hiba történt", user = currentUser)
                }
            }
        }
    }

    private fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}