package hu.bme.aut.android.demo.domain.market.usecase

import hu.bme.aut.android.demo.domain.market.repository.MarketRepository
import javax.inject.Inject

/**
 * UseCase egy konkrét eladó felszerelés iránti érdeklődés elküldéséhez.
 * * Ezt hívja meg a ViewModel, amikor a felhasználó a Piacon rákattint az "Érdekel!" gombra.
 * * A háttérben ez egy push értesítést generál az eladónak.
 */
class InquireEquipmentUseCase @Inject constructor(
    private val repository: MarketRepository
) {
    /**
     * @param equipmentId A kiszemelt felszerelés (ütő) egyedi azonosítója.
     */
    suspend operator fun invoke(equipmentId: Int) {
        repository.inquireAboutEquipment(equipmentId)
    }
}