package hu.bme.aut.android.demo.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
sealed class WsEvent {
    @Serializable
    @SerialName("PlayerAdded")
    data class PlayerAdded(val player: PlayerDTO) : WsEvent()

    @Serializable
    @SerialName("PlayerDeleted")
    data class PlayerDeleted(val id: Int) : WsEvent()
}
