package hu.bme.aut.android.demo.data.fcm.repository

import hu.bme.aut.android.demo.data.network.api.ApiService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class FcmRepositoryTest {

    private val apiService = mockk<ApiService>()
    private val repository = FcmRepository(apiService)

    @Test
    fun `sendPushNotification constructs correct payload`() = runTest {
        // GIVEN
        val email = "target@mail.com"
        val title = "Hello"
        val body = "World"

        // Slot segítségével elkapjuk, mit küldött a repo az API-nak (Map<String, String>)
        val payloadSlot = slot<Map<String, String>>()
        coEvery { apiService.sendPushNotification(capture(payloadSlot)) } returns Unit

        // WHEN
        repository.sendPushNotification(email, title, body)

        // THEN
        val payload = payloadSlot.captured
        assertEquals(email, payload["targetEmail"])
        assertEquals(title, payload["title"])
        assertEquals(body, payload["body"])

        coVerify { apiService.sendPushNotification(any()) }
    }
}