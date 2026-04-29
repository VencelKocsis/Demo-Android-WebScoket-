package hu.bme.aut.android.demo.data.market.model

import hu.bme.aut.android.demo.data.equipment.model.RacketDTO
import kotlinx.serialization.Serializable

/**
 * Data Transfer Object (DTO) a piacon (Market) szereplő eladó felszerelések hálózati kommunikációjához.
 * * Csomagolja magát az eladó ütőt ([RacketDTO]) és a tulajdonos publikus adatait,
 * hogy a Piac képernyőn meg tudjuk jeleníteni, ki árulja az adott eszközt.
 *
 * @property racket Maga az eladásra kínált felszerelés (ütőfa és borítások).
 * @property ownerName A felszerelés tulajdonosának teljes neve (pl. "Kovács János").
 * @property ownerId A tulajdonos egyedi azonosítója a backend adatbázisban (az érdeklődés küldéséhez).
 */
@Serializable
data class MarketItemDTO(
    val racket: RacketDTO,
    val ownerName: String,
    val ownerId: Int
)