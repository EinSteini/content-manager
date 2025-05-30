package de.busesteinkamp.main

import de.busesteinkamp.adapters.content.TextContent
import de.busesteinkamp.adapters.generate.TextPostGenerator
import de.busesteinkamp.application.generate.GenerateTextContentUseCase
import de.busesteinkamp.application.process.ExecuteDistributionUseCase
import de.busesteinkamp.application.process.OpenUrlUseCase
import de.busesteinkamp.domain.auth.AuthKeyRepository
import de.busesteinkamp.domain.events.DomainEventPublisher
import de.busesteinkamp.domain.generator.GenAIService
import de.busesteinkamp.domain.generator.Generator
import de.busesteinkamp.domain.platform.PublishParameters
import de.busesteinkamp.domain.platform.SocialMediaPlatform
import de.busesteinkamp.domain.platform.SocialMediaPlatformRepository
import de.busesteinkamp.domain.process.Distribution
import de.busesteinkamp.domain.process.DistributionRepository
import de.busesteinkamp.domain.server.Server
import de.busesteinkamp.domain.user.User
import de.busesteinkamp.plugins.auth.DotenvPlugin
import de.busesteinkamp.plugins.auth.SqliteAuthKeyRepository
import de.busesteinkamp.plugins.client.GeminiClient
import de.busesteinkamp.plugins.logging.TextFileEventLogger
import de.busesteinkamp.plugins.media.InMemorySocialMediaPlatformRepository
import de.busesteinkamp.plugins.platform.ThreadsPlatform
import de.busesteinkamp.plugins.process.InMemoryDistributionRepository
import de.busesteinkamp.plugins.server.KtorServer
import de.busesteinkamp.plugins.utility.DesktopBrowserOpener
import kotlinx.coroutines.runBlocking
import java.util.*


fun main(): Unit = runBlocking {
    val fileLogger = TextFileEventLogger()
    val eventPublisher: DomainEventPublisher = fileLogger
    val platformRepository: SocialMediaPlatformRepository = InMemorySocialMediaPlatformRepository()
    val distributionRepository: DistributionRepository = InMemoryDistributionRepository()

    val executeDistributionUseCase = ExecuteDistributionUseCase(distributionRepository, eventPublisher)

    val server: Server = KtorServer(8443)
    val authKeyRepository: AuthKeyRepository = SqliteAuthKeyRepository()

    val envRetriever = DotenvPlugin()
    val genAIService: GenAIService = GeminiClient(envRetriever)
    val textPostGenerator: Generator = TextPostGenerator(genAIService = genAIService)
    val generateTextContentUseCase = GenerateTextContentUseCase(textPostGenerator)

    val openUrlUseCase = OpenUrlUseCase(false, DesktopBrowserOpener())

    val textContent = generateTextContentUseCase.execute(
        input = "Programmierung"
    )

    val content: TextContent = TextContent(content = textContent.get().toString())
    println(content.get())

    val threads: SocialMediaPlatform =
        ThreadsPlatform(UUID.randomUUID(), "Threads", server, authKeyRepository, openUrlUseCase, envRetriever)
    val mainUser = User.create("main", listOf(threads))

    // Publish user creation events
    if (mainUser.hasUncommittedEvents()) {
        mainUser.getUncommittedEvents().forEach { event ->
            eventPublisher.publish(event)
        }
        mainUser.markEventsAsCommitted()
    }
    platformRepository.save(threads)
    val publishParameters = PublishParameters.createDefault().copy(
        title = "New Post"
    )

    val distribution = Distribution.create(
        content = content,
        publishParameters = publishParameters,
        platforms = mainUser.platforms
    )

    // Add shutdown hook to properly close the file logger session
    Runtime.getRuntime().addShutdownHook(Thread {
        fileLogger.closeSession()
    })

    server.start()
    executeDistributionUseCase.execute(distribution)

    Scanner(System.`in`).nextLine()
}