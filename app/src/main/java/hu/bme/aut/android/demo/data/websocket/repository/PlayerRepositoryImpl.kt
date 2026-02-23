package hu.bme.aut.android.demo.data.websocket.repository

import hu.bme.aut.android.demo.data.network.api.RetrofitApi
import hu.bme.aut.android.demo.data.websocket.PlayersWebSocketClient
import hu.bme.aut.android.demo.data.fcm.model.FcmToken
import hu.bme.aut.android.demo.domain.websocket.model.NewPlayerDTO
import hu.bme.aut.android.demo.domain.websocket.model.PlayerDTO
import hu.bme.aut.android.demo.domain.websocket.model.WsEvent
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PlayerRepositoryImpl @Inject constructor(
    private val retrofitApi: RetrofitApi,
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
        return retrofitApi.getPlayers()
    }

    // ----------------------------------------------------
    // FCM MŰVELETEK (HTTP)
    // ----------------------------------------------------

    override suspend fun registerFcmToken(email: String, token: String) {
        retrofitApi.registerFcmToken(FcmToken(email, token))
    }

    // ----------------------------------------------------
    // CRUD MŰVELETEK (HTTP)
    // ----------------------------------------------------
    override suspend fun addPlayer(player: NewPlayerDTO): PlayerDTO {
        // Hívjuk a REST API-t az ApiService-en keresztül
        return retrofitApi.addPlayer(player)
        // A Ktor backend küldi a WS eseményt, nem kell itt foglalkozni vele
    }

    override suspend fun deletePlayer(id: Int) {
        // Hívjuk a REST API-t az ApiService-en keresztül
        retrofitApi.deletePlayer(id)
        // A Ktor backend küldi a WS eseményt
    }

    override suspend fun updatePlayer(id: Int, player: NewPlayerDTO) {
        // Hívjuk a REST API-t az ApiService-en keresztül
        retrofitApi.updatePlayer(id, player)
        // A Ktor backend küldi a WS eseményt
    }
}