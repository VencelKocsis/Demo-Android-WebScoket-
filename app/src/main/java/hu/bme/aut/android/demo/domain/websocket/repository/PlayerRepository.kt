package hu.bme.aut.android.demo.domain.websocket.repository

import hu.bme.aut.android.demo.domain.websocket.model.NewPlayerDTO
import hu.bme.aut.android.demo.domain.websocket.model.PlayerDTO
import hu.bme.aut.android.demo.domain.websocket.model.WsEvent
import kotlinx.coroutines.flow.Flow

interface PlayerRepository {

    suspend fun getInitialPlayers(): List<PlayerDTO>

    fun getWsEventsFlow(): Flow<WsEvent>

    // HTTP POST
    suspend fun addPlayer(player: NewPlayerDTO): PlayerDTO

    // HTTP DELETE
    suspend fun deletePlayer(id: Int)

    // HTTP PUT
    suspend fun updatePlayer(id: Int, player: NewPlayerDTO)

    suspend fun registerFcmToken(userId: String, token: String)
}