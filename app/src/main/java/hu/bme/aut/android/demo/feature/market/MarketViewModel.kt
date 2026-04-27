package hu.bme.aut.android.demo.feature.market

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.bme.aut.android.demo.domain.market.model.MarketItem
import hu.bme.aut.android.demo.domain.market.usecase.GetMarketItemsUseCase
import hu.bme.aut.android.demo.domain.market.usecase.InquireEquipmentUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MarketUiState(
    val isLoading: Boolean = true,
    val items: List<MarketItem> = emptyList(),
    val errorMessage: String? = null,
    val inquirySuccessMessage: String? = null
)

@HiltViewModel
class MarketViewModel @Inject constructor(
    private val getMarketItemsUseCase: GetMarketItemsUseCase,
    private val inquireEquipmentUseCase: InquireEquipmentUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MarketUiState())
    val uiState: StateFlow<MarketUiState> = _uiState.asStateFlow()

    init {
        loadMarketItems()
    }

    fun loadMarketItems() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val items = getMarketItemsUseCase()
                _uiState.update { it.copy(isLoading = false, items = items) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Hiba a piac betöltésekor: ${e.message}") }
            }
        }
    }

    fun inquireAboutEquipment(equipmentId: Int) {
        viewModelScope.launch {
            try {
                inquireEquipmentUseCase(equipmentId)
                _uiState.update { it.copy(inquirySuccessMessage = "Érdeklődés sikeresen elküldve a tulajdonosnak!") }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Nem sikerült elküldeni az érdeklődést.") }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, inquirySuccessMessage = null) }
    }
}