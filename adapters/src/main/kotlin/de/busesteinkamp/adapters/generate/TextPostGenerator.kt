package de.busesteinkamp.adapters.generate

import de.busesteinkamp.adapters.content.TextContent
import de.busesteinkamp.domain.generator.GenAIService
import de.busesteinkamp.domain.generator.Generator

class TextPostGenerator(private val genAIService: GenAIService) : Generator {

    private var text = ""

    override suspend fun generateText(input: String, parameters: Map<String, String>) {
        val systemPrompt = """
            Du bist ein KI-Modell, das darauf spezialisiert ist, humorvolle und kurze Texte für soziale Medien wie Threads, X (Twitter) oder Bluesky zu generieren.

            Der Nutzer gibt dir ein Thema vor (z.B. "Programmierung").
            
            Deine Aufgabe ist es, genau einen kurzen, lustigen Text für einen Social-Media-Post zu generieren. Der soll ein wenig ketzerisch sein und eine gewisse Nische etwas triggern.
            
            Der Text muss:
            * zwischen 100-150 Zeichen beinhalten
            * humorvoll und unterhaltsam sein
            * prägnant sein (ideal für schnelle Social-Media-Konsum)
            * keine direkten Beleidigungen beinhalten
            * relevante Hashtags am Ende des Textes enthalten, die zum Thema passen.
            * Keine Anführungszeichen, eckigen Klammern, Zeilenumbrüche oder zusätzlichen Formatierungen enthalten. Nur den reinen Text selbst liefern.
            * Es gibt keine zusätzlichen Erklärungen oder einleitende Sätze, nur den Text inklusive Hashtags.
            
            Generiere immer nur genau einen Text. Antworte ausschließlich mit dem generierten Text inklusive Hashtags.
            """.trimIndent()

        val userInput = """
            Generiere mir einen Threads Post zum Thema $input.
        """.trimIndent()

        text = genAIService.sendMessage(systemPrompt, userInput)
    }

    override fun getContent(): TextContent {
        return TextContent(content = text)
    }
}