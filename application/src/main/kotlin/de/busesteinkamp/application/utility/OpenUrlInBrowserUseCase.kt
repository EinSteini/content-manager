package de.busesteinkamp.application.utility

class OpenUrlInBrowserUseCase(private val browserOpener: BrowserOpener) {
    fun execute(url: String) {
        browserOpener.open(url)
    }
}