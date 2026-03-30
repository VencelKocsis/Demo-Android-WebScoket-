package hu.bme.aut.android.demo.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import hu.bme.aut.android.demo.R

sealed class Screen(
    val route: String,
    @StringRes val titleResId: Int? = null,
    val icon: ImageVector? = null
) {
    data object Login : Screen("login_screen")
    data object Players : Screen("demo_screen")

    data object Main : Screen("main_screen")

    data object Tournament : Screen("tournament", R.string.championship, Icons.Default.EmojiEvents)
    data object Team : Screen("team", R.string.club, Icons.Default.Group)
    data object History : Screen("history", R.string.history, Icons.Default.History)
    data object Profile : Screen("profile", R.string.profile, Icons.Default.Person)

    data object RacketEditor : Screen("racket_editor")
    data object TeamEditor : Screen("team_editor") {
        fun createRoute(teamId: Int): String = "$route/$teamId"
    }
    data object MatchDetails : Screen("match_details") {
        fun createRoute(matchId: Int): String = "$route/$matchId"
    }

    data object LiveMatch : Screen("live_match") {
        fun createRoute(matchId: Int): String = "$route/$matchId"
    }

    data object MatchScorer : Screen("match_scorer") {
        fun createRoute(matchId: Int, individualMatchId: Int): String = "$route/$matchId/$individualMatchId"
    }
}