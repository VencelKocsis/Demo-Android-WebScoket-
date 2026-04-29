package hu.bme.aut.android.demo.data.fcm.repository

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class FcmRepositoryImplTest {

    private val retrofitApi = mockk<RetrofitApi>()
    private val repository = FcmRepositoryImpl(retrofitApi)

    @Test
    fun `sendPushNotification constructs correct payload`() = runTest {
        // GIVEN
        val email = "target@mail.com"
        val title = "Hello"
        val body = "World"

        // Slot segítségével elkapjuk, mit küldött a repo az API-nak (Map<String, String>)
        val payloadSlot = slot<Map<String, String>>()
        coEvery { retrofitApi.sendPushNotification(capture(payloadSlot)) } returns Unit

        // WHEN
        repository.sendPushNotification(email, title, body)

        // THEN
        val payload = payloadSlot.captured
        assertEquals(email, payload["targetEmail"])
        assertEquals(title, payload["title"])
        assertEquals(body, payload["body"])

        coVerify { retrofitApi.sendPushNotification(any()) }
    }
}