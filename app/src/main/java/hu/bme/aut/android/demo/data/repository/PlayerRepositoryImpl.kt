package hu.bme.aut.android.demo.data.repository

import hu.bme.aut.android.demo.data.network.ApiService
import hu.bme.aut.android.demo.data.network.PlayersWebSocketClient
import hu.bme.aut.android.demo.domain.model.NewPlayerDTO
import hu.bme.aut.android.demo.domain.model.PlayerDTO
import hu.bme.aut.android.demo.domain.model.WsEvent
import hu.bme.aut.android.demo.domain.repository.PlayerRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

// Ez az osztály implementálja az interfészt és injektálja a hálózati szolgáltatásokat
class PlayerRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val wsClient: PlayersWebSocketClient
) : PlayerRepository {

    // ----------------------------------------------------
    // FLOW KEZELÉS: HTTP kezdeti betöltés + WS frissítés
    // ----------------------------------------------------

    // ÚJ: Ezt a metódust fogja használni a Use Case a WS események figyelésére
    override fun getWsEventsFlow(): Flow<WsEvent> {
        wsClient.connect() // Elindítjuk a WS kapcsolatot, amikor elkezdik figyelni a Flow-t
        return wsClient.events
    }

    // Módosított: A kezdeti listát csak simán HTTP-n kérjük le.
    override suspend fun getInitialPlayers(): List<PlayerDTO> {
        return apiService.getPlayers()
    }

    // ----------------------------------------------------
    // CRUD MŰVELETEK (HTTP)
    // ----------------------------------------------------

    override suspend fun addPlayer(player: NewPlayerDTO): PlayerDTO {
        // Hívjuk a REST API-t az ApiService-en keresztül
        return apiService.addPlayer(player)
        // A Ktor backend küldi a WS eseményt, nem kell itt foglalkozni vele
    }

    override suspend fun deletePlayer(id: Int) {
        // Hívjuk a REST API-t az ApiService-en keresztül
        apiService.deletePlayer(id)
        // A Ktor backend küldi a WS eseményt
    }

        // Először lekérjük a teljes listát HTTP-n keresztül
        // Utána pedig figyeljük a WS eseményeket, és a teljes listát frissítjük a háttérben.

        // Mivel a WS események nem a teljes listát küldik, hanem csak a delta-t,
        // a ViewModelben az előzőleg javasolt megoldás (ahol a ViewModel kezeli a listát a WS alapján)
        // a legegyszerűbb. A Repository csak továbbítja az eseményeket, vagy
        // VAGY az egyszerűség kedvéért a Repository csak a WS Flow-ját továbbítja a Viewmodelnek.

        // Azonban a cél az MVI. Tegyük fel, hogy az apiService.getPlayers() adja a kezdeti listát.

        // Mivel a ViewModel már kezeli a WS események alapján a lista frissítését,
        // itt csak elindítjuk a kapcsolatot és visszatérünk a WS eseményekkel.

        // DE: A Use Case-ben láttuk, hogy Flow<List<PlayerDTO>>-t várunk.
        // Ezt a ViewModel logikájával valósítottuk meg.

        // Javaslat: Visszatérünk a ViewModel logikájához, de itt kezdeményezzük az első HTTP hívást.
        // Mivel a ViewModel most már csak a Flow-t kéri, ennek a függvénynek kellene
        // a kezdeti HTTP lekérést elindítani, majd rátérni a WS események gyűjtésére.

        // Ehelyett a tisztaság kedvéért:
        // Csatlakozunk a WS-hez.
        // A ViewModel hívja meg a kezdeti HTTP lekérést is (ha a Use Case 2 metódusra oszlik).

        // Ha a Use Case-ed Flow<List<PlayerDTO>>-t vár, akkor a Repository-nak ezt kell megoldania:
}