package hu.bme.aut.android.demo

import android.util.Log
import hu.bme.aut.android.demo.models.WsEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener

class PlayersWebSocketClient {
    private val client = OkHttpClient()
    private var ws: WebSocket? = null

    private val json = Json {
        classDiscriminator = "type"
        ignoreUnknownKeys = true
    }

    private val _events = MutableSharedFlow<WsEvent>(extraBufferCapacity = 5)
    val events = _events.asSharedFlow()

    fun connect() {
        val request = Request.Builder()
            .url("ws://10.0.2.2:8080/ws/players")
            .build()

        ws = client.newWebSocket(request, object : WebSocketListener() {

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    Log.d("WS", "Received raw: $text")

                    val event = json.decodeFromString(WsEvent.serializer(), text)

                    val emitted = _events.tryEmit(event)
                    // Ez a log mostantól "true" kell legyen!
                    Log.i("WS", "Event emitted to flow: $emitted, Event: ${event.javaClass.simpleName}")

                } catch (e: Exception) {
                    Log.e("WS", "Hiba az üzenet feldolgozásakor: ${e.message}", e)
                }
            }
        })
    }

    fun close() {
        ws?.close(1000, "Closing")
    }
}