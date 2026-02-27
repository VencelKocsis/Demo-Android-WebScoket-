package hu.bme.aut.android.demo.data.auth.model

import kotlinx.serialization.Serializable

@Serializable
data class UserDTO(
    val id: Int = 0,
    val email: String,
    val firstName: String,
    val lastName: String
)
