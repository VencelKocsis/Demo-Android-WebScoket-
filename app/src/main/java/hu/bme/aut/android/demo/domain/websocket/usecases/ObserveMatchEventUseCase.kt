package hu.bme.aut.android.demo.domain.websocket.usecases

import hu.bme.aut.android.demo.domain.websocket.repository.MatchWsRepository
import hu.bme.aut.android.demo.domain.websocket.model.MatchWsEvent
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * UseCase a WebSocket események (élő eredmények, aláírások) feliratkozására.
 * * A UI (ViewModel) ezt hívja meg, hogy reagálni tudjon a valós idejű változásokra.
 */
class ObserveMatchEventUseCase @Inject constructor(
    private val repository: MatchWsRepository
) {
    operator fun invoke(): Flow<MatchWsEvent> {
        return repository.getMatchWsEventsFlow()
    }
}