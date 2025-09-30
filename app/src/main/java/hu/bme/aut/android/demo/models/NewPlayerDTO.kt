package hu.bme.aut.android.demo.models

import kotlinx.serialization.Serializable

@Serializable
data class NewPlayerDTO(
    val name: String,
    val age: Int?
)