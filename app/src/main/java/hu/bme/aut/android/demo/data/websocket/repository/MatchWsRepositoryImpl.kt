package hu.bme.aut.android.demo.data.websocket.repository

import hu.bme.aut.android.demo.data.websocket.MatchWebSocketClient
import hu.bme.aut.android.demo.domain.websocket.model.MatchWsEvent
import hu.bme.aut.android.demo.domain.websocket.repository.MatchWsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * A [MatchWsRepository] konkrét megvalósítása a Data rétegben.
 * * Becsomagolja a [MatchWebSocketClient] hívásait, és átadja az eseményeket a Domain felé.
 */
class MatchWsRepositoryImpl @Inject constructor(
    private val matchWsClient: MatchWebSocketClient
) : MatchWsRepository {

    override fun getMatchWsEventsFlow(): Flow<MatchWsEvent> {
        matchWsClient.connect() // Biztosítjuk, hogy éljen a kapcsolat a lekéréskor
        return matchWsClient.events
    }
}