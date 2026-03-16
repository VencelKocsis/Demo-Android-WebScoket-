package hu.bme.aut.android.demo.data.websocket

import android.util.Log
import hu.bme.aut.android.demo.domain.websocket.model.MatchWsEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

class MatchWebSocketClient(
    private val client: OkHttpClient,
    private val json: Json
) {
    private var ws: WebSocket? = null

    private val _events = MutableSharedFlow<MatchWsEvent>(extraBufferCapacity = 10)
    val events = _events.asSharedFlow()

    fun connect() {
        if (ws != null) return // Ne csatlakozzunk duplán

        val request = Request.Builder()
        // FIGYELEM: Ez a Ktor backend új végpontja lesz!
            .url("wss://ktor-demo-c3yb.onrender.com/ws/matches")
            .build()

        ws = client.newWebSocket(request, object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val event = json.decodeFromString(MatchWsEvent.serializer(), text)
                    _events.tryEmit(event)
                } catch (e: Exception) {
                    Log.e("MatchWS", "Hiba az üzenet feldolgozásakor: ${e.message}", e)
                }
            }

            override fun onFailure(webScoket: WebSocket, t: Throwable, response: Response?) {
                Log.e("MatchWS", "WebSocket failure: ${t.message}")
                ws = null
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.w("MatchWS", "WebSocket closed: $reason ($code)")
                ws = null
            }
        })
    }

    fun close() {
        ws?.close(1000, "Normal closing")
        ws = null
    }

    fun signMatch(matchId: Int, teamSide: String) {

    }
}