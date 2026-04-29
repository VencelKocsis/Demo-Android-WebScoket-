package hu.bme.aut.android.demo.domain.market.usecase

import hu.bme.aut.android.demo.domain.market.model.MarketItem
import hu.bme.aut.android.demo.domain.market.repository.MarketRepository
import javax.inject.Inject

/**
 * UseCase az aktuálisan eladó felszerelések (piaci hirdetések) lekérdezéséhez.
 * * A ViewModel ezt az osztályt hívja meg, amikor a felhasználó megnyitja a Piac képernyőt,
 * vagy amikor "lehúzza frissítésre" (Pull-to-refresh) a listát.
 */
class GetMarketItemsUseCase @Inject constructor(
    private val repository: MarketRepository
) {
    /**
     * @return A piacon lévő hirdetések listája tiszta Domain modellekként.
     */
    suspend operator fun invoke(): List<MarketItem> {
        return repository.getMarketItems()
    }
}