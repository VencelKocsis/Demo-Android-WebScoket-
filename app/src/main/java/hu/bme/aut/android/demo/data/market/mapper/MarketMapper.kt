package hu.bme.aut.android.demo.data.market.mapper

import hu.bme.aut.android.demo.data.market.model.MarketItemDTO
import hu.bme.aut.android.demo.data.racket.mapper.toDTO
import hu.bme.aut.android.demo.data.racket.mapper.toDomain
import hu.bme.aut.android.demo.domain.market.model.MarketItem

fun MarketItemDTO.toDomain(): MarketItem {
    return MarketItem(
        equipment = this.racket.toDomain(),
        ownerName = this.ownerName,
        ownerId = this.ownerId
    )
}

fun MarketItem.toDTO(): MarketItemDTO {
    return MarketItemDTO(
        racket = this.equipment.toDTO(),
        ownerName = this.ownerName,
        ownerId = this.ownerId
    )
}