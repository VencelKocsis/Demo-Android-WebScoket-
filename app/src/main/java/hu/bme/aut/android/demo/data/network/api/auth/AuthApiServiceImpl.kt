package hu.bme.aut.android.demo.data.network.api.auth

import hu.bme.aut.android.demo.data.auth.mapper.toDTO
import hu.bme.aut.android.demo.data.auth.mapper.toDomain
import hu.bme.aut.android.demo.domain.auth.model.User
import javax.inject.Inject

class AuthApiServiceImpl @Inject constructor(
    private val authRetrofitApi: AuthRetrofitApi
) : AuthApiService {
    override suspend fun syncUser(user: User): User = authRetrofitApi.syncUser(user.toDTO()).toDomain()
    override suspend fun updateUser(user: User): User = authRetrofitApi.updateUser(user.toDTO()).toDomain()
    override suspend fun getUserById(uid: String): User = authRetrofitApi.getUserById(uid).toDomain()
}