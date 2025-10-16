package hu.bme.aut.android.demo.domain.websocket.usecases

import hu.bme.aut.android.demo.domain.websocket.model.NewPlayerDTO
import hu.bme.aut.android.demo.domain.websocket.model.PlayerDTO
import hu.bme.aut.android.demo.domain.websocket.repository.PlayerRepository
import javax.inject.Inject

class AddPlayerUseCase @Inject constructor(
    private val repository: PlayerRepository
) {
    /**
     * Hozzáad egy új játékost a rendszerhez.
     * @param newPlayer Az új játékos adatai (név és kor).
     * @return A szerver által generált ID-val ellátott PlayerDTO-t adja vissza.
     */
    suspend operator fun invoke(newPlayer: NewPlayerDTO): PlayerDTO {
        return repository.addPlayer(newPlayer)
    }
}
