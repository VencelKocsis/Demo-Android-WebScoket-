package hu.bme.aut.android.demo.data.network.api.auth

import hu.bme.aut.android.demo.data.auth.model.UserDTO
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface AuthRetrofitApi {
    @POST("/auth/sync")
    suspend fun syncUser(@Body user: UserDTO): UserDTO

    @PUT("/auth/me")
    suspend fun updateUser(@Body user: UserDTO): UserDTO

    @GET("users/{uid}")
    suspend fun getUserById(@Path("uid") uid: String): UserDTO
}