package hu.bme.aut.android.demo.data.repository

import hu.bme.aut.android.demo.data.network.ApiService
import hu.bme.aut.android.demo.data.network.PlayersWebSocketClient
import hu.bme.aut.android.demo.domain.model.PlayerDTO
import hu.bme.aut.android.demo.domain.model.WsEvent
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class PlayerRepositoryImplTest {

    private lateinit var mockApiService: ApiService
    private lateinit var mockWsClient: PlayersWebSocketClient
    private lateinit var repository: PlayerRepositoryImpl

    private val testWsEvents = MutableSharedFlow<WsEvent>(extraBufferCapacity = 5)

    @Before
    fun setUp() {
        mockApiService = mockk()
        mockWsClient = mockk()

        // Mockoljuk az events flow-t és a connect hívást
        coEvery { mockWsClient.events } returns testWsEvents
        every { mockWsClient.connect() } just runs

        repository = PlayerRepositoryImpl(mockApiService, mockWsClient)
    }

    @Test
    fun getInitialPlayers_callsApiService() = runTest {
        val expected = listOf(PlayerDTO(1, "Test", 30))
        coEvery { mockApiService.getPlayers() } returns expected

        val actual = repository.getInitialPlayers()

        coVerify(exactly = 1) { mockApiService.getPlayers() }
        assertEquals(expected, actual)
    }

    @Test
    fun getWsEventsFlow_callsConnectAndReturnsClientFlow() = runTest {
        // When
        val flow = repository.getWsEventsFlow()

        // Then: connect() meghívódott
        io.mockk.verify(exactly = 1) { mockWsClient.connect() }
        assertNotNull(flow)

        // Extra: itt ténylegesen emitálunk valamit a tesztelt flow-ba
        val expectedEvent = WsEvent.PlayerDeleted(123)
        testWsEvents.emit(expectedEvent)

        // Most már biztonságosan olvashatjuk, mert tudjuk hogy jön érték
        val collected = flow.first()
        assertEquals(expectedEvent, collected)
    }
}