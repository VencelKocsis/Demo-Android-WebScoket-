package hu.bme.aut.android.demo.data.network.interceptor

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * Ez az Interceptor minden kimenő Retrofit kérést elfog, és ha a felhasználó
 * be van jelentkezve, hozzácsatolja a Firebase ID tokent az "Authorization" fejlécbe.
 */
class AuthInterceptor @Inject constructor(
    private val auth: FirebaseAuth
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()

        // Megpróbáljuk megszerezni a jelenlegi felhasználót
        val currentUser = auth.currentUser

        if (currentUser != null) {
            try {
                // A Firebase token lekérése aszinkron művelet, de az Interceptor
                // szinkron fut (runBlocking szükséges a háttérszálon futó hálózati híváshoz)
                val tokenResult = runBlocking {
                    // ForceRefresh=false, mert a Firebase SDK automatikusan kezeli a lejáratot (1 óra)
                    currentUser.getIdToken(false).await()
                }

                val token = tokenResult.token

                if (!token.isNullOrEmpty()) {
                    // Ha megvan a token, klónozzuk a kérést és hozzáadjuk a fejlécet
                    request = request.newBuilder()
                        .addHeader("Authorization", "Bearer $token")
                        .build()
                }
            } catch (e: Exception) {
                // Ha hiba történik (pl. nincs internet a token frissítéshez),
                // az eredeti (token nélküli) kérést küldjük el.
                e.printStackTrace()
            }
        }

        // Továbbengedjük a kérést (fejléccel vagy anélkül) a szerver felé
        return chain.proceed(request)
    }
}