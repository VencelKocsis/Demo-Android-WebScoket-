package hu.bme.aut.android.demo.data.network.api.market

import hu.bme.aut.android.demo.data.market.model.MarketItemDTO
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * A Retrofit hálózati interfésze a Piac (Market) funkcióhoz.
 * * Csak ez a réteg ismeri az API végpontokat és a [MarketItemDTO] formátumot.
 */
interface MarketRetrofitApi {
    @GET("/api/market/equipment")
    suspend fun getMarketItems(): List<MarketItemDTO>

    @POST("/api/market/equipment/{id}/inquire")
    suspend fun inquireAboutEquipment(@Path("id") equipmentId: Int)
}