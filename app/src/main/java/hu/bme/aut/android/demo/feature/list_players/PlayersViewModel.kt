package hu.bme.aut.android.demo.feature.list_players

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.bme.aut.android.demo.domain.fcm.usecases.RegisterFcmTokenUseCase
import hu.bme.aut.android.demo.domain.fcm.usecases.SendPushNotificationUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
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

    private val _uiState = MutableStateFlow(PlayersUiState(loading = true))
    val uiState: StateFlow<PlayersUiState> = _uiState.asStateFlow()

    // A lista tartalmát a Use Case-től kapott Flow-ból töltjük fel
    val players = mutableStateOf<List<PlayerDTO>>(emptyList())


    init {
        viewModelScope.launch {

            // 1. WebSocket események figyelése a lista frissítéséhez
            val wsFlow = observePlayersEventsUseCase()
                .onEach { event ->
                    // Frissítjük a lista állapotát a WS események alapján
                    _uiState.update { currentState ->
                        when (event) {
                            is WsEvent.PlayerAdded -> {
                                Log.i(TAG, "WS: PlayerAdded - ${event.player.name}")

                                // 🔑 JAVÍTÁS: CSAK AKKOR ADJA HOZZÁ, HA AZ ID MÉG NEM LÉTEZIK
                                if (currentState.players.none { it.id == event.player.id }) {
                                    currentState.copy(
                                        players = currentState.players + event.player
                                    )
                                } else {
                                    // Ha az ID már létezik (duplikátum), visszatérünk a jelenlegi állapottal
                                    Log.w(TAG, "Figyelem: Duplikált PlayerAdded esemény érkezett. ID: ${event.player.id}")
                                    currentState
                                }
                            }
                            is WsEvent.PlayerDeleted -> {
                                // ... (logika nem változik)
                                currentState.copy(
                                    players = currentState.players.filter { it.id != event.id }
                                )
                            }
                            is WsEvent.PlayerUpdated -> {
                                // ... (logika nem változik)
                                currentState.copy(
                                    players = currentState.players.map {
                                        if (it.id == event.player.id) event.player else it
                                    }
                                )
                            }
                        }
                    }
                }
                .launchIn(viewModelScope)

            // 2. HTTP hívás a kezdeti listáért
            try {
                val initialList = getInitialPlayersUseCase()
                Log.d(TAG, "Initial players loaded: ${initialList.size}")

                // Frissítjük a StateFlow-t a kezdeti listával és eltávolítjuk a loading állapotot
                _uiState.update { it.copy(players = initialList, loading = false, error = null) }

            } catch (e: Exception) {
                Log.e(TAG, "Error loading initial list", e)
                _uiState.update { it.copy(loading = false, error = "Hiba a játékosok betöltésekor: ${e.message}") }
            }
        }
    }

    // --- FCM Funkció ---
    private fun registerFcmTokenForUser(userEmail: String) {
        viewModelScope.launch {
            try {
                val token = FirebaseMessaging.getInstance().token.await()
                Log.d(TAG, "Lekért FCM token: $token, Email: $userEmail")
                registerFcmTokenUseCase(userEmail, token)
                Log.i(TAG, "FCM token sikeresen elküldve a backendnek: $userEmail")
            } catch (e: Exception) {
                Log.e(TAG, "Hiba az FCM token regisztrációja során: ${e.message}", e)
                // Figyelem: A hiba csak a logba megy, nem jelenik meg a UI-n
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
                // Hiba jelzése a UI-n, ha szükséges (jelenleg nincs UI esemény ehhez)
            }
        }
    }

    // --- CRUD Funkciók ---

    fun addPlayer(name: String, age: Int?, email: String) {
        viewModelScope.launch {
            try {
                val newPlayer = NewPlayerDTO(name, age, email)
                addPlayerUseCase(newPlayer)

                registerFcmTokenForUser(email)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Hiba hozzáadáskor: ${e.message}") }
                Log.e(TAG, "Hiba hozzáadáskor", e)
            }
        }
    }

    fun deletePlayer(id: Int) {
        viewModelScope.launch {
            try {
                deletePlayerUseCase(id)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Hiba törléskor: ${e.message}") }
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
                _uiState.update { it.copy(error = "Hiba frissítéskor: ${e.message}") }
                Log.e(TAG, "Hiba frissítéskor", e)
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
