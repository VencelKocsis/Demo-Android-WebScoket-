package hu.bme.aut.android.demo.domain.fcm.usecases

import hu.bme.aut.android.demo.data.fcm.repository.FcmRepository
import hu.bme.aut.android.demo.domain.websocket.repository.PlayerRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class FcmUseCasesTest {

    @Test
    fun `RegisterFcmTokenUseCase calls PlayerRepository`() = runTest {
        // GIVEN
        val playerRepository = mockk<PlayerRepository>()
        coEvery { playerRepository.registerFcmToken(any(), any()) } returns Unit

        val useCase = RegisterFcmTokenUseCase(playerRepository)

        // WHEN
        useCase("email@test.com", "token_123")

        // THEN
        coVerify { playerRepository.registerFcmToken("email@test.com", "token_123") }
    }

    @Test
    fun `SendPushNotificationUseCase calls FcmRepository`() = runTest {
        // GIVEN
        val fcmRepository = mockk<FcmRepository>()
        coEvery { fcmRepository.sendPushNotification(any(), any(), any()) } returns Unit

        val useCase = SendPushNotificationUseCase(fcmRepository)

        // WHEN
        useCase("target@mail.com", "Title", "Body")

        // THEN
        coVerify { fcmRepository.sendPushNotification("target@mail.com", "Title", "Body") }
    }
}