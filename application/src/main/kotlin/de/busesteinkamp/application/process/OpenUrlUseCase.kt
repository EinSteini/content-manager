package de.busesteinkamp.application.process

import de.busesteinkamp.application.utility.BrowserOpener

class OpenUrlUseCase(private val headless: Boolean, private val browserOpener: BrowserOpener? = null) {
    fun execute(url: String) {
        if (headless || browserOpener == null) {
            println("Please open the following URL in your browser: $url")
        } else {
            browserOpener.open(url)
        }
    }
}