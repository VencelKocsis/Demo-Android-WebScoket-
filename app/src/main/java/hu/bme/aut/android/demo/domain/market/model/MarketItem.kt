package hu.bme.aut.android.demo.domain.market.model

import hu.bme.aut.android.demo.domain.equipment.model.Equipment

data class MarketItem(
    val equipment: Equipment,
    val ownerName: String,
    val ownerId: Int
)