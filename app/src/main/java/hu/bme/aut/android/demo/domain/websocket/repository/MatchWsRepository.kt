package hu.bme.aut.android.demo.domain.websocket.repository

import hu.bme.aut.android.demo.domain.websocket.model.MatchWsEvent
import kotlinx.coroutines.flow.Flow

/**
 * A meccsekkel kapcsolatos WebSocket események elvont szerződése a Domain rétegben.
 * * Csak azt definiálja, hogy folyamatos eseményeket (Flow) tudunk szolgáltatni,
 * de nem tudja, hogy ez a háttérben OkHttp WebSocketből jön.
 */
interface MatchWsRepository {
    fun getMatchWsEventsFlow(): Flow<MatchWsEvent>
}