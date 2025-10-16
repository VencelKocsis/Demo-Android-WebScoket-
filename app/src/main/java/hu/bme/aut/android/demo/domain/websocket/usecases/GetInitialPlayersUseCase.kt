package hu.bme.aut.android.demo.domain.websocket.usecases

import hu.bme.aut.android.demo.domain.websocket.model.PlayerDTO
import hu.bme.aut.android.demo.domain.websocket.repository.PlayerRepository
import javax.inject.Inject

class GetInitialPlayersUseCase @Inject constructor(
    private val repository: PlayerRepository
) {
    suspend operator fun invoke(): List<PlayerDTO> {
        return repository.getInitialPlayers()
    }
}