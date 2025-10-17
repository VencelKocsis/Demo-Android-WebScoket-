package hu.bme.aut.android.demo.navigation

sealed class Screen(val route: String) {
    data object Login : Screen("login_screen")
    data object Players : Screen("demo_screen")
}