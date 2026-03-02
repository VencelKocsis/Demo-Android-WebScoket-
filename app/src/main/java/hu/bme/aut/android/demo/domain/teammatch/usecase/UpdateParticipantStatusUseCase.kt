package hu.bme.aut.android.demo.domain.teammatch.usecase

import hu.bme.aut.android.demo.data.network.api.ApiService
import javax.inject.Inject

class UpdateParticipantStatusUseCase @Inject constructor(
    private val apiService: ApiService
) {
    suspend operator fun invoke(participantId: Int, status: String) {
        apiService.updateParticipantStatus(participantId, status)
    }
}