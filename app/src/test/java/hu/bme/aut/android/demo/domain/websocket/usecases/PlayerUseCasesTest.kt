package hu.bme.aut.android.demo.domain.websocket.usecases

import hu.bme.aut.android.demo.domain.websocket.model.NewPlayerDTO
import hu.bme.aut.android.demo.domain.websocket.model.PlayerDTO
import hu.bme.aut.android.demo.domain.websocket.model.WsEvent
import hu.bme.aut.android.demo.domain.websocket.repository.PlayerRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class PlayerUseCasesTest {

    // Közös mock Repository
    private val repository = mockk<PlayerRepository>()

    @Test
    fun `GetInitialPlayersUseCase returns list from repository`() = runTest {
        // GIVEN
        val expectedList = listOf(PlayerDTO(1, "Test", 20, "email"))
        coEvery { repository.getInitialPlayers() } returns expectedList

        val useCase = GetInitialPlayersUseCase(repository)

        // WHEN
        val result = useCase()

        // THEN
        assertEquals(expectedList, result)
        coVerify { repository.getInitialPlayers() }
    }

    @Test
    fun `AddPlayerUseCase calls repository with correct params`() = runTest {
        // GIVEN
        val newPlayer = NewPlayerDTO("Jani", 10, "jani@test.com")
        val returnedPlayer = PlayerDTO(1, "Jani", 10, "jani@test.com")

        coEvery { repository.addPlayer(newPlayer) } returns returnedPlayer
        val useCase = AddPlayerUseCase(repository)

        // WHEN
        val result = useCase(newPlayer)

        // THEN
        assertEquals(returnedPlayer, result)
        coVerify { repository.addPlayer(newPlayer) }
    }

    @Test
    fun `DeletePlayerUseCase calls repository`() = runTest {
        // GIVEN
        val id = 123
        coEvery { repository.deletePlayer(id) } returns Unit
        val useCase = DeletePlayerUseCase(repository)

        // WHEN
        useCase(id)

        // THEN
        coVerify { repository.deletePlayer(id) }
    }

    @Test
    fun `UpdatePlayerUseCase calls repository`() = runTest {
        // GIVEN
        val id = 1
        val updateData = NewPlayerDTO("Updated", 25, "mail")
        coEvery { repository.updatePlayer(id, updateData) } returns Unit
        val useCase = UpdatePlayerUseCase(repository)

        // WHEN
        useCase(id, updateData)

        // THEN
        coVerify { repository.updatePlayer(id, updateData) }
    }

    @Test
    fun `ObservePlayersEventsUseCase returns flow from repository`() {
        // GIVEN
        val mockFlow = flowOf<WsEvent>()
        every { repository.getWsEventsFlow() } returns mockFlow

        // --- EZ IS HIÁNYZOTT: ---
        val useCase = ObservePlayersEventsUseCase(repository)

        // WHEN
        val result = useCase()

        // THEN
        assertEquals(mockFlow, result)
        verify { repository.getWsEventsFlow() }
    }
}