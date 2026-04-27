package hu.bme.aut.android.demo.data.network.api.auth

import hu.bme.aut.android.demo.domain.auth.model.User

interface AuthApiService {
    suspend fun syncUser(user: User): User
    suspend fun updateUser(user: User): User
    suspend fun getUserById(uid: String): User
}