package hu.bme.aut.android.demo.feature.auth

/**
 * A felhasználó hitelesítési állapotát reprezentáló felsorolás.
 * Ez alapján dönti el az AppNavHost, hogy a LoginScreen-re vagy a DemoScreen-re navigáljon.
 */
enum class AuthState {
    /** A kezdeti állapot, amíg a hitelesítési token ellenőrzése fut. */
    UNKNOWN,

    /** A felhasználó sikeresen be van jelentkezve. */
    AUTHENTICATED,

    /** A felhasználó nincs bejelentkezve (kivéve, ha UNKNOWN). */
    UNAUTHENTICATED
}