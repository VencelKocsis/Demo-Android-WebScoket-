package hu.bme.aut.android.demo.feature.list_players

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.bme.aut.android.demo.domain.model.NewPlayerDTO
import hu.bme.aut.android.demo.domain.model.PlayerDTO
import hu.bme.aut.android.demo.domain.model.WsEvent
import hu.bme.aut.android.demo.domain.usecases.AddPlayerUseCase
import hu.bme.aut.android.demo.domain.usecases.DeletePlayerUseCase
import hu.bme.aut.android.demo.domain.usecases.GetInitialPlayersUseCase
import hu.bme.aut.android.demo.domain.usecases.ObservePlayersEventsUseCase
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayersViewModel @Inject constructor(
    // A Hilt injektálja a Domain réteg Use Case-eit
    private val getInitialPlayersUseCase: GetInitialPlayersUseCase,
    private val observePlayersEventsUseCase: ObservePlayersEventsUseCase,
    private val addPlayerUseCase: AddPlayerUseCase,
    private val deletePlayerUseCase: DeletePlayerUseCase
) : ViewModel() {

    // A lista tartalmát a Use Case-től kapott Flow-ból töltjük fel
    val players = mutableStateOf<List<PlayerDTO>>(emptyList())

    val loading = mutableStateOf(false)
    val error = mutableStateOf<String?>(null)

    init {
        viewModelScope.launch {
            loading.value = true
            error.value = null

            try {
                // 1. HTTP hívás a kezdeti listáért
                val initialList = getInitialPlayersUseCase()
                players.value = initialList
            } catch (e: Exception) {
                // ... hiba kezelése ...
            } finally {
                loading.value = false
            }

            // 2. WS események figyelése a lista frissítéséhez
            observePlayersEventsUseCase()
                .collect { event ->
                    when (event) {
                        is WsEvent.PlayerAdded -> {
                            players.value = players.value + event.player
                        }
                        is WsEvent.PlayerDeleted -> {
                            players.value = players.value.filter { it.id != event.id }
                        }
                    }
                }
        }
    }

    override fun onCleared() {
        super.onCleared()
    }

    fun addPlayer(name: String, age: Int?) {
        viewModelScope.launch {
            try {
                // A Use Case-t használjuk a hálózati hívás helyett
                addPlayerUseCase(NewPlayerDTO(name, age))
                // Megjegyzés: Nincs szükség a lista frissítésére, mert a WS frissíti
            } catch (e: Exception) {
                error.value = "Hiba hozzáadáskor: ${e.message}"
            }
        }
    }

    fun deletePlayer(id: Int) {
        viewModelScope.launch {
            try {
                // A Use Case-t használjuk a hálózati hívás helyett
                deletePlayerUseCase(id)
                // Megjegyzés: Nincs szükség a lista frissítésére, mert a WS frissíti
            } catch (e: Exception) {
                error.value = "Hiba törléskor: ${e.message}"
            }
        }
    }
}