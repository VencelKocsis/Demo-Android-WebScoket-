package hu.bme.aut.android.demo.domain.websocket.model

import kotlinx.serialization.Serializable

@Serializable
data class PlayerDTO(
    val id: Int,
    val name: String,
    val age: Int?
)