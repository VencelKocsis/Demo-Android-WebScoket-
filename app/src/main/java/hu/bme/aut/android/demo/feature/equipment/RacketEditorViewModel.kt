package hu.bme.aut.android.demo.feature.equipment

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.bme.aut.android.demo.domain.catalog.usecase.GetBladeManufacturersUseCase
import hu.bme.aut.android.demo.domain.catalog.usecase.GetBladeModelsUseCase
import hu.bme.aut.android.demo.domain.catalog.usecase.GetRubberManufacturersUseCase
import hu.bme.aut.android.demo.domain.catalog.usecase.GetRubberModelsUseCase
import hu.bme.aut.android.demo.domain.equipment.model.Equipment
import hu.bme.aut.android.demo.domain.equipment.usecase.DeleteUserEquipmentUseCase
import hu.bme.aut.android.demo.domain.equipment.usecase.GetEquipmentByIdUseCase
import hu.bme.aut.android.demo.domain.equipment.usecase.SaveUserEquipmentUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Az Ütő Szerkesztő logikai központja.
 * * Kezeli a katalógusadatok (gyártók, modellek) lekérését a megfelelő UseCase-eken keresztül.
 * * Fogadja a UI-tól a felhasználói interakciókat, és frissíti a [RacketEditorUiState]-et.
 */
@HiltViewModel
class RacketEditorViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getBladeManufacturersUseCase: GetBladeManufacturersUseCase,
    private val getBladeModelsUseCase: GetBladeModelsUseCase,
    private val getRubberManufacturersUseCase: GetRubberManufacturersUseCase,
    private val getRubberModelsUseCase: GetRubberModelsUseCase,
    private val saveUserEquipmentUseCase: SaveUserEquipmentUseCase,
    private val deleteUserEquipmentUseCase: DeleteUserEquipmentUseCase,
    private val getEquipmentByIdUseCase: GetEquipmentByIdUseCase
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

            // Pragmatikus újrapróbálkozás, ha a katalógus esetleg még nem töltött be
            var retries = 0
            while (bladeMfs.isEmpty() && retries < 10) {
                delay(200)
                bladeMfs = getBladeManufacturersUseCase()
                rubberMfs = getRubberManufacturersUseCase()
                retries++
            }

            _uiState.update {
                it.copy(
                    bladeManufacturers = bladeMfs,
                    rubberManufacturers = rubberMfs
                )
            }

            val editRacketId = savedStateHandle.get<Int>("racketId")

            // Ha létező ütőt szerkesztünk, betöltjük az adatait
            if (editRacketId != null) {
                try {
                    val existingRacket = getEquipmentByIdUseCase(editRacketId)

                    if (existingRacket != null) {
                        val bModels = getBladeModelsUseCase(existingRacket.bladeManufacturer)
                        val fhModels = getRubberModelsUseCase(existingRacket.fhRubberManufacturer)
                        val bhModels = getRubberModelsUseCase(existingRacket.bhRubberManufacturer)

                        _uiState.update { state ->
                            state.copy(
                                racketId = existingRacket.id,
                                availableBladeModels = bModels,
                                availableFhModels = fhModels,
                                availableBhModels = bhModels,
                                currentBlade = Blade(
                                    manufacturer = existingRacket.bladeManufacturer,
                                    model = existingRacket.bladeModel
                                ),
                                currentForehand = Rubber(
                                    manufacturer = existingRacket.fhRubberManufacturer,
                                    model = existingRacket.fhRubberModel,
                                    color = existingRacket.fhRubberColor
                                ),
                                currentBackhand = Rubber(
                                    manufacturer = existingRacket.bhRubberManufacturer,
                                    model = existingRacket.bhRubberModel,
                                    color = existingRacket.bhRubberColor
                                ),
                                isForSale = existingRacket.isForSale,
                                isLoading = false
                            )
                        }
                        return@launch
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            _uiState.update { it.copy(isLoading = false) }

            // Ha új ütőt hozunk létre, betöltjük az első gyártó modelljeit
            if (bladeMfs.isNotEmpty()) updateBladeManufacturer(bladeMfs.first())
            if (rubberMfs.isNotEmpty()) {
                updateFhManufacturer(rubberMfs.first())
                updateBhManufacturer(rubberMfs.first())
            }
        }
    }

    fun updateIsForSale(isForSale: Boolean) {
        _uiState.update { it.copy(isForSale = isForSale) }
    }

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

    fun saveRacket() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val state = _uiState.value

                // A UI adatokból tiszta Domain modellt építünk
                val domainModel = Equipment(
                    id = state.racketId,
                    bladeManufacturer = state.currentBlade.manufacturer,
                    bladeModel = state.currentBlade.model,
                    fhRubberManufacturer = state.currentForehand.manufacturer,
                    fhRubberModel = state.currentForehand.model,
                    fhRubberColor = state.currentForehand.color,
                    bhRubberManufacturer = state.currentBackhand.manufacturer,
                    bhRubberModel = state.currentBackhand.model,
                    bhRubberColor = state.currentBackhand.color,
                    isForSale = state.isForSale
                )

                saveUserEquipmentUseCase(domainModel)
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Hiba a mentésnél: ${e.message}") }
            }
        }
    }

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