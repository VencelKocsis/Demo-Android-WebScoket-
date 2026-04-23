package hu.bme.aut.android.demo.feature.racketEditor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.bme.aut.android.demo.domain.catalog.usecase.GetBladeManufacturersUseCase
import hu.bme.aut.android.demo.domain.catalog.usecase.GetBladeModelsUseCase
import hu.bme.aut.android.demo.domain.catalog.usecase.GetRubberManufacturersUseCase
import hu.bme.aut.android.demo.domain.catalog.usecase.GetRubberModelsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// Az állapot, amit a UI figyelni fog
data class RacketEditorUiState(
    val isLoading: Boolean = true,

    // Lista a legördülőkhöz az adatbázisból
    val bladeManufacturers: List<String> = emptyList(),
    val rubberManufacturers: List<String> = emptyList(),

    val availableBladeModels: List<String> = emptyList(),
    val availableFhModels: List<String> = emptyList(),
    val availableBhModels: List<String> = emptyList(),

    val rubberColors: List<String> = listOf("Red", "Black", "Blue", "Green", "Pink", "Purple"),

    // A felhasználó jelenlegi választásai
    val currentBlade: Blade = Blade(),
    val currentForehand: Rubber = Rubber(color = "Black"),
    val currentBackhand: Rubber = Rubber(color = "Red")
)

@HiltViewModel
class RacketEditorViewModel @Inject constructor(
    private val getBladeManufacturersUseCase: GetBladeManufacturersUseCase,
    private val getBladeModelsUseCase: GetBladeModelsUseCase,
    private val getRubberManufacturersUseCase: GetRubberManufacturersUseCase,
    private val getRubberModelsUseCase: GetRubberModelsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RacketEditorUiState())
    val uiState: StateFlow<RacketEditorUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            // Jelezzük a UI-nak, hogy még töltünk
            _uiState.update { it.copy(isLoading = true) }

            // Lekérjük a gyártókat a Room adatbázisból
            var bladeMfs = getBladeManufacturersUseCase()
            var rubberMfs = getRubberManufacturersUseCase()

            // --- VERSENYHELYZET (RACE CONDITION) KIVÉDÉSE ---
            // Ha az app legelső indításakor a Room még épp a háttérben tölti be az adatokat
            // az onCreate callbackből, akkor várunk egy picit, és újra lekérdezzük.
            // Maximum 10-szer próbáljuk (kb. 2 másodperc), utána feladjuk.
            var retries = 0
            while (bladeMfs.isEmpty() && retries < 10) {
                kotlinx.coroutines.delay(200) // Várunk 200 ms-ot
                bladeMfs = getBladeManufacturersUseCase()
                rubberMfs = getRubberManufacturersUseCase()
                retries++
            }

            _uiState.update {
                it.copy(
                    bladeManufacturers = bladeMfs,
                    rubberManufacturers = rubberMfs,
                    isLoading = false
                )
            }

            // Ha van adat az adatbázisban, kiválasztjuk az elsőt alapértelmezettnek
            if (bladeMfs.isNotEmpty()) updateBladeManufacturer(bladeMfs.first())
            if (rubberMfs.isNotEmpty()) {
                updateFhManufacturer(rubberMfs.first())
                updateBhManufacturer(rubberMfs.first())
            }
        }
    }

    // --- FA ESEMÉNYEK ---
    fun updateBladeManufacturer(manufacturer: String) {
        viewModelScope.launch {
            // Ha változik a gyártó, lekérjük a Hozzá tartozó modelleket!
            val models = getBladeModelsUseCase(manufacturer)
            val newModel = models.firstOrNull() ?: ""
            _uiState.update { state ->
                state.copy(
                    availableBladeModels = models,
                    currentBlade = state.currentBlade.copy(manufacturer = manufacturer, model = newModel)
                )
            }
        }
    }

    fun updateBladeModel(model: String) {
        _uiState.update { it.copy(currentBlade = it.currentBlade.copy(model = model)) }
    }

    // --- TENYERES (FH) ESEMÉNYEK ---
    fun updateFhManufacturer(manufacturer: String) {
        viewModelScope.launch {
            val models = getRubberModelsUseCase(manufacturer)
            val newModel = models.firstOrNull() ?: ""
            _uiState.update { state ->
                state.copy(
                    availableFhModels = models,
                    currentForehand = state.currentForehand.copy(manufacturer = manufacturer, model = newModel)
                )
            }
        }
    }

    fun updateFhModel(model: String) {
        _uiState.update { it.copy(currentForehand = it.currentForehand.copy(model = model)) }
    }

    fun updateFhColor(color: String) {
        _uiState.update { it.copy(currentForehand = it.currentForehand.copy(color = color)) }
    }

    // --- FONÁK (BH) ESEMÉNYEK ---
    fun updateBhManufacturer(manufacturer: String) {
        viewModelScope.launch {
            val models = getRubberModelsUseCase(manufacturer)
            val newModel = models.firstOrNull() ?: ""
            _uiState.update { state ->
                state.copy(
                    availableBhModels = models,
                    currentBackhand = state.currentBackhand.copy(manufacturer = manufacturer, model = newModel)
                )
            }
        }
    }

    fun updateBhModel(model: String) {
        _uiState.update { it.copy(currentBackhand = it.currentBackhand.copy(model = model)) }
    }

    fun updateBhColor(color: String) {
        _uiState.update { it.copy(currentBackhand = it.currentBackhand.copy(color = color)) }
    }

    // --- MENTÉS ---
    fun saveRacket() {
        // TODO: Backend (Ktor) hívás a mentéshez
        val currentState = _uiState.value
        println("Mentés: ${currentState.currentBlade}, FH: ${currentState.currentForehand}, BH: ${currentState.currentBackhand}")
    }
}