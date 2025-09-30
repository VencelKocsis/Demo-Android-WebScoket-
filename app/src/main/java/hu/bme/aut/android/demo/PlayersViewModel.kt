package hu.bme.aut.android.demo

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hu.bme.aut.android.demo.models.NewPlayerDTO
import hu.bme.aut.android.demo.models.PlayerDTO
import hu.bme.aut.android.demo.models.WsEvent
import kotlinx.coroutines.launch
import kotlin.collections.filter

class PlayersViewModel : ViewModel() {
    val players = androidx.compose.runtime.mutableStateOf<List<PlayerDTO>>(emptyList())
    val loading = androidx.compose.runtime.mutableStateOf(false)
    val error = androidx.compose.runtime.mutableStateOf<String?>(null)

    private val wsClient = PlayersWebSocketClient()

    init {
        viewModelScope.launch {
            wsClient.events.collect { event ->
                Log.w("VM", "Received WsEvent: ${event.javaClass.simpleName}. Updating list...")

                when (event) {
                    is WsEvent.PlayerAdded -> {
                        players.value = players.value + event.player
                        Log.w("VM", "Player added, new list size: ${players.value.size}")
                    }
                    is WsEvent.PlayerDeleted -> {
                        players.value = players.value.filter { it.id != event.id }
                        Log.w("VM", "Player deleted, new list size: ${players.value.size}")
                    }
                }
            }
        }
        wsClient.connect()
    }

    override fun onCleared() {
        super.onCleared()
        wsClient.close()
    }

    fun loadPlayers() {
        viewModelScope.launch {
            loading.value = true
            error.value = null
            try {
                val fetchedPlayers = NetworkClient.api.getPlayers()
                players.value = fetchedPlayers
            } catch (e: Exception) {
                e.printStackTrace()
                error.value = "Hiba: ${e.message}"
            } finally {
                loading.value = false
            }
        }
    }

    fun addPlayer(name: String, age: Int?) {
        viewModelScope.launch {
            try {
                NetworkClient.api.addPlayer(NewPlayerDTO(name, age))
            } catch (e: Exception) {
                e.printStackTrace()
                error.value = "Hiba hozzáadáskor: ${e.message}"
            }
        }
    }

    fun deletePlayer(id: Int) {
        viewModelScope.launch {
            try {
                NetworkClient.api.deletePlayer(id)
            } catch (e: Exception) {
                e.printStackTrace()
                error.value = "Hiba törléskor: ${e.message}"
            }
        }
    }
}
