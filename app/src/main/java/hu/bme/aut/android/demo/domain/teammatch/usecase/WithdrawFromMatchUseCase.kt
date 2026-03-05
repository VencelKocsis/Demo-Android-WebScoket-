package hu.bme.aut.android.demo.domain.teammatch.usecase

import hu.bme.aut.android.demo.data.network.api.ApiService
import javax.inject.Inject

class WithdrawFromMatchUseCase @Inject constructor(
    private val apiService: ApiService
) {
    suspend operator fun invoke(matchId: Int) {
        apiService.withdrawFromMatch(matchId)
    }
}