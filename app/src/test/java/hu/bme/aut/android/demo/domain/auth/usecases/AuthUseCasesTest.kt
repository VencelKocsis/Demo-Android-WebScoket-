package hu.bme.aut.android.demo.domain.auth.usecases

import com.google.firebase.auth.FirebaseUser
import hu.bme.aut.android.demo.domain.auth.repository.AuthRepository
import hu.bme.aut.android.demo.domain.auth.usecase.SignInUserUseCase
import hu.bme.aut.android.demo.domain.auth.usecase.SignOutUserUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthUseCasesTest {

    private val repository = mockk<AuthRepository>()
    private val mockUser = mockk<FirebaseUser>()

    @Test
    fun `RegisterUserUseCase returns success result`() = runTest {
        // GIVEN
        val email = "new@user.com"
        val password = "password"
        coEvery { repository.registerUser(email, password) } returns Result.success(mockUser)
        val useCase = RegisterUserUseCase(repository)

        // WHEN
        val result = useCase(email, password)

        // THEN
        assertTrue(result.isSuccess)
        assertEquals(mockUser, result.getOrNull())
        coVerify { repository.registerUser(email, password) }
    }

    @Test
    fun `SignInUserUseCase returns failure on repository error`() = runTest {
        // GIVEN
        val email = "bad@user.com"
        val password = "wrong"
        val exception = Exception("Login failed")

        coEvery { repository.signInUser(email, password) } returns Result.failure(exception)
        val useCase = SignInUserUseCase(repository)

        // WHEN
        val result = useCase(email, password)

        // THEN
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    @Test
    fun `SignOutUserUseCase calls repository`() {
        // GIVEN
        every { repository.signOutUser() } returns Unit
        val useCase = SignOutUserUseCase(repository)

        // WHEN
        useCase()

        // THEN
        verify { repository.signOutUser() }
    }
}