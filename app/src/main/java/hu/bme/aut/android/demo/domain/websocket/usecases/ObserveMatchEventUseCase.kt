package hu.bme.aut.android.demo.domain.websocket.usecases

import hu.bme.aut.android.demo.data.websocket.repository.MatchWsRepository
import hu.bme.aut.android.demo.domain.websocket.model.MatchWsEvent
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveMatchEventUseCase @Inject constructor(
    private val repository: MatchWsRepository
) {
    operator fun invoke(): Flow<MatchWsEvent> {
        return repository.getMatchWsEventsFlow()
    }
}