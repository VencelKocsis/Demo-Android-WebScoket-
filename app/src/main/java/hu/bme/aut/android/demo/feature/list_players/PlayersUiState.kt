package hu.bme.aut.android.demo.feature.list_players

data class PlayersUiState(
    val players: List<PlayerDTO> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null
)