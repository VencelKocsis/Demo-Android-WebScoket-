package hu.bme.aut.android.demo.data.market.model

import hu.bme.aut.android.demo.data.racket.model.RacketDTO
import kotlinx.serialization.Serializable

@Serializable
data class MarketItemDTO(
    val racket: RacketDTO,
    val ownerName: String,
    val ownerId: Int
)