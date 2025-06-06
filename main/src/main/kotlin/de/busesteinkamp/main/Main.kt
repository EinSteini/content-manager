package de.busesteinkamp.main

import de.busesteinkamp.application.process.ExecuteDistributionUseCase
import de.busesteinkamp.application.process.OpenUrlUseCase
import de.busesteinkamp.domain.auth.AuthKeyRepository
import de.busesteinkamp.domain.content.Content
import de.busesteinkamp.domain.content.ContentProvider
import de.busesteinkamp.domain.events.DomainEventPublisher
import de.busesteinkamp.domain.platform.PublishParameters
import de.busesteinkamp.domain.platform.SocialMediaPlatform
import de.busesteinkamp.domain.platform.SocialMediaPlatformRepository
import de.busesteinkamp.domain.process.Distribution
import de.busesteinkamp.domain.process.DistributionRepository
import de.busesteinkamp.domain.server.Server
import de.busesteinkamp.domain.user.User
import de.busesteinkamp.domain.user.UserRepository
import de.busesteinkamp.plugins.auth.DotenvPlugin
import de.busesteinkamp.plugins.auth.SqliteAuthKeyRepository
import de.busesteinkamp.plugins.content.ContentFileReader
import de.busesteinkamp.plugins.logging.TextFileEventLogger
import de.busesteinkamp.plugins.media.InMemorySocialMediaPlatformRepository
import de.busesteinkamp.plugins.platform.BlueskyPlatform
import de.busesteinkamp.plugins.process.InMemoryDistributionRepository
import de.busesteinkamp.plugins.server.KtorServer
import de.busesteinkamp.plugins.user.InMemoryUserRepository
import de.busesteinkamp.plugins.utility.DesktopBrowserOpener
import kotlinx.coroutines.runBlocking
import java.util.*

open class Main

fun main(args: Array<String>): Unit = runBlocking {
    // Hier die Abhängigkeiten manuell erstellen und injizieren
    val fileLogger = TextFileEventLogger()
    val eventPublisher: DomainEventPublisher = fileLogger
    val platformRepository: SocialMediaPlatformRepository = InMemorySocialMediaPlatformRepository()
    val userRepository: UserRepository = InMemoryUserRepository()
    val distributionRepository: DistributionRepository = InMemoryDistributionRepository()
    val executeDistributionUseCase = ExecuteDistributionUseCase(distributionRepository, eventPublisher)
    val server: Server = KtorServer(8443)
    val authKeyRepository: AuthKeyRepository = SqliteAuthKeyRepository()
    val openUrlUseCase = OpenUrlUseCase(true, DesktopBrowserOpener())
    val envRetriever = DotenvPlugin()

    val examplePostPath = javaClass.getResource("/example_post.txt")?.path
    if (examplePostPath == null) {
        println("Could not find example_post.txt")
        return@runBlocking
    }

    val exampleImagePath = javaClass.getResource("/helloworld.jpg")?.path
    if (exampleImagePath == null) {
        println("Could not find helloworld.jpg")
        return@runBlocking
    }

    val textProvider: ContentProvider = ContentFileReader(examplePostPath)
    val imageProvider: ContentProvider = ContentFileReader(exampleImagePath)

    val textContent: Content = textProvider.getContent()
    val imageContent: Content = imageProvider.getContent()

//    val threads: SocialMediaPlatform =
//        ThreadsPlatform(UUID.randomUUID(), "Threads", server, authKeyRepository, openUrlUseCase, envRetriever)
    val bsky: SocialMediaPlatform = BlueskyPlatform(UUID.randomUUID(), "Bluesky", envRetriever)
//    val twitter: Platform = TwitterPlatform(UUID.randomUUID(), "Twitter", server, authKeyRepository, openUrlUseCase, envRetriever)
    val mainUser: User = User.create("main", listOf(bsky))

    // Publish user creation events
    if (mainUser.hasUncommittedEvents()) {
        mainUser.getUncommittedEvents().forEach { event ->
            eventPublisher.publish(event)
        }
        mainUser.markEventsAsCommitted()
    }

    platformRepository.save(bsky)
    val publishParameters: PublishParameters =
        PublishParameters.createDefault().copy(title = "Und sogar bis zu 4 Bilder klappen!")

    val distribution = Distribution.create(
        content = textContent,
        publishParameters = publishParameters,
        platforms = mainUser.platforms
    )

    val imageDistribution = Distribution.create(
        content = imageContent,
        publishParameters = publishParameters,
        platforms = listOf(bsky)
    )

    // Add shutdown hook to properly close the file logger session
    Runtime.getRuntime().addShutdownHook(Thread {
        fileLogger.closeSession()
    })

    server.start()
    executeDistributionUseCase.execute(distribution)

    Scanner(System.`in`).nextLine()
}