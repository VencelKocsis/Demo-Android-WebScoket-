package hu.bme.aut.android.demo.data.network.api.auth

import hu.bme.aut.android.demo.data.auth.mapper.toDTO
import hu.bme.aut.android.demo.data.auth.mapper.toDomain
import hu.bme.aut.android.demo.domain.auth.model.User
import javax.inject.Inject

/**
 * Az [AuthApiService] interfész konkrét megvalósítása.
 * * Ez az osztály működik "tolmácsként": fogadja a Repository-ból érkező tiszta Domain
 * modelleket, átalakítja őket hálózati DTO-vá (a [toDTO] mapperrel), majd átadja a
 * [AuthRetrofitApi]-nak. A kapott választ visszakonvertálja Domain jellé (a [toDomain] mapperrel).
 */
class AuthApiServiceImpl @Inject constructor(
    private val authRetrofitApi: AuthRetrofitApi
) : AuthApiService {

    override suspend fun syncUser(user: User): User =
        authRetrofitApi.syncUser(user.toDTO()).toDomain()

    override suspend fun updateUser(user: User): User =
        authRetrofitApi.updateUser(user.toDTO()).toDomain()

    override suspend fun getUserById(uid: String): User =
        authRetrofitApi.getUserById(uid).toDomain()
}