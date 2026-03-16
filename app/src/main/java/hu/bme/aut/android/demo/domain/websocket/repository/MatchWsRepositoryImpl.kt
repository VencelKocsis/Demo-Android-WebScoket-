package hu.bme.aut.android.demo.domain.websocket.repository

import hu.bme.aut.android.demo.data.websocket.MatchWebSocketClient
import hu.bme.aut.android.demo.data.websocket.repository.MatchWsRepository
import hu.bme.aut.android.demo.domain.websocket.model.MatchWsEvent
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MatchWsRepositoryImpl @Inject constructor(
    private val matchWsClient: MatchWebSocketClient
) : MatchWsRepository {
    override fun getMatchWsEventsFlow(): Flow<MatchWsEvent> {
        matchWsClient.connect()
        return matchWsClient.events
    }
}