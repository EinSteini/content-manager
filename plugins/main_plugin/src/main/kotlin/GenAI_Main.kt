package de.busesteinkamp

import de.busesteinkamp.application.generate.GenerateTextContentUseCase
import de.busesteinkamp.adapters.generate.TextPostGenerator
import de.busesteinkamp.application.process.ExecuteDistributionUseCase
import de.busesteinkamp.application.process.OpenUrlUseCase
import de.busesteinkamp.domain.auth.AuthKeyRepository
import de.busesteinkamp.domain.generator.GenAIService
import de.busesteinkamp.domain.generator.Generator
import de.busesteinkamp.domain.platform.Platform
import de.busesteinkamp.domain.platform.PlatformRepository
import de.busesteinkamp.domain.platform.PublishParameters
import de.busesteinkamp.domain.process.Distribution
import de.busesteinkamp.domain.process.DistributionRepository
import de.busesteinkamp.domain.server.Server
import de.busesteinkamp.domain.user.User
import de.busesteinkamp.plugins.auth.DotenvPlugin
import de.busesteinkamp.plugins.auth.SqliteAuthKeyRepository
import de.busesteinkamp.plugins.client.GeminiClient
import de.busesteinkamp.plugins.media.InMemoryPlatformRepository
import de.busesteinkamp.adapters.content.TxtContent
import de.busesteinkamp.plugins.platform.ThreadsPlatform
import de.busesteinkamp.plugins.process.InMemoryDistributionRepository
import de.busesteinkamp.plugins.server.KtorServer
import de.busesteinkamp.plugins.utility.DesktopBrowserOpener
import kotlinx.coroutines.runBlocking
import java.util.*


fun main(): Unit = runBlocking {
    val platformRepository: PlatformRepository = InMemoryPlatformRepository()
    val distributionRepository: DistributionRepository = InMemoryDistributionRepository()

    val executeDistributionUseCase = ExecuteDistributionUseCase(distributionRepository)

    val server: Server = KtorServer(8443)
    val authKeyRepository: AuthKeyRepository = SqliteAuthKeyRepository()

    val genAIService: GenAIService = GeminiClient()
    val textPostGenerator: Generator = TextPostGenerator(genAIService = genAIService)
    val generateTextContentUseCase = GenerateTextContentUseCase(textPostGenerator)

    val openUrlUseCase = OpenUrlUseCase(false, DesktopBrowserOpener())
    val dotenv = DotenvPlugin()

    val textContent = generateTextContentUseCase.execute(
        input = "Programmierung"
    )

    val content: TxtContent = TxtContent(content = textContent.get().toString())
    println(content.get())

    val threads: Platform = ThreadsPlatform(UUID.randomUUID(), "Threads", server, authKeyRepository, openUrlUseCase, dotenv)
    val mainUser = User(UUID.randomUUID(), "main", listOf(threads))
    platformRepository.save(threads)
    val publishParameters = PublishParameters()
    publishParameters.title = "New Post"

    val distribution = Distribution(
        content = content,
        publishParameters = publishParameters,
        platforms = mainUser.platforms
    )

    server.start()
    executeDistributionUseCase.execute(distribution)

    Scanner(System.`in`).nextLine()
}