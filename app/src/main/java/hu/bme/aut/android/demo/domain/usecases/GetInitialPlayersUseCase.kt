package hu.bme.aut.android.demo.domain.usecases

import hu.bme.aut.android.demo.domain.model.PlayerDTO
import hu.bme.aut.android.demo.domain.repository.PlayerRepository
import javax.inject.Inject

class GetInitialPlayersUseCase @Inject constructor(
    private val repository: PlayerRepository
) {
    // A Use Case a beolvasott lista Flow-ját biztosítja
    // Ez a Flow figyeli a Repository-n keresztül a WebSocket eseményeket is
    suspend operator fun invoke(): List<PlayerDTO> {
        return repository.getInitialPlayers()
    }
}