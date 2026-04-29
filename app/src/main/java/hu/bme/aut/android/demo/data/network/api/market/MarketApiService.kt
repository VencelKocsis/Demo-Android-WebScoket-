package hu.bme.aut.android.demo.data.network.api.market

import hu.bme.aut.android.demo.domain.market.model.MarketItem

/**
 * A Piac hálózati műveleteinek tiszta szerződése a Data rétegen belül.
 * * Elrejti a hálózati implementációt, és csak Domain modelleket ([MarketItem]) ad vissza.
 */
interface MarketApiService {
    suspend fun getMarketItems(): List<MarketItem>
    suspend fun inquireAboutEquipment(equipmentId: Int)
}