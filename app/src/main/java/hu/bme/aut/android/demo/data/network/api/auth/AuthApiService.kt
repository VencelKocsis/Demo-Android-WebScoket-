package hu.bme.aut.android.demo.data.network.api.auth

import hu.bme.aut.android.demo.domain.auth.model.User

/**
 * A felhasználói hálózati műveletek elvont (absztrakt) szerződése a Data rétegen belül.
 * * Elrejti a Repository elől, hogy a háttérben milyen hálózati könyvtár (pl. Retrofit) fut.
 * * Csak tiszta Domain modellekkel ([User]) dolgozik, hálózati DTO-k itt már nem szerepelnek.
 */
interface AuthApiService {
    suspend fun syncUser(user: User): User
    suspend fun updateUser(user: User): User
    suspend fun getUserById(uid: String): User
}