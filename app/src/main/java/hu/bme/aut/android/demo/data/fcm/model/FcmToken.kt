package hu.bme.aut.android.demo.data.fcm.model

import kotlinx.serialization.Serializable

/**
 * Hálózati adatátviteli objektum (DTO) az FCM token szerverre küldéséhez.
 *
 * A szerver ezen tokenek alapján tudja, hogy pontosan melyik fizikai eszközre
 * (vagy eszközökre) kell elküldenie a push értesítést.
 *
 * @property email A felhasználó e-mail címe, akihez a token tartozik.
 * @property token A készülék egyedi Firebase push értesítés tokenje.
 */
@Serializable
data class FcmToken(
    val email: String,
    val token: String
)