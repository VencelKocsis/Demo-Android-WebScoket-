package hu.bme.aut.android.demo.data.fcm.model

import kotlinx.serialization.Serializable

@Serializable
data class FcmToken(
    val userId: String,
    val token: String
)