package hu.bme.aut.android.demo.data.websocket.repository

import hu.bme.aut.android.demo.data.network.api.ApiService
import hu.bme.aut.android.demo.data.websocket.PlayersWebSocketClient
import hu.bme.aut.android.demo.data.fcm.model.FcmToken
import hu.bme.aut.android.demo.domain.websocket.model.NewPlayerDTO
import hu.bme.aut.android.demo.domain.websocket.model.PlayerDTO
import hu.bme.aut.android.demo.domain.websocket.model.WsEvent
import hu.bme.aut.android.demo.domain.websocket.repository.PlayerRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PlayerRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val wsClient: PlayersWebSocketClient
) : PlayerRepository {

    // ----------------------------------------------------
    // FLOW KEZELÉS: HTTP kezdeti betöltés + WS frissítés
    // ----------------------------------------------------

    override fun getWsEventsFlow(): Flow<WsEvent> {
        wsClient.connect()
        return wsClient.events
    }

    override suspend fun getInitialPlayers(): List<PlayerDTO> {
        return apiService.getPlayers()
    }

    // ----------------------------------------------------
    // FCM MŰVELETEK (HTTP)
    // ----------------------------------------------------

    override suspend fun registerFcmToken(userId: String, token: String) {
        apiService.registerFcmToken(FcmToken(userId, token))
    }

    // ----------------------------------------------------
    // CRUD MŰVELETEK (HTTP)
    // ----------------------------------------------------
    override suspend fun addPlayer(player: NewPlayerDTO): PlayerDTO {
        // Hívjuk a REST API-t az ApiService-en keresztül
        return apiService.addPlayer(player)
        // A Ktor backend küldi a WS eseményt, nem kell itt foglalkozni vele
    }

    override suspend fun deletePlayer(id: Int) {
        // Hívjuk a REST API-t az ApiService-en keresztül
        apiService.deletePlayer(id)
        // A Ktor backend küldi a WS eseményt
    }

    override suspend fun updatePlayer(id: Int, player: NewPlayerDTO) {
        // Hívjuk a REST API-t az ApiService-en keresztül
        apiService.updatePlayer(id, player)
        // A Ktor backend küldi a WS eseményt
    }
}