package de.busesteinkamp.domain.user

import de.busesteinkamp.domain.platform.SocialMediaPlatform
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*

class UserTest {

    @Test
    fun createUserWithValidNameAndPlatformsCreatesUserSuccessfully() {
        // Arrange
        val twitterPlatform = mockk<SocialMediaPlatform> {
            every { name } returns "Twitter"
            every { id } returns UUID.randomUUID()
        }
        val facebookPlatform = mockk<SocialMediaPlatform> {
            every { name } returns "Facebook"
            every { id } returns UUID.randomUUID()
        }
        val platforms = listOf(twitterPlatform, facebookPlatform)

        // Act
        val user = User.create("John Doe", platforms)

        // Assert
        assertEquals("John Doe", user.name)
        assertEquals(2, user.platforms.size)
        assertTrue(user.platforms.any { it.name == "Twitter" })
        assertTrue(user.platforms.any { it.name == "Facebook" })
    }

    @Test
    fun createUserWithBlankNameThrowsException() {
        // Arrange
        val blankName = " "

        // Act & Assert
        val exception = assertThrows(IllegalArgumentException::class.java) {
            User.create(blankName)
        }
        assertEquals("User name cannot be blank", exception.message)
    }

    @Test
    fun addPlatformAddsPlatformSuccessfully() {
        // Arrange
        val user = User.create("John Doe")
        val uuid = UUID.randomUUID()
        val platform = mockk<SocialMediaPlatform> {
            every { name } returns "Instagram"
            every { id } returns uuid
        }

        // Act
        user.addPlatform(platform)

        // Assert
        assertEquals(1, user.platforms.size)
        assertEquals("Instagram", user.platforms.first().name)
        assertEquals(uuid, user.platforms.first().id)
    }

    @Test
    fun addDuplicatePlatformThrowsException() {
        // Arrange
        val platform = mockk<SocialMediaPlatform> {
            every { name } returns "Instagram"
            every { id } returns UUID.randomUUID()
        }
        val user = User.create("John Doe", listOf(platform))

        // Act & Assert
        val exception = assertThrows(IllegalArgumentException::class.java) {
            user.addPlatform(platform)
        }
        assertEquals("Platform Instagram is already associated with this user", exception.message)
    }

    @Test
    fun removePlatformRemovesPlatformSuccessfully() {
        // Arrange
        val platform = mockk<SocialMediaPlatform> {
            every { name } returns "Instagram"
            every { id } returns UUID.randomUUID()
        }
        val user = User.create("John Doe", listOf(platform))

        // Act
        user.removePlatform("Instagram")

        // Assert
        assertTrue(user.platforms.isEmpty())
    }

    @Test
    fun removeNonExistentPlatformDoesNothing() {
        // Arrange
        val platform = mockk<SocialMediaPlatform> {
            every { name } returns "Instagram"
            every { id } returns UUID.randomUUID()
        }
        val user = User.create("John Doe", listOf(platform))

        // Act
        user.removePlatform("Twitter")

        // Assert
        assertEquals(1, user.platforms.size)
        assertEquals("Instagram", user.platforms.first().name)
    }

    @Test
    fun updateNameUpdatesNameSuccessfully() {
        // Arrange
        val user = User.create("John Doe")

        // Act
        user.updateName("Jane Doe")

        // Assert
        assertEquals("Jane Doe", user.name)
    }

    @Test
    fun updateNameWithBlankNameThrowsException() {
        // Arrange
        val user = User.create("John Doe")

        // Act & Assert
        val exception = assertThrows(IllegalArgumentException::class.java){
            user.updateName(" ")
        }
        assertEquals("User name cannot be blank", exception.message)
    }

    @Test
    fun reconstructUserWithValidDataCreatesUserSuccessfully() {
        // Arrange
        val uuid = UUID.randomUUID()
        val platform = mockk<SocialMediaPlatform> {
            every { name } returns "Twitter"
            every { id } returns UUID.randomUUID()
        }
        val platforms = listOf(platform)

        // Act
        val user = User.reconstruct(uuid, "John Doe", platforms)

        // Assert
        assertEquals(uuid, user.id)
        assertEquals("John Doe", user.name)
        assertEquals(1, user.platforms.size)
        assertEquals("Twitter", user.platforms.first().name)
    }
}