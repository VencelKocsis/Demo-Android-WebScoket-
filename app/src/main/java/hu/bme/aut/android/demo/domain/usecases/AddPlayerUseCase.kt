package hu.bme.aut.android.demo.domain.usecases

import hu.bme.aut.android.demo.domain.model.NewPlayerDTO
import hu.bme.aut.android.demo.domain.model.PlayerDTO
import hu.bme.aut.android.demo.domain.repository.PlayerRepository
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
        // A ViewModel csak a Use Case-t hívja meg, nem ismeri a hálózati hívásokat.
        return repository.addPlayer(newPlayer)
    }
}
