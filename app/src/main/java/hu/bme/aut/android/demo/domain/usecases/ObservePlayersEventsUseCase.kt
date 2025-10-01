package hu.bme.aut.android.demo.domain.usecases

import hu.bme.aut.android.demo.domain.model.WsEvent
import hu.bme.aut.android.demo.domain.repository.PlayerRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

// Ez egy Flow-t visszaadó Use Case, ami a WS eseményeket figyeli
class ObservePlayersEventsUseCase @Inject constructor(
    private val repository: PlayerRepository
) {
    operator fun invoke(): Flow<WsEvent> {
        // Meghívja a repository Flow-t visszaadó függvényét
        return repository.getWsEventsFlow()
    }
}