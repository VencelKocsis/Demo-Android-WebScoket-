package hu.bme.aut.android.demo.data.websocket.repository

import hu.bme.aut.android.demo.domain.websocket.model.MatchWsEvent
import kotlinx.coroutines.flow.Flow

/**
 * Dedikált Repository a Meccsekkel kapcsolatos WebSocket események kezelésére.
 */
interface MatchWsRepository {
    fun getMatchWsEventsFlow(): Flow<MatchWsEvent>
}