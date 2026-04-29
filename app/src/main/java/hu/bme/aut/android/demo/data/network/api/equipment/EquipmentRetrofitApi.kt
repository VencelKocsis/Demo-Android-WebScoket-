package hu.bme.aut.android.demo.data.network.api.equipment

import hu.bme.aut.android.demo.data.racket.model.RacketDTO
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * A Retrofit hálózati interfésze a saját felszerelések (ütők) kezeléséhez.
 * * Ez az egyetlen hely, amely ismeri a HTTP kéréseket és a [RacketDTO] hálózati adatszerkezetet.
 */
interface EquipmentRetrofitApi {
    @POST("api/users/equipment")
    suspend fun saveEquipment(@Body racketDto: RacketDTO)

    @DELETE("api/users/equipment/{racketId}")
    suspend fun deleteEquipment(@Path("racketId") racketId: Int)
}