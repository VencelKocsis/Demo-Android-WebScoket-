package hu.bme.aut.android.demo.data.auth.mapper

import hu.bme.aut.android.demo.data.auth.model.UserDTO
import hu.bme.aut.android.demo.data.racket.mapper.toDTO
import hu.bme.aut.android.demo.data.racket.mapper.toDomain
import hu.bme.aut.android.demo.domain.auth.model.User

fun UserDTO.toDomain(): User {
    return User(
        id = this.id,
        email = this.email,
        firstName = this.firstName,
        lastName = this.lastName,
        equipment = this.equipment.map { it.toDomain() }
    )
}

fun User.toDTO(): UserDTO {
    return UserDTO(
        id = this.id,
        email = this.email,
        firstName = this.firstName,
        lastName = this.lastName,
        equipment = this.equipment.map { it.toDTO() }
    )
}