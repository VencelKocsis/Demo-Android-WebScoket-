package hu.bme.aut.android.demo.data.network

import android.util.Log
import hu.bme.aut.android.demo.domain.model.WsEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener

class PlayersWebSocketClient(
    private val client: OkHttpClient,
            private val json: Json
) {
    private var ws : WebSocket? = null

    private val _events = MutableSharedFlow<WsEvent>(extraBufferCapacity = 5)
    val events = _events.asSharedFlow()

    fun connect() {
        val request = Request.Builder()
            .url("wss://ktor-demo-c3yb.onrender.com/ws/players")
            .build()

        ws = client.newWebSocket(request, object : WebSocketListener() {

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    Log.d("WS", "Received raw: $text")
                    val event = json.decodeFromString(WsEvent.serializer(), text)
                    val emitted = _events.tryEmit(event)
                    Log.i("WS", "Event emitted to flow: $emitted, Event: ${event.javaClass.simpleName}")
                } catch (e: Exception) {
                    Log.e("WS", "Hiba az üzenet feldolgozásakor: ${e.message}", e)
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: okhttp3.Response?) {
                Log.e("WS", "WebSocket failure: ${t.message}")
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.w("WS", "WebSocket closed: $reason ($code)")
            }
        })
    }

    fun close() {
        ws?.close(1000, "Closing")
    }
}