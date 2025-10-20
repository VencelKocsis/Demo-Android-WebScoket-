package hu.bme.aut.android.demo.feature.list_players

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.messaging.FirebaseMessaging // <-- ÚJ IMPORT: FCM eléréshez
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.bme.aut.android.demo.domain.websocket.model.NewPlayerDTO
import hu.bme.aut.android.demo.domain.websocket.model.PlayerDTO
import hu.bme.aut.android.demo.domain.websocket.model.WsEvent
import hu.bme.aut.android.demo.domain.websocket.usecases.AddPlayerUseCase
import hu.bme.aut.android.demo.domain.websocket.usecases.DeletePlayerUseCase
import hu.bme.aut.android.demo.domain.websocket.usecases.GetInitialPlayersUseCase
import hu.bme.aut.android.demo.domain.websocket.usecases.ObservePlayersEventsUseCase
import hu.bme.aut.android.demo.domain.fcm.usecases.RegisterFcmTokenUseCase
import hu.bme.aut.android.demo.domain.fcm.usecases.SendPushNotificationUseCase
import hu.bme.aut.android.demo.domain.websocket.usecases.UpdatePlayerUseCase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await // <-- ÚJ IMPORT: await() funkcióhoz
import javax.inject.Inject

private const val TAG = "PlayersViewModel"

@HiltViewModel
class PlayersViewModel @Inject constructor(
    // A Hilt injektálja a Domain réteg Use Case-eit
    private val getInitialPlayersUseCase: GetInitialPlayersUseCase,
    private val observePlayersEventsUseCase: ObservePlayersEventsUseCase,
    private val addPlayerUseCase: AddPlayerUseCase,
    private val deletePlayerUseCase: DeletePlayerUseCase,

    // Use Case a játékos szerkesztéséhez
    private val updatePlayerUseCase: UpdatePlayerUseCase,

    // Use Case az FCM token regisztrálásához
    private val registerFcmTokenUseCase: RegisterFcmTokenUseCase,
    private val sendPushNotificationUseCase: SendPushNotificationUseCase
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
                Log.d(TAG, "Initial players loaded: ${initialList.size}")
            } catch (e: Exception) {
                Log.e(TAG, "Error loading initial list", e)
                error.value = "Hiba a játékosok betöltésekor: ${e.message}"
            } finally {
                loading.value = false
            }

            // 2. WS események figyelése a lista frissítéséhez
            observePlayersEventsUseCase()
                .collect { event ->
                    when (event) {
                        is WsEvent.PlayerAdded -> {
                            Log.i(TAG, "WS: PlayerAdded - ${event.player.name}")
                            players.value = players.value + event.player
                        }
                        is WsEvent.PlayerDeleted -> {
                            Log.i(TAG, "WS: PlayerDeleted - ID: ${event.id}")
                            players.value = players.value.filter { it.id != event.id }
                        }

                        is WsEvent.PlayerUpdated -> {
                            Log.i(TAG, "WS: PlayerUpdated - ID: ${event.player.id}")
                            players.value = players.value.map {
                                if (it.id == event.player.id) event.player else it
                            }
                        }
                    }
                }
        }
    }

    // --- FCM Funkció ---
    fun registerFcmTokenForUser(userEmail: String) {
        viewModelScope.launch {
            try {
                val token = FirebaseMessaging.getInstance().token.await()

                Log.d(TAG, "Lekért FCM token: $token, Email: $userEmail")
                registerFcmTokenUseCase(userEmail, token)

                Log.i(TAG, "FCM token sikeresen elküldve a backendnek: $userEmail")
            } catch (e: Exception) {
                Log.e(TAG, "Hiba az FCM token regisztráció indítása során: ${e.message}")
            }
        }
    }

    fun sendPushNotification(targetEmail: String) {
        viewModelScope.launch {
            try {
                sendPushNotificationUseCase(
                    targetEmail = targetEmail,
                    title = "Új értesítés 🎾",
                    body = "Helló, $targetEmail! Értesítést kaptál."
                )
                Log.i(TAG, "Push notification elküldve: $targetEmail")
            } catch (e: Exception) {
                Log.e(TAG, "Push notification küldési hiba", e)
            }
        }
    }

    // --- CRUD Funkciók ---

    fun addPlayer(name: String, age: Int?, email: String) {
        viewModelScope.launch {
            try {
                addPlayerUseCase(NewPlayerDTO(name, age, email))
                registerFcmTokenForUser(email)

            } catch (e: Exception) {
                error.value = "Hiba hozzáadáskor: ${e.message}"
                Log.e(TAG, "Hiba hozzáadáskor", e)
            }
        }
    }

    fun deletePlayer(id: Int) {
        viewModelScope.launch {
            try {
                deletePlayerUseCase(id)
            } catch (e: Exception) {
                error.value = "Hiba törléskor: ${e.message}"
                Log.e(TAG, "Hiba törléskor", e)
            }
        }
    }

    // Játékos frissítése
    fun updatePlayer(id: Int, name: String, age: Int?, email: String) {
        viewModelScope.launch {
            try {
                val newPlayer = NewPlayerDTO(name, age, email)
                updatePlayerUseCase(id, newPlayer)
                // Megjegyzés: Nincs szükség a lista frissítésére, mert a WS esemény (PlayerUpdated) megteszi
            } catch (e: Exception) {
                error.value = "Hiba frissítéskor: ${e.message}"
                Log.e(TAG, "Hiba frissítéskor", e)
            }
        }
    }
}
