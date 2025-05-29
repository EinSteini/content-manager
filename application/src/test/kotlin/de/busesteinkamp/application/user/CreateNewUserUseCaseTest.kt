package de.busesteinkamp.application.user

import de.busesteinkamp.domain.platform.SocialMediaPlatform
import de.busesteinkamp.domain.user.User
import de.busesteinkamp.domain.user.UserRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CreateNewUserUseCaseTest {

    @Test
    fun executeWithValidNameAndPlatformsCreatesAndSavesUser() {
        // Arrange
        val userRepository = mockk<UserRepository> {
            every { save(any()) } answers { firstArg() }
        }
        val useCase = CreateNewUserUseCase(userRepository)
        val platforms = listOf(
            mockk<SocialMediaPlatform> { every { name } returns "Twitter" },
            mockk<SocialMediaPlatform> { every { name } returns "Facebook" }
        )

        // Act
        val user = useCase.execute("John Doe", platforms)

        // Assert
        assertEquals("John Doe", user.name)
        assertEquals(2, user.platforms.size)
        verify { userRepository.save(user) }
    }

    @Test
    fun executeWithBlankNameThrowsException() {
        // Arrange
        val userRepository = mockk<UserRepository>()
        val useCase = CreateNewUserUseCase(userRepository)
        val platforms = listOf(mockk<SocialMediaPlatform>())

        // Act & Assert
        val exception = assertThrows<IllegalArgumentException> {
            useCase.execute(" ", platforms)
        }

        assertEquals("User name cannot be blank", exception.message)
    }

    @Test
    fun executeWithEmptyPlatformsCreatesUserWithoutPlatforms() {
        // Arrange
        val userRepository = mockk<UserRepository> {
            every { save(any()) } answers { firstArg() }
        }
        val useCase = CreateNewUserUseCase(userRepository)

        // Act
        val user = useCase.execute("John Doe", emptyList())

        // Assert
        assertEquals("John Doe", user.name)
        assertTrue(user.platforms.isEmpty())
        verify { userRepository.save(user) }
    }
}