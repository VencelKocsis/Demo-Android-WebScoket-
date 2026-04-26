package hu.bme.aut.android.demo.data.auth.model

import kotlinx.serialization.Serializable
import hu.bme.aut.android.demo.data.racket.model.RacketDTO

@Serializable
data class UserDTO(
    val id: Int = 0,
    val email: String,
    val firstName: String,
    val lastName: String,
    val equipment: List<RacketDTO> = emptyList()
)
