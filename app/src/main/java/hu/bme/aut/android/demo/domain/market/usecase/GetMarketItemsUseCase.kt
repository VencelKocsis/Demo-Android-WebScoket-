package hu.bme.aut.android.demo.domain.market.usecase

import hu.bme.aut.android.demo.domain.market.model.MarketItem
import hu.bme.aut.android.demo.domain.market.repository.MarketRepository
import javax.inject.Inject

class GetMarketItemsUseCase @Inject constructor(
    private val repository: MarketRepository
) {
    suspend operator fun invoke(): List<MarketItem> {
        return repository.getMarketItems()
    }
}