package de.busesteinkamp.domain.process

import de.busesteinkamp.domain.content.Content
import de.busesteinkamp.domain.platform.PublishParameters
import de.busesteinkamp.domain.platform.SocialMediaPlatform
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Instant
import java.util.*

class DistributionTest {
    @Test
    fun reportStatusWithInvalidPlatformThrowsException() {
        // Arrange
        val platform = mockk<SocialMediaPlatform> {
            every { name } returns "Twitter"
        }
        val content = mockk<Content> {
            every { id } returns UUID.randomUUID()
        }
        val publishParameters = mockk<PublishParameters> {
            every { publishDate } returns Date.from(Instant.now())
            every { title } returns "Test Content"
            every { description } returns "This is a test content"
        }
        val distribution = Distribution.create(content, publishParameters, listOf(platform))

        val invalidPlatform = mockk<SocialMediaPlatform> {
            every { name } returns "Facebook"
        }

        // Act & Assert

        val exception = assertThrows<IllegalArgumentException> {
            distribution.reportStatus(invalidPlatform, UploadStatus.FINISHED)
        }

        assertEquals("Platform Facebook is not part of this distribution", exception.message)
    }

    @Test
    fun isCompleteReturnsTrueWhenAllPlatformsAreFinishedOrFailed() {
        // Arrange
        val platform1 = mockk<SocialMediaPlatform> {
            every { name } returns "Twitter"
        }
        val platform2 = mockk<SocialMediaPlatform> {
            every { name } returns "Facebook"
        }
        val content = mockk<Content> {
            every { id } returns UUID.randomUUID()
        }
        val publishParameters = mockk<PublishParameters> {
            every { publishDate } returns Date.from(Instant.now())
            every { title } returns "Test Content"
            every { description } returns "This is a test content"
        }
        val distribution = Distribution.create(content, publishParameters, listOf(platform1, platform2))

        // Act
        distribution.reportStatus(platform1, UploadStatus.FINISHED)
        distribution.reportStatus(platform2, UploadStatus.FAILED)

        // Assert
        assertTrue(distribution.isComplete())
    }

    @Test
    fun isSuccessfulReturnsFalseWhenAnyPlatformFails() {
        // Arrange
        val platform1 = mockk<SocialMediaPlatform> {
            every { name } returns "Twitter"
        }
        val platform2 = mockk<SocialMediaPlatform> {
            every { name } returns "Facebook"
        }
        val content = mockk<Content> {
            every { id } returns UUID.randomUUID()
        }
        val publishParameters = mockk<PublishParameters> {
            every { publishDate } returns Date.from(Instant.now())
            every { title } returns "Test Content"
            every { description } returns "This is a test content"
        }
        val distribution = Distribution.create(content, publishParameters, listOf(platform1, platform2))

        // Act
        distribution.reportStatus(platform1, UploadStatus.FINISHED)
        distribution.reportStatus(platform2, UploadStatus.FAILED)

        // Assert
        assertFalse(distribution.isSuccessful())
    }

    @Test
    fun getOverallStatusReturnsNotStartedWhenAllStatusesAreInitial() {
        // Arrange
        val platform = mockk<SocialMediaPlatform> {
            every { name } returns "Twitter"
        }
        val content = mockk<Content> {
            every { id } returns UUID.randomUUID()
        }
        val publishParameters = mockk<PublishParameters> {
            every { publishDate } returns Date.from(Instant.now())
            every { title } returns "Test Content"
            every { description } returns "This is a test content"
        }
        val distribution = Distribution.create(content, publishParameters, listOf(platform))
        val wantedStatus = DistributionStatus.NOT_STARTED

        // Act
        val overallStatus = distribution.getOverallStatus()

        // Assert
        assertEquals(wantedStatus, overallStatus)
    }

    @Test
    fun getOverallStatusReturnsCompletedSuccessfullyWhenAllPlatformsAreFinished() {
        val platform1 = mockk<SocialMediaPlatform> {
            every { name } returns "Twitter"
        }
        val platform2 = mockk<SocialMediaPlatform> {
            every { name } returns "Facebook"
        }
        val content = mockk<Content> {
            every { id } returns UUID.randomUUID()
        }
        val publishParameters = mockk<PublishParameters> {
            every { publishDate } returns Date.from(Instant.now())
            every { title } returns "Test Content"
            every { description } returns "This is a test content"
        }
        val distribution = Distribution.create(content, publishParameters, listOf(platform1, platform2))

        distribution.reportStatus(platform1, UploadStatus.FINISHED)
        distribution.reportStatus(platform2, UploadStatus.FINISHED)

        assertEquals(DistributionStatus.COMPLETED_SUCCESSFULLY, distribution.getOverallStatus())
    }
}