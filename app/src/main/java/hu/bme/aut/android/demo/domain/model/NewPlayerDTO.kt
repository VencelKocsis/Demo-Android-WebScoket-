package hu.bme.aut.android.demo.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class NewPlayerDTO(
    val name: String,
    val age: Int?
)