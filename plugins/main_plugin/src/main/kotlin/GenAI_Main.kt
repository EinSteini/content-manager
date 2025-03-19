package de.busesteinkamp

import de.busesteinkamp.application.generate.GenerateTextPostUseCase
import de.busesteinkamp.application.generate.TextPostGenerator
import de.busesteinkamp.application.process.ExecuteDistributionUseCase
import de.busesteinkamp.application.process.OpenUrlUseCase
import de.busesteinkamp.domain.auth.AuthKeyRepository
import de.busesteinkamp.domain.generator.GenAIService
import de.busesteinkamp.domain.generator.Generator
import de.busesteinkamp.domain.media.MediaFile
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
import de.busesteinkamp.plugins.media.TxtFile
import de.busesteinkamp.plugins.platform.ThreadsPlatform
import de.busesteinkamp.plugins.process.InMemoryDistributionRepository
import de.busesteinkamp.plugins.server.KtorServer
import de.busesteinkamp.plugins.utility.DesktopBrowserOpener
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.IOException
import java.util.*


fun main(): Unit = runBlocking {
    val temporaryPostFile = File(System.getProperty("user.dir"), "GenAI_Temp.txt")
    if (!temporaryPostFile.exists()) {
        temporaryPostFile.parentFile.mkdirs()
        temporaryPostFile.createNewFile()
    }

    val platformRepository: PlatformRepository = InMemoryPlatformRepository()
    val distributionRepository: DistributionRepository = InMemoryDistributionRepository()

    val executeDistributionUseCase = ExecuteDistributionUseCase(distributionRepository)

    val server: Server = KtorServer(8443)
    val authKeyRepository: AuthKeyRepository = SqliteAuthKeyRepository()

    val genAIService: GenAIService = GeminiClient()
    val textPostGenerator: Generator = TextPostGenerator(genAIService = genAIService)
    val generateTextPostUseCase = GenerateTextPostUseCase(textPostGenerator)

    val openUrlUseCase = OpenUrlUseCase(false, DesktopBrowserOpener())
    val dotenv = DotenvPlugin()

    val textContent = generateTextPostUseCase.execute(
        input = "Programmierung"
    )

    try {
        temporaryPostFile.writeText(textContent)
        println("Write to file successful: $textContent")
    } catch (e: IOException) {
        println("Error writing to file: ${e.message}")
        return@runBlocking
    }

    val mediaFile: MediaFile = TxtFile(
        filename = temporaryPostFile.path,
        fileSize = 1234,
        id = UUID.randomUUID()
    )
    println(mediaFile.toString())

    val threads: Platform = ThreadsPlatform(UUID.randomUUID(), "Threads", server, authKeyRepository, openUrlUseCase, dotenv)
    val mainUser = User(UUID.randomUUID(), "main", listOf(threads))
    platformRepository.save(threads)
    val publishParameters = PublishParameters()
    publishParameters.title = "New Post"

    val distribution = Distribution(
        mediaFile = mediaFile,
        publishParameters = publishParameters,
        platforms = mainUser.platforms
    )

    server.start()
    executeDistributionUseCase.execute(distribution)

    Scanner(System.`in`).nextLine()
}