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
import de.busesteinkamp.domain.user.UserRepository
import de.busesteinkamp.plugins.auth.DotenvPlugin
import de.busesteinkamp.plugins.auth.SqliteAuthKeyRepository
import de.busesteinkamp.plugins.client.GeminiClient
import de.busesteinkamp.plugins.content.ContentFileReader
import de.busesteinkamp.adapters.content.TxtContent
import de.busesteinkamp.application.utility.BrowserOpener
import de.busesteinkamp.plugins.media.*
import de.busesteinkamp.plugins.platform.BlueskyPlatform
import de.busesteinkamp.plugins.platform.ThreadsPlatform
import de.busesteinkamp.plugins.platform.TwitterPlatform
import de.busesteinkamp.plugins.process.InMemoryDistributionRepository
import de.busesteinkamp.plugins.server.KtorServer
import de.busesteinkamp.plugins.user.InMemoryUserRepository
import de.busesteinkamp.plugins.utility.DesktopBrowserOpener
import kotlinx.coroutines.*
import java.io.File
import java.util.*

/**
 * ANSI escape codes for terminal colors and styles.
 */
object TerminalColors {
    const val RESET = "\u001B[0m"
    const val GREEN = "\u001B[32m"
    const val YELLOW = "\u001B[33m"
    const val BLUE = "\u001B[34m"
    const val RED = "\u001B[31m"
    const val BOLD = "\u001B[1m"
    const val CYAN = "\u001B[36m"
    const val PURPLE = "\u001B[35m"
}

/**
 * Main class for the Terminal Content Manager Application.
 * This class handles user interaction and manages the posting of content to various platforms.
 */
class TerminalMain {
    private val platformRepository: PlatformRepository = InMemoryPlatformRepository()
    private val userRepository: UserRepository = InMemoryUserRepository()
    private val distributionRepository: DistributionRepository = InMemoryDistributionRepository()
    private val executeDistributionUseCase = ExecuteDistributionUseCase(distributionRepository)
    private val server: Server = KtorServer(8443)
    private val authKeyRepository: AuthKeyRepository = SqliteAuthKeyRepository()
    private val openUrlUseCase = OpenUrlUseCase(false, DesktopBrowserOpener())
    private val genAIService: GenAIService = GeminiClient()
    private val textPostGenerator: Generator = TextPostGenerator(genAIService = genAIService)
    private val generateTextContentUseCase = GenerateTextContentUseCase(textPostGenerator)
    private val envRetriever = DotenvPlugin()

    // List of available platform factories for creating platform instances
    private val availablePlatformFactories = listOf(
        "Twitter" to { id: UUID -> TwitterPlatform(id, "Twitter", server, authKeyRepository, openUrlUseCase, envRetriever) },
        "Bluesky" to { id: UUID -> BlueskyPlatform(id, "Bluesky", envRetriever) },
        "Threads" to { id: UUID -> ThreadsPlatform(id, "Threads", server, authKeyRepository, openUrlUseCase, envRetriever) }
    )

    // Currently selected user preset
    private var currentUser: User? = null
    private val rickRollUrl = "https://shattereddisk.github.io/rickroll/rickroll.mp4"

    /**
     * Displays a fancy welcome message with ASCII art.
     */
    private fun displayWelcome() {
        println(
            """
            ${TerminalColors.CYAN}
               ______            __             __      
              / ____/___  ____  / /____  ____  / /_     
             / /   / __ \/ __ \/ __/ _ \/ __ \/ __/     
            / /___/ /_/ / / / / /_/  __/ / / / /_       
            \_____\____/_/ /_/\__/\___/_/ /_/\__/       
               /  |/  /___ _____  ____ _____ ____  _____
              / /|_/ / __ `/ __ \/ __ `/ __ `/ _ \/ ___/
             / /  / / /_/ / / / / /_/ / /_/ /  __/ /    
            /_/  /_/\__,_/_/ /_/\__,_/\__, /\___/_/     
                                     /____/           
            ${TerminalColors.RESET}     
            ${TerminalColors.PURPLE}Welcome to your Social Media Content Manager!${TerminalColors.RESET}
            ==================================================
        """.trimIndent()
        )
    }

