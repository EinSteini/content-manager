package de.busesteinkamp.plugins.utility

import de.busesteinkamp.application.utility.BrowserOpener
import java.awt.Desktop
import java.net.URI

class DesktopBrowserOpener : BrowserOpener {
    override fun open(url: String) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(URI(url))
            } else {
                ProcessBuilder("xdg-open", url).start()  // Linux fallback
            }
        }catch (e: Exception) {
            e.printStackTrace()
        }
    }

}