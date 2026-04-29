package hu.bme.aut.android.demo.data.network.api.auth

import hu.bme.aut.android.demo.data.auth.model.UserDTO
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/**
 * A Retrofit könyvtár által használt alacsony szintű hálózati interfész.
 * * Kizárólag ez az osztály ismeri a HTTP protokoll részleteit (GET, POST, PUT),
 * az API végpontok útvonalait és a hálózati adatszerkezeteket (DTO).
 */
interface AuthRetrofitApi {

    /**
     * Szinkronizálja a felhasználó adatait a backenddel bejelentkezéskor vagy regisztrációkor.
     * @param user A küldendő felhasználói adatok (DTO).
     * @return A szerver által visszaadott (esetleg frissített) felhasználói adatok (DTO).
     */
    @POST("/auth/sync")
    suspend fun syncUser(@Body user: UserDTO): UserDTO

    /**
     * Frissíti a bejelentkezett felhasználó saját profiladatait.
     */
    @PUT("/auth/me")
    suspend fun updateUser(@Body user: UserDTO): UserDTO

    /**
     * Lekéri egy adott felhasználó nyilvános vagy részletes adatait a Firebase UID alapján.
     * @param uid A felhasználó egyedi Firebase azonosítója.
     */
    @GET("users/{uid}")
    suspend fun getUserById(@Path("uid") uid: String): UserDTO
}