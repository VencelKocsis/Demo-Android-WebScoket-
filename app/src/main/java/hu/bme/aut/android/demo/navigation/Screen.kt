package hu.bme.aut.android.demo.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person

sealed class Screen(val route: String, val title: String? = null, val icon: ImageVector? = null) {
    data object Login : Screen("login_screen")
    data object Players : Screen("demo_screen")

    data object Main : Screen("main_screen")

    data object Tournament : Screen("tournament", "Bajnokság", Icons.Default.EmojiEvents)
    data object Team : Screen("team", "Csapat", Icons.Default.Group)
    data object History : Screen("history", "Előzmények", Icons.Default.History)
    data object Profile : Screen("profile", "Profil", Icons.Default.Person)

    data object RoundDetails : Screen("round_details")
    data object RacketEditor : Screen("racket_editor")
}