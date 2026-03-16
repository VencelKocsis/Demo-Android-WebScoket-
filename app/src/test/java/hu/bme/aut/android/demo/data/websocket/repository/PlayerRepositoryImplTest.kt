package hu.bme.aut.android.demo.data.websocket.repository

import hu.bme.aut.android.demo.data.fcm.model.FcmToken
import hu.bme.aut.android.demo.data.network.api.RetrofitApi
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class PlayerRepositoryImplTest {

    private val retrofitApi = mockk<RetrofitApi>()
    // A WebSocket kilenst 'relaxed'-re állítjuk, hogy ne kellen mindent mockolni benne
    private val wsClient = mockk<PlayersWebSocketClient>(relaxed = true)

    // SUT (System Under Test)
    private lateinit var repository: PlayerRepositoryImpl

    @Before
    fun setup() {
        repository = PlayerRepositoryImpl(retrofitApi, wsClient)
    }

    @Test
    fun `getInitialPlayers calls ApiService`() = runTest {
        // GIVEN
        val mockPlayers = listOf(PlayerDTO(1, "Test", 20, "test@test.com"))
        coEvery { retrofitApi.getPlayers() } returns mockPlayers

        // WHEN
        val result = repository.getInitialPlayers()

        // THEN
        coVerify { retrofitApi.getPlayers() }
    }

    @Test
    fun `addPlayer calls ApiService`() = runTest {
        // GIVEN
        val newPlayer = NewPlayerDTO("New", 10, "new@test.com")
        val returnPlayer = PlayerDTO(1, "New", 10, "new@test.com")

        coEvery { retrofitApi.addPlayer(newPlayer) } returns returnPlayer

        // WHEN
        val result = repository.addPlayer(newPlayer)

        // THEN
        assertEquals(returnPlayer, result)
        coVerify { retrofitApi.addPlayer(newPlayer) }
    }

    @Test
    fun `deletePlayer calls ApiService`() = runTest {
        // GIVEN
        coEvery { retrofitApi.deletePlayer(any()) } returns Unit

        // WHEN
        repository.deletePlayer(1)

        // THEN
        coVerify { retrofitApi.deletePlayer(1) }
    }

    @Test
    fun `registerFcmToken maps parameters to FcmToken and calls ApiService`() = runTest {
        // GIVEN
        val email = "email@test.com"
        val token = "xyz_token"
        val expectedDto = FcmToken(email, token)

        coEvery { retrofitApi.registerFcmToken(any()) } returns Unit

        // WHEN
        repository.registerFcmToken(email, token)

        // THEN
        // Ellenőrizzük, hogy a repository helyesen csomagolta-e be az adatokat FcmToken objektumba
        coVerify { retrofitApi.registerFcmToken(expectedDto) }
    }

    @Test
    fun `getWsEventsFlow connects client and returns flow`() {
        // GIVEN
        val mockFlow = MutableSharedFlow<WsEvent>()
        every { wsClient.events } returns mockFlow

        // WHEN
        val resultFlow = repository.getWsEventsFlow()

        // THEN
        verify { wsClient.connect() } // Ellenőrizzük, hogy meghívta-e a csatlakozást
        assertEquals(mockFlow, resultFlow)
    }
}