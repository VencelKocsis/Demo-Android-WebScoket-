package hu.bme.aut.android.demo.models

import kotlinx.serialization.Serializable

@Serializable
data class PlayerDTO(
    val id: Int,
    val name: String,
    val age: Int?
)