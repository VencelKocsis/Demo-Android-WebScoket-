package hu.bme.aut.android.demo.data.network

import hu.bme.aut.android.demo.domain.model.WsEvent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.WebSocket
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import io.mockk.mockk

class PlayersWebSocketClientTest {

    private lateinit var mockOkHttpClient: OkHttpClient
    private lateinit var json: Json
    private lateinit var wsClient: PlayersWebSocketClient

    @Before
    fun setUp() {
        json = Json {
            classDiscriminator = "type"
            ignoreUnknownKeys = true
        }
        mockOkHttpClient = mockk(relaxed = true)
        wsClient = PlayersWebSocketClient(mockOkHttpClient, json)
    }

    @Test
    fun onMessage_PlayerAdded_decodesCorrectly() = runTest {
        val rawJson = """
            {"type": "PlayerAdded", "player": {"id": 1, "name": "Béla", "age": 25}}
        """.trimIndent()

        val listener = wsClient.playerWebSocketListener
        val mockWs = mockk<WebSocket>(relaxed = true)

        // When: Megküldjük a nyers üzenetet (hívja a tryEmit-et)
        listener.onMessage(mockWs, rawJson)

        // JAVÍTÁS: Kényszerítjük a Coroutine Scheduler-t az emit azonnali feldolgozására
        testScheduler.runCurrent()

        // Assert: Első event kiolvasása
        val emittedEvent = wsClient.events.first()

        assertTrue(emittedEvent is WsEvent.PlayerAdded)
        val added = emittedEvent as WsEvent.PlayerAdded
        assertEquals(1, added.player.id)
        assertEquals("Béla", added.player.name)
        assertEquals(25, added.player.age)
    }

    @Test
    fun onMessage_PlayerDeleted_decodesCorrectly() = runTest {
        val rawJson = """{"type": "PlayerDeleted", "id": 42}"""

        val listener = wsClient.playerWebSocketListener
        val mockWs = mockk<WebSocket>(relaxed = true)

        // When
        listener.onMessage(mockWs, rawJson)

        // JAVÍTÁS: Kényszerítjük a Coroutine Scheduler-t az emit azonnali feldolgozására
        testScheduler.runCurrent()

        // Assert
        val emittedEvent = wsClient.events.first()

        assertTrue(emittedEvent is WsEvent.PlayerDeleted)
        val deleted = emittedEvent as WsEvent.PlayerDeleted
        assertEquals(42, deleted.id)
    }
}