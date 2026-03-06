package hu.bme.aut.android.demo.util

/**
 * Egy generikus osztály, amely egy adatfolyam (Flow) állapotát írja le.
 * Lehet Loading (tölt), Success (sikeres adatokkal) vagy Error (hiba).
 */
class Resource<out T>(val isLoading: Boolean, private val data: T?, val error: Throwable?) {
    companion object {
        fun <T> loading() = Resource<T>(true, null, null)
        fun <T> success(data: T) = Resource<T>(false, data, null)
        fun <T> error(e: Throwable) = Resource<T>(false, null, e)
    }

    // Segédfüggvények a biztonságos adateléréshez
    fun getOrNull() = data
    fun exceptionOrNull() = error
}