package de.busesteinkamp.application.process

import de.busesteinkamp.domain.content.Content
import de.busesteinkamp.domain.events.DomainEventPublisher
import de.busesteinkamp.domain.platform.PublishParameters
import de.busesteinkamp.domain.process.Distribution
import de.busesteinkamp.domain.process.DistributionRepository
import de.busesteinkamp.domain.platform.SocialMediaPlatform
import de.busesteinkamp.domain.process.UploadStatus
import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.*

class ExecuteDistributionUseCaseTest {

    @Test
    fun executeSuccessfullyReportsStatusAndUpdatesRepository() {
        // Arrange
        val platform = mockk<SocialMediaPlatform> {
            every { upload(any(), any(), any()) } answers {
                thirdArg<(UploadStatus) -> Unit>().invoke(UploadStatus.FINISHED)
            }
        }
        val mockContent = mockk<Content>{
            every { get() } returns "content data"
        }
        val pp = mockk<PublishParameters>{
            every { publishDate } returns Date.from(Instant.now())
            every { title } returns "foo"
            every { description } returns "bar"
        }
        val distribution = mockk<Distribution> {
            every { platforms } returns listOf(platform)
            every { content } returns mockContent
            every { publishParameters } returns pp
            every { reportStatus(any(), any()) } just Runs
            every { hasUncommittedEvents() } returns false
        }
        val repository = mockk<DistributionRepository> {
            every { save(any()) } returns distribution
            every { update(any()) } just Runs
        }
        val eventPublisher = mockk<DomainEventPublisher> {
            every { publish(any()) } just Runs
        }
        val useCase = ExecuteDistributionUseCase(repository, eventPublisher)

        // Act
        useCase.execute(distribution)

        // Assert
        verify { distribution.reportStatus(platform, UploadStatus.PENDING) }
        verify { distribution.reportStatus(platform, UploadStatus.FINISHED) }
        verify { repository.update(distribution) }
    }

    @Test
    fun executeHandlesUploadFailureAndReportsFailedStatus() {
        // Arrange
        val platform = mockk<SocialMediaPlatform> {
            every { upload(any(), any(), any()) } throws RuntimeException("Upload failed")
        }
        val mockContent = mockk<Content>{
            every { get() } returns "content data"
        }
        val pp = mockk<PublishParameters>{
            every { publishDate } returns Date.from(Instant.now())
            every { title } returns ""
            every { description } returns ""
        }
        val distribution = mockk<Distribution> {
            every { platforms } returns listOf(platform)
            every { content } returns mockContent
            every { publishParameters } returns pp
            every { reportStatus(any(), any()) } just Runs
            every { hasUncommittedEvents() } returns false
        }
        val repository = mockk<DistributionRepository> {
            every { save(any()) } returns distribution
            every { update(any()) } just Runs
        }
        val eventPublisher = mockk<DomainEventPublisher> {
            every { publish(any()) } just Runs
        }
        val useCase = ExecuteDistributionUseCase(repository, eventPublisher)

        // Act
        useCase.execute(distribution)

        // Assert
        verify { distribution.reportStatus(platform, UploadStatus.PENDING) }
        verify { distribution.reportStatus(platform, UploadStatus.FAILED) }
        verify { repository.update(distribution) }
    }

    @Test
    fun executeWithEmptyPlatformsDoesNotReportOrUpdate() {
        // Arrange
        val mockContent = mockk<Content>{
            every { get() } returns "content data"
        }
        val pp = mockk<PublishParameters>{
            every { publishDate } returns Date.from(Instant.now())
            every { title } returns ""
            every { description } returns ""
        }
        val distribution = mockk<Distribution> {
            every { platforms } returns emptyList()
            every { content } returns mockContent
            every { publishParameters } returns pp
            every { reportStatus(any(), any()) } just Runs
            every { hasUncommittedEvents() } returns false
        }
        val repository = mockk<DistributionRepository> {
            every { save(any()) } returns distribution
            every { update(any()) } just Runs
        }
        val eventPublisher = mockk<DomainEventPublisher> {
            every { publish(any()) } just Runs
        }
        val useCase = ExecuteDistributionUseCase(repository, eventPublisher)

        // Act
        useCase.execute(distribution)

        // Assert
        verify(exactly = 0) { distribution.reportStatus(any(), any()) }
        verify(exactly = 0) { repository.update(distribution) }
    }
}