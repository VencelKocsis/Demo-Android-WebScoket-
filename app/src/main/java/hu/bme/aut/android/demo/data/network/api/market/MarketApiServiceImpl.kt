package hu.bme.aut.android.demo.data.network.api.market

import hu.bme.aut.android.demo.data.market.mapper.toDomain
import hu.bme.aut.android.demo.domain.market.model.MarketItem
import javax.inject.Inject

/**
 * A [MarketApiService] megvalósítása.
 * * Lefordítja a [MarketRetrofitApi]-tól kapott [MarketItemDTO] listát
 * tiszta [MarketItem] Domain modellekké a Repository számára.
 */
class MarketApiServiceImpl @Inject constructor(
    private val marketRetrofitApi: MarketRetrofitApi
) : MarketApiService {
    override suspend fun getMarketItems(): List<MarketItem> =
        marketRetrofitApi.getMarketItems().map { it.toDomain() }

    override suspend fun inquireAboutEquipment(equipmentId: Int) =
        marketRetrofitApi.inquireAboutEquipment(equipmentId)
}