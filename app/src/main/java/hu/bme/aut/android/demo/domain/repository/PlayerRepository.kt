package hu.bme.aut.android.demo.domain.repository

import hu.bme.aut.android.demo.domain.model.NewPlayerDTO
import hu.bme.aut.android.demo.domain.model.PlayerDTO
import hu.bme.aut.android.demo.domain.model.WsEvent
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
}