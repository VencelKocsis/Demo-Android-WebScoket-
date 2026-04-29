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

/**
 * Alacsony szintű hálózati osztály, amely az OkHttp segítségével kezeli
 * a valós idejű (WebSocket) kapcsolatot a Ktor backenddel.
 */
class MatchWebSocketClient(
    private val client: OkHttpClient,
    private val json: Json
) {
    private var ws: WebSocket? = null

    // A SharedFlow tökéletes választás események (event bus) sugárzására
    private val _events = MutableSharedFlow<MatchWsEvent>(extraBufferCapacity = 10)
    val events = _events.asSharedFlow()

    fun connect() {
        if (ws != null) return // Ne csatlakozzunk duplán

        val request = Request.Builder()
            .url("wss://ktor-demo-c3yb.onrender.com/ws/matches")
            .build()

        ws = client.newWebSocket(request, object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    // A JSON szöveget azonnal átalakítjuk Kotlin objektummá
                    val event = json.decodeFromString(MatchWsEvent.serializer(), text)
                    _events.tryEmit(event)
                } catch (e: Exception) {
                    Log.e("MatchWS", "Hiba az üzenet feldolgozásakor: ${e.message}", e)
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
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
        // Implementálandó, ha kliensről is küldünk fel üzenetet
    }
}