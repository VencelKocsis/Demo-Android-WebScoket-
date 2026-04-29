package hu.bme.aut.android.demo.data.market.mapper

import hu.bme.aut.android.demo.data.market.model.MarketItemDTO
import hu.bme.aut.android.demo.data.racket.mapper.toDTO
import hu.bme.aut.android.demo.data.racket.mapper.toDomain
import hu.bme.aut.android.demo.domain.market.model.MarketItem

/**
 * A hálózati adatátviteli objektumot (DTO) alakítja át a Piac üzleti logikájában (Domain)
 * használt tiszta objektummá.
 * * A belső ütő (Racket) objektum konvertálását a már létező RacketMapper-re bízza.
 *
 * @return A [MarketItem] domain modell.
 */
fun MarketItemDTO.toDomain(): MarketItem {
    return MarketItem(
        equipment = this.racket.toDomain(),
        ownerName = this.ownerName,
        ownerId = this.ownerId
    )
}

/**
 * A Domain modellt alakítja át hálózati adatátviteli objektummá (DTO),
 * amelyet el lehet küldeni a backend szervernek (pl. új eladó elem felöltésekor).
 *
 * @return A JSON szerializálásra kész [MarketItemDTO].
 */
fun MarketItem.toDTO(): MarketItemDTO {
    return MarketItemDTO(
        racket = this.equipment.toDTO(),
        ownerName = this.ownerName,
        ownerId = this.ownerId
    )
}