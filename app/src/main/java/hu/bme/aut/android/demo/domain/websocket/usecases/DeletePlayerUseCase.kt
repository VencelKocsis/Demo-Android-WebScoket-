package hu.bme.aut.android.demo.domain.websocket.usecases

import hu.bme.aut.android.demo.domain.websocket.repository.PlayerRepository
import javax.inject.Inject

class DeletePlayerUseCase @Inject constructor(
    private val repository: PlayerRepository
) {
    /**
     * Töröl egy játékost a megadott ID alapján.
     * @param id A törlendő játékos azonosítója.
     */
    suspend operator fun invoke(id: Int) {
        // A hívás továbbítva a Repository-nak.
        repository.deletePlayer(id)
        // A törlés után a Repository indítja a WS broadcastot
    }
}