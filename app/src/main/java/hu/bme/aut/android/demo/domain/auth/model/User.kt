package hu.bme.aut.android.demo.domain.auth.model

import hu.bme.aut.android.demo.domain.equipment.model.Equipment

/**
 * A felhasználó tiszta üzleti (Domain) modellje.
 * * Ez az osztály teljesen független a hálózati kommunikációtól (pl. JSON, DTO)
 * vagy a helyi adatbázistól. Az alkalmazás belső logikája (UI réteg, UseCase-ek)
 * kizárólag ezt a modellt használja.
 *
 * @property id A felhasználó egyedi azonosítója a saját backendünkön.
 * @property email A felhasználó e-mail címe.
 * @property firstName A felhasználó keresztneve.
 * @property lastName A felhasználó vezetékneve.
 * @property equipment A felhasználóhoz tartozó felszerelések (ütők) listája tiszta Domain modellként.
 */
data class User(
    val id: Int = 0,
    val email: String,
    val firstName: String,
    val lastName: String,
    val equipment: List<Equipment> = emptyList()
)