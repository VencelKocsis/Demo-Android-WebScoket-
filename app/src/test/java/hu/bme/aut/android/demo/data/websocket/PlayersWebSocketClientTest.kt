package hu.bme.aut.android.demo.data.websocket

import app.cash.turbine.test
import hu.bme.aut.android.demo.domain.websocket.model.WsEvent
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PlayersWebSocketClientTest {

    private val okHttpClient = mockk<OkHttpClient>()
    private val webSocket = mockk<WebSocket>(relaxed = true)

    // Igazi JSON serializer-t használunk, hogy a parse-olást is teszteljük
    private val json = Json {
        ignoreUnknownKeys = true
        classDiscriminator = "type"
    }

    private lateinit var client: PlayersWebSocketClient

    @Before
    fun setup() {
        client = PlayersWebSocketClient(okHttpClient, json)
    }

    @Test
    fun `incoming JSON message is parsed and emitted to flow`() = runTest {
        // GIVEN
        // Elkapjuk a WebSocketListener-t, amit a client.connect() hoz létre
        val listenerSlot = slot<WebSocketListener>()

        every {
            okHttpClient.newWebSocket(any(), capture(listenerSlot))
        } returns webSocket

        // Csatlakozunk (ez regisztrálja a listenert)
        client.connect()

        // A szerverről érkező minta JSON válasz (PlayerAdded)
        val jsonMessage = """
            {
                "type": "PlayerAdded",
                "player": {
                    "id": 100,
                    "name": "Test User",
                    "age": 25,
                    "email": "test@user.com"
                }
            }
        """.trimIndent()

        // WHEN & THEN (Turbine segítségével figyeljük a flow-t)
        client.events.test {
            // Szimuláljuk, hogy a WebSocket üzenetet kapott (meghívjuk a listener onMessage-ét)
            listenerSlot.captured.onMessage(webSocket, jsonMessage)

            // Várjuk az eseményt a Flow-ban
            val event = awaitItem()

            assertTrue(event is WsEvent.PlayerAdded)
            val addedEvent = event as WsEvent.PlayerAdded
            assertTrue(addedEvent.player.name == "Test User")
            assertTrue(addedEvent.player.id == 100)

            // Nincs több esemény
            cancelAndIgnoreRemainingEvents()
        }
    }
}