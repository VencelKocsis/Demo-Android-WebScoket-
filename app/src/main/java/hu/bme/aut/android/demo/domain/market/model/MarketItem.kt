package hu.bme.aut.android.demo.domain.market.model

import hu.bme.aut.android.demo.domain.equipment.model.Equipment

/**
 * A piacon (Market) szereplő hirdetés tiszta üzleti (Domain) modellje.
 * * Ez az osztály teljesen független a hálózati JSON formátumtól, a UI kizárólag
 * ezt a modellt használja a piac képernyő megjelenítéséhez.
 *
 * @property equipment A hirdetett felszerelés (ütőfa és borítások) tiszta Domain modellje.
 * @property ownerName Az eladó (tulajdonos) teljes neve (pl. "Kovács János"), hogy a vevő lássa, kitől vásárol.
 * @property ownerId A tulajdonos egyedi belső azonosítója, amely alapján a backend értesíteni tudja őt.
 */
data class MarketItem(
    val equipment: Equipment,
    val ownerName: String,
    val ownerId: Int
)