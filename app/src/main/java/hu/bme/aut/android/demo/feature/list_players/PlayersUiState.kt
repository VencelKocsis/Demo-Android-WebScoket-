package hu.bme.aut.android.demo.feature.list_players

import hu.bme.aut.android.demo.domain.websocket.model.PlayerDTO

data class PlayersUiState(
    val players: List<PlayerDTO> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null
)