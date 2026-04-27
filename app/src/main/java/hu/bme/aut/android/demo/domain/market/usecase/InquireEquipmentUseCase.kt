package hu.bme.aut.android.demo.domain.market.usecase

import hu.bme.aut.android.demo.domain.market.repository.MarketRepository
import javax.inject.Inject

class InquireEquipmentUseCase @Inject constructor(
    private val repository: MarketRepository
) {
    suspend operator fun invoke(equipmentId: Int) {
        repository.inquireAboutEquipment(equipmentId)
    }
}