package hu.bme.aut.android.demo.domain.fcm.repository

/**
 * A Firebase Cloud Messaging (FCM) műveletek szerződése (interfésze) a Domain rétegben.
 * * Meghatározza a push értesítésekhez szükséges tokenek regisztrálásának és az
 * üzenetek küldésének szabályait, függetlenül attól, hogy a Data réteg hogyan valósítja meg azokat.
 */
interface FcmRepository {
    /**
     * Regisztrálja a felhasználó aktuális eszközének FCM tokenjét a backend szerveren.
     *
     * @param email A felhasználó e-mail címe (azonosítója).
     * @param token A készülék egyedi Firebase push értesítés tokenje.
     */
    suspend fun registerFcmToken(email: String, token: String)

    /**
     * Kezdeményezi egy push értesítés küldését a backend API-n keresztül.
     *
     * @param targetEmail A címzett felhasználó e-mail címe.
     * @param title Az értesítés címe (fejléce).
     * @param body Az értesítés szöveges tartalma (üzenete).
     */
    suspend fun sendPushNotification(targetEmail: String, title: String, body: String)
}