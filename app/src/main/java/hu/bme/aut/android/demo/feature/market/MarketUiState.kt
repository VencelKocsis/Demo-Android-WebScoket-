package hu.bme.aut.android.demo.feature.market

import hu.bme.aut.android.demo.domain.market.model.MarketItem

/**
 * A Piac (Market) képernyő egyetlen igazságforrása.
 * Tartalmazza a betöltési állapotot, az eladó felszereléseket ([MarketItem]),
 * és a felugró üzeneteket (hiba vagy siker).
 */
data class MarketUiState(
    val isLoading: Boolean = true,
    val items: List<MarketItem> = emptyList(),
    val errorMessage: String? = null,
    val inquirySuccessMessage: String? = null
)

/**
 * MVI (Model-View-Intent) események a Piac képernyőhöz.
 * * Csak ezeken keresztül kommunikálhat a UI a ViewModel-lel.
 */
sealed class MarketEvent {
    /** A piac adatainak manuális frissítése (újratöltés). */
    object RefreshMarket : MarketEvent()

    /** Érdeklődés küldése (Push Notification) egy adott felszerelés tulajdonosának. */
    data class InquireAboutEquipment(val equipmentId: Int) : MarketEvent()

    /** A megjelenített Snackbar üzenetek törlése az állapotból. */
    object ClearMessages : MarketEvent()
}