    /**
     * Validates if a file path exists and is accessible.
     */
    private fun validatePath(path: String): Boolean {
        return try {
            val file = File(path)
            file.exists() && file.canRead()
        } catch (e: SecurityException) {
            false
        }
    }

    /**
     * Shows a progress indicator while executing a task.
     */
    private suspend fun showProgress(message: String, task: suspend () -> Unit) {
        val progressChars = listOf("⣾", "⣽", "⣻", "⢿", "⡿", "⣟", "⣯", "⣷")
        var index = 0
        var isRunning = true

        // Start progress animation in a coroutine
        val progressJob = CoroutineScope(Dispatchers.IO).launch {
            while (isRunning) {
                print("\r$message ${progressChars[index]} ")
                index = (index + 1) % progressChars.size
                delay(100)
            }
        }

        try {
            task()
        } finally {
            isRunning = false
            progressJob.cancel()
            println("\r$message ✓") // Show completion
        }
    }

    /**
     * Starts the terminal application and handles user interaction.
     */
    fun start() = runBlocking {
        server.start()
        displayWelcome()

        while (true) {
            println("\nWhat would you like to do?")
            println("${TerminalColors.GREEN}1. Select/Create User Preset${TerminalColors.RESET}")
            println("${TerminalColors.GREEN}2. Post Content ${currentUser?.let { "(${it.name})" } ?: ""}${TerminalColors.RESET}")
            println("${TerminalColors.GREEN}3. Exit${TerminalColors.RESET}")
            print("${TerminalColors.YELLOW}Please select an option (1-3): ${TerminalColors.RESET}")

            val input = readlnOrNull()?.trim() ?: continue

            when (input.toIntOrNull()) {
                1 -> handleUserPreset()
                2 -> handlePostContent()
                3 -> {
                    println("${TerminalColors.GREEN}Goodbye!${TerminalColors.RESET}")
                    return@runBlocking
                }
                // For those who try to read, even on the first step...
                4 -> {
                    println("${TerminalColors.GREEN}You can´t read your choices...${TerminalColors.RESET}")
                    println("${TerminalColors.GREEN}Now you will be rick rolled...${TerminalColors.RESET}")
                    openUrlUseCase.execute(rickRollUrl)
                }

                else -> println("${TerminalColors.RED}Invalid option. Please try again.${TerminalColors.RESET}")
            }
        }
    }

    /**
     * Manages user preset selection and creation.
     */
    private suspend fun handleUserPreset() {
        println("\nCurrent User: ${currentUser?.name ?: "No preset selected"}")
        println("==================================================")
        println("\nUser Preset Management")
        println("${TerminalColors.GREEN}1. Select existing preset${TerminalColors.RESET}")
        println("${TerminalColors.GREEN}2. Create new preset${TerminalColors.RESET}")
        println("${TerminalColors.GREEN}3. Back to main menu${TerminalColors.RESET}")
        print("${TerminalColors.YELLOW}Please select an option (1-3): ${TerminalColors.RESET}")

        when (readlnOrNull()?.toIntOrNull()) {
            1 -> selectExistingPreset()
            2 -> createNewPreset()
            3 -> return
            else -> println("${TerminalColors.RED}Invalid option. Please try again.${TerminalColors.RESET}")
        }
    }

    /**
     * Allows the user to select an existing preset.
     */
    private suspend fun selectExistingPreset() {
        val users = userRepository.findAll()
        if (users.isEmpty()) {
            println("${TerminalColors.RED}No presets found. Please create a new preset first.${TerminalColors.RESET}")
            return
        }

        println("\nAvailable presets:")
        users.forEachIndexed { index: Int, user: User ->
            println("${index + 1}. ${user.name}")
        }

        print("${TerminalColors.YELLOW}Select a preset (1-${users.size}): ${TerminalColors.RESET}")
        val selection = readlnOrNull()?.toIntOrNull()

        if (selection != null && selection in 1..users.size) {
            currentUser = users[selection - 1]
            println("${TerminalColors.GREEN}Selected preset: ${currentUser?.name}${TerminalColors.RESET}")
        } else {
            println("${TerminalColors.RED}Invalid selection.${TerminalColors.RESET}")
        }
    }

    /**
     * Creates a new user preset with selected platforms.
     */
    private suspend fun createNewPreset() {
        print("\nEnter preset name: ")
        val name = readlnOrNull() ?: return

        val platforms = selectPlatforms()
        if (platforms.isEmpty()) {
            println("${TerminalColors.RED}No platforms selected. Preset creation cancelled.${TerminalColors.RESET}")
            return
        }

        val user = User(name = name, platforms = platforms)
        currentUser = userRepository.save(user)
        println("${TerminalColors.GREEN}Created new preset: ${user.name}${TerminalColors.RESET}")
    }

    /**
     * Handles the content posting process.
     */
    private suspend fun handlePostContent() {
        println("\nWhat would you like to post? ${currentUser?.let { "(${it.name})" } ?: ""}")
        println("${TerminalColors.GREEN}1. GenAI Generated Text${TerminalColors.RESET}")
        println("${TerminalColors.GREEN}2. Plain Text${TerminalColors.RESET}")
        println("${TerminalColors.GREEN}3. Text File${TerminalColors.RESET}")
        println("${TerminalColors.GREEN}4. Image with Text${TerminalColors.RESET}")
        println("${TerminalColors.GREEN}5. Multiple Images with Text${TerminalColors.RESET}")
        println("${TerminalColors.GREEN}6. Back to main menu${TerminalColors.RESET}")
        print("${TerminalColors.YELLOW}Please select an option (1-7): ${TerminalColors.RESET}")

        when (readlnOrNull()?.toIntOrNull()) {
            1 -> handleGenAIText()
            2 -> handlePlainText()
            3 -> handleTextFile()
            4 -> handleImageWithText()
            5 -> handleMultipleImagesWithText()
            6 -> return
            else -> println("${TerminalColors.RED}Invalid option. Please try again.${TerminalColors.RESET}")
        }
    }

    /**
     * Prompts the user to select platforms for posting.
     * @return List of selected platforms.
     */
    private fun selectPlatforms(): List<Platform> {
        println("\nSelect platforms to post to (comma-separated numbers):")
        availablePlatformFactories.forEachIndexed { index, (name, _) ->
            println("${index + 1}. $name")
        }

        val selections =
            readlnOrNull()?.split(",")?.map { it.trim().toIntOrNull() }?.filterNotNull() ?: return emptyList()
        return selections.mapNotNull { index ->
            availablePlatformFactories.getOrNull(index - 1)?.let { (_, factory) ->
                factory(UUID.randomUUID())
            }
        }
    }

    /**
     * Handles the generation of text posts using GenAI.
     */
    private suspend fun handleGenAIText() {
        println("\nEnter your topic for GenAI post generation:")
        val prompt = readlnOrNull() ?: return

        val platforms = if (currentUser != null) {
            currentUser!!.platforms
        } else {
            selectPlatforms()
        }
        if (platforms.isEmpty()) return

        val textContent = generateTextContentUseCase.execute(
            input = prompt
        )

        val publishParams = PublishParameters()
        publishParams.title = textContent.get().toString()

        val distribution = Distribution(
            content = TxtContent(UUID.randomUUID(), textContent.get().toString()),
            publishParameters = publishParams,
            platforms = platforms
        )

        executeAndShowResults(distribution)
    }

    /**
     * Handles posting of plain text content.
     */
    private suspend fun handlePlainText() {
        println("\nEnter your text:")
        val text = readlnOrNull() ?: return

        val platforms = if (currentUser != null) {
            currentUser!!.platforms
        } else {
            selectPlatforms()
        }
        if (platforms.isEmpty()) return

        val publishParams = PublishParameters()
        publishParams.title = text

        val distribution = Distribution(
            content = TxtContent(UUID.randomUUID(), text),
            publishParameters = publishParams,
            platforms = platforms
        )

        executeAndShowResults(distribution)
    }

    /**
     * Handles posting of content from a text file.
     */
    private suspend fun handleTextFile() {
        println("\nEnter the path to your text file:")
        val filePath = readlnOrNull() ?: return

        if (!validatePath(filePath)) {
            println("${TerminalColors.RED}Error: File does not exist or is not accessible.${TerminalColors.RESET}")
            return
        }

        val platforms = if (currentUser != null) {
            currentUser!!.platforms
        } else {
            selectPlatforms()
        }
        if (platforms.isEmpty()) return

        val publishParams = PublishParameters()
        publishParams.title = "Text from file"

        val distribution = Distribution(
            content = TxtContent(UUID.randomUUID(), filePath),
            publishParameters = publishParams,
            platforms = platforms
        )

        executeAndShowResults(distribution)
    }

    /**
     * Handles posting of an image with accompanying text.
     */
    private suspend fun handleImageWithText() {
        println("\nEnter the path to your image:")
        val imagePath = readlnOrNull() ?: return

        println("Enter alt text for the image:")
        val altText = readlnOrNull() ?: return

        println("Enter your text:")
        val text = readlnOrNull() ?: return

        val platforms = if (currentUser != null) {
            currentUser!!.platforms
        } else {
            selectPlatforms()
        }
        if (platforms.isEmpty()) return

        val publishParams = PublishParameters()
        publishParams.title = text

        val imageContent = ContentFileReader(imagePath).getContent()

        val distribution = Distribution(
            content = imageContent,
            publishParameters = publishParams,
            platforms = platforms
        )

        executeAndShowResults(distribution)
    }

    /**
     * Handles posting of multiple images with accompanying text.
     */
    private suspend fun handleMultipleImagesWithText() {
        println("\n${TerminalColors.RED}Error: This feature is currently disabled.${TerminalColors.RESET}")

//        println("\nEnter the paths to your images (comma-separated):")
//        val imagePaths = readlnOrNull()?.split(",")?.map { it.trim() } ?: return
//
//        println("Enter alt texts for the images (comma-separated):")
//        val altTexts = readlnOrNull()?.split(",")?.map { it.trim() } ?: return
//
//        if (imagePaths.size != altTexts.size) {
//            println("${TerminalColors.RED}Error: Number of images and alt texts must match!${TerminalColors.RESET}")
//            return
//        }
//
//        println("Enter your text:")
//        val text = readlnOrNull() ?: return
//
//        val platforms = if (currentUser != null) {
//            currentUser!!.platforms
//        } else {
//            selectPlatforms()
//        }
//        if (platforms.isEmpty()) return
//
//        val publishParams = PublishParameters()
//        publishParams.title = text
//
//        val distribution = Distribution(
//            mediaFile = MultipleImageFiles(UUID.randomUUID(), imagePaths, altTexts),
//            publishParameters = publishParams,
//            platforms = platforms
//        )
//
//        executeAndShowResults(distribution)
    }

    /**
     * Executes the distribution of the content to the selected platforms and shows the results.
     * @param distribution The distribution object containing media and platforms.
     */
    private suspend fun executeAndShowResults(distribution: Distribution) {
        try {
            showProgress("Distributing content to platforms...") {
                executeDistributionUseCase.execute(distribution)
            }

            println("\n${TerminalColors.GREEN}Distribution completed successfully!${TerminalColors.RESET}")
            println("Posted to platforms:")
            distribution.platforms.forEach { platform ->
                println("${TerminalColors.BLUE}- ${platform.name}${TerminalColors.RESET}")
            }
        } catch (e: Exception) {
            println("\n${TerminalColors.RED}Error during distribution: ${e.message}${TerminalColors.RESET}")
        }
        println("\nPress Enter to continue...")
        readlnOrNull()
    }
}

/**
 * Main function to start the TerminalMain application.
 */
fun main(args: Array<String>) {
    TerminalMain().start()
} 