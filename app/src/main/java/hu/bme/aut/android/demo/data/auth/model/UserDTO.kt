package hu.bme.aut.android.demo.data.auth.model

import kotlinx.serialization.Serializable
import hu.bme.aut.android.demo.data.equipment.model.RacketDTO

/**
 * Data Transfer Object (DTO) a felhasználó adatainak tárolására és hálózati küldésére.
 *
 * A [@Serializable] annotáció mondja meg a JSON konverternek, hogy ezt az osztályt
 * biztonságosan tudja szöveggé (JSON), illetve szövegből objektummá alakítani.
 *
 * @property id A felhasználó egyedi azonosítója az adatbázisban.
 * @property email A felhasználó e-mail címe (Firebase auth alapú).
 * @property firstName A felhasználó keresztneve.
 * @property lastName A felhasználó vezetékneve.
 * @property equipment A felhasználóhoz tartozó felszerelések (ütők) listája.
 */
@Serializable
data class UserDTO(
    val id: Int = 0,
    val email: String,
    val firstName: String,
    val lastName: String,
    val equipment: List<RacketDTO> = emptyList()
)