package hu.bme.aut.android.demo.data.websocket.repository

import hu.bme.aut.android.demo.data.fcm.model.FcmToken
import hu.bme.aut.android.demo.data.network.api.ApiService
import hu.bme.aut.android.demo.data.websocket.PlayersWebSocketClient
import hu.bme.aut.android.demo.domain.websocket.model.NewPlayerDTO
import hu.bme.aut.android.demo.domain.websocket.model.PlayerDTO
import hu.bme.aut.android.demo.domain.websocket.model.WsEvent
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

    private val apiService = mockk<ApiService>()
    // A WebSocket kilenst 'relaxed'-re állítjuk, hogy ne kellen mindent mockolni benne
    private val wsClient = mockk<PlayersWebSocketClient>(relaxed = true)

    // SUT (System Under Test)
    private lateinit var repository: PlayerRepositoryImpl

    @Before
    fun setup() {
        repository = PlayerRepositoryImpl(apiService, wsClient)
    }

    @Test
    fun `getInitialPlayers calls ApiService`() = runTest {
        // GIVEN
        val mockPlayers = listOf(PlayerDTO(1, "Test", 20, "test@test.com"))
        coEvery { apiService.getPlayers() } returns mockPlayers

        // WHEN
        val result = repository.getInitialPlayers()

        // THEN
        coVerify { apiService.getPlayers() }
    }

    @Test
    fun `addPlayer calls ApiService`() = runTest {
        // GIVEN
        val newPlayer = NewPlayerDTO("New", 10, "new@test.com")
        val returnPlayer = PlayerDTO(1, "New", 10, "new@test.com")

        coEvery { apiService.addPlayer(newPlayer) } returns returnPlayer

        // WHEN
        val result = repository.addPlayer(newPlayer)

        // THEN
        assertEquals(returnPlayer, result)
        coVerify { apiService.addPlayer(newPlayer) }
    }

    @Test
    fun `deletePlayer calls ApiService`() = runTest {
        // GIVEN
        coEvery { apiService.deletePlayer(any()) } returns Unit

        // WHEN
        repository.deletePlayer(1)

        // THEN
        coVerify { apiService.deletePlayer(1) }
    }

    @Test
    fun `registerFcmToken maps parameters to FcmToken and calls ApiService`() = runTest {
        // GIVEN
        val email = "email@test.com"
        val token = "xyz_token"
        val expectedDto = FcmToken(email, token)

        coEvery { apiService.registerFcmToken(any()) } returns Unit

        // WHEN
        repository.registerFcmToken(email, token)

        // THEN
        // Ellenőrizzük, hogy a repository helyesen csomagolta-e be az adatokat FcmToken objektumba
        coVerify { apiService.registerFcmToken(expectedDto) }
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