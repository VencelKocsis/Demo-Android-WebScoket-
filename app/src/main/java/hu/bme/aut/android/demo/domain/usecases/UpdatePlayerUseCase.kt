package hu.bme.aut.android.demo.domain.usecases

import hu.bme.aut.android.demo.domain.model.NewPlayerDTO
import hu.bme.aut.android.demo.domain.repository.PlayerRepository
import javax.inject.Inject

class UpdatePlayerUseCase @Inject constructor(
    private val repository: PlayerRepository
) {
    /**
     * Frissíti egy meglévő játékos adatait a rendszerben.
     * @param id A frissítendő játékos azonosítója.
     * @param name Az új név.
     * @param age Az új kor.
     */
    suspend operator fun invoke(id: Int, newPlayer: NewPlayerDTO) {
        repository.updatePlayer(id, newPlayer)
    }
}