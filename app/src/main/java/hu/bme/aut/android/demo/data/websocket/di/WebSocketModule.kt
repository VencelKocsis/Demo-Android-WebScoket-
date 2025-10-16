package hu.bme.aut.android.demo.data.websocket.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import hu.bme.aut.android.demo.data.websocket.PlayersWebSocketClient
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WebSocketModule {

    // ----------------------------------------------------
    // 1. PlayersWebSocketClient (WS kapcsolat)
    // ----------------------------------------------------
    @Provides
    @Singleton
    fun providePlayersWebSocketClient(okHttpClient: OkHttpClient, json: Json): PlayersWebSocketClient {
        // A Hilt injektálja a NetworkModule-ban definiált OkHttpClient-et és Json-t.
        return PlayersWebSocketClient(okHttpClient, json)
    }
}
