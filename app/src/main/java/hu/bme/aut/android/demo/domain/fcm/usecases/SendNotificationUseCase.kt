package hu.bme.aut.android.demo.domain.fcm.usecases

import hu.bme.aut.android.demo.data.fcm.repository.FcmRepository
import javax.inject.Inject

class SendPushNotificationUseCase @Inject constructor(
    private val repository: FcmRepository
) {
    suspend operator fun invoke(targetEmail: String, title: String, body: String) {
        repository.sendPushNotification(targetEmail, title, body)
    }
}
