package hu.bme.aut.android.demo.feature.racketEditor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.bme.aut.android.demo.domain.catalog.usecase.GetBladeManufacturersUseCase
import hu.bme.aut.android.demo.domain.catalog.usecase.GetBladeModelsUseCase
import hu.bme.aut.android.demo.domain.catalog.usecase.GetRubberManufacturersUseCase
import hu.bme.aut.android.demo.domain.catalog.usecase.GetRubberModelsUseCase
import hu.bme.aut.android.demo.domain.equipment.model.Equipment
import hu.bme.aut.android.demo.domain.equipment.usecase.DeleteUserEquipmentUseCase
import hu.bme.aut.android.demo.domain.equipment.usecase.SaveUserEquipmentUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// Az állapot, amit a UI figyelni fog
data class RacketEditorUiState(
    val isLoading: Boolean = true,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val racketId: Int? = null, // null ha új ütő, Int ha létezőt szerkesztünk

    // Lista a legördülőkhöz az adatbázisból
    val bladeManufacturers: List<String> = emptyList(),
    val rubberManufacturers: List<String> = emptyList(),

    val availableBladeModels: List<String> = emptyList(),
    val availableFhModels: List<String> = emptyList(),
    val availableBhModels: List<String> = emptyList(),

    // TODO: Ezt is lehetne adatbázisból tölteni, de most hardcoded marad
    val rubberColors: List<String> = listOf("Red", "Black", "Blue", "Green", "Pink", "Purple"),

    // A felhasználó jelenlegi választásai
    val currentBlade: Blade = Blade(),
    val currentForehand: Rubber = Rubber(color = "Black"),
    val currentBackhand: Rubber = Rubber(color = "Red")
)

@HiltViewModel
class RacketEditorViewModel @Inject constructor(
    // Helyi (Katalógus) UseCase-ek
    private val getBladeManufacturersUseCase: GetBladeManufacturersUseCase,
    private val getBladeModelsUseCase: GetBladeModelsUseCase,
    private val getRubberManufacturersUseCase: GetRubberManufacturersUseCase,
    private val getRubberModelsUseCase: GetRubberModelsUseCase,

    // API (Felszerelés) UseCase-ek
    private val saveUserEquipmentUseCase: SaveUserEquipmentUseCase,
    private val deleteUserEquipmentUseCase: DeleteUserEquipmentUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RacketEditorUiState())
    val uiState: StateFlow<RacketEditorUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            var bladeMfs = getBladeManufacturersUseCase()
            var rubberMfs = getRubberManufacturersUseCase()

            // Versenyhelyzet kivédése első indításkor
            var retries = 0
            while (bladeMfs.isEmpty() && retries < 10) {
                kotlinx.coroutines.delay(200)
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

    // --- MENTÉS (ÚJ VAGY LÉTEZŐ) ---
    fun saveRacket() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val state = _uiState.value

                val domainModel = Equipment(
                    id = state.racketId,
                    bladeManufacturer = state.currentBlade.manufacturer,
                    bladeModel = state.currentBlade.model,
                    fhRubberManufacturer = state.currentForehand.manufacturer,
                    fhRubberModel = state.currentForehand.model,
                    fhRubberColor = state.currentForehand.color,
                    bhRubberManufacturer = state.currentBackhand.manufacturer,
                    bhRubberModel = state.currentBackhand.model,
                    bhRubberColor = state.currentBackhand.color
                )

                saveUserEquipmentUseCase(domainModel)
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Hiba a mentésnél: ${e.message}") }
            }
        }
    }

    // --- TÖRLÉS ---
    fun deleteRacket() {
        val id = _uiState.value.racketId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                deleteUserEquipmentUseCase(id)
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Hiba a törlésnél: ${e.message}") }
            }
        }
    }
}