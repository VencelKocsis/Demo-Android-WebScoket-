package hu.bme.aut.android.demo.feature.profile

import hu.bme.aut.android.demo.domain.auth.model.User

/**
 * A Profil (saját és más játékosok) képernyő egyetlen igazságforrása.
 * Tartalmazza a játékos személyes adatait, felszerelését és a számított statisztikákat.
 */
data class ProfileUiState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val userTeamNames: List<String> = emptyList(),

    // Szezon szűrő adatok
    val availableSeasons: List<Pair<Int, String>> = emptyList(),
    val selectedSeasonId: Int? = null,

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
    val flawlessDays: Int = 0, // 4/4 győzelmek egy napon belül (Flawless Victory)

    // 3. Egymás elleni (H2H)
    val favoriteOpponent: Pair<String, Int>? = null, // Név és Győzelmek száma
    val nemesis: Pair<String, Int>? = null // Név és Vereségek száma
)

/**
 * MVI események a Profil képernyő vezérléséhez.
 */
sealed class ProfileEvent {
    data class InitOwnUser(val user: User?) : ProfileEvent()
    data class LoadPublicProfile(val uid: String) : ProfileEvent()
    data class UpdateProfileData(val firstName: String, val lastName: String) : ProfileEvent()
    data class SelectSeason(val seasonId: Int?) : ProfileEvent()
    object RefreshProfile : ProfileEvent()
    object ClearError : ProfileEvent()
}