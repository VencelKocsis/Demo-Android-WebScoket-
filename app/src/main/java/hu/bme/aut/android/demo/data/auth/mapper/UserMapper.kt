package hu.bme.aut.android.demo.data.auth.mapper

import hu.bme.aut.android.demo.data.auth.model.UserDTO
import hu.bme.aut.android.demo.data.equipment.mapper.toDTO
import hu.bme.aut.android.demo.data.equipment.mapper.toDomain
import hu.bme.aut.android.demo.domain.auth.model.User

/**
 * A hálózati adatátviteli objektumot (DTO) alakítja át az üzleti logikában (Domain)
 * használt tiszta objektummá.
 * * Ez a függvény biztosítja, hogy a Domain réteg ne találkozzon DTO-kkal.
 *
 * @return A [User] domain modell.
 */
fun UserDTO.toDomain(): User {
    return User(
        id = this.id,
        email = this.email,
        firstName = this.firstName,
        lastName = this.lastName,
        // A felszerelések listáját is át kell konvertálni (DTO -> Domain)
        equipment = this.equipment.map { it.toDomain() }
    )
}

/**
 * A Domain modellt alakítja át hálózati adatátviteli objektummá (DTO),
 * amelyet már el lehet küldeni a backend szervernek.
 *
 * @return A JSON szerializálásra kész [UserDTO].
 */
fun User.toDTO(): UserDTO {
    return UserDTO(
        id = this.id,
        email = this.email,
        firstName = this.firstName,
        lastName = this.lastName,
        // A felszerelések listáját is át kell konvertálni (Domain -> DTO)
        equipment = this.equipment.map { it.toDTO() }
    )
}