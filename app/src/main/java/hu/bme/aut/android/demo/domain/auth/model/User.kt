package hu.bme.aut.android.demo.domain.auth.model

import hu.bme.aut.android.demo.domain.equipment.model.Equipment

data class User(
    val id: Int = 0,
    val email: String,
    val firstName: String,
    val lastName: String,
    val equipment: List<Equipment> = emptyList()
)