package de.busesteinkamp.plugins.logging

import de.busesteinkamp.domain.events.DomainEvent
import de.busesteinkamp.domain.events.DomainEventPublisher
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * TextFile-based implementation of DomainEventPublisher that logs all domain events
 * to text files. Each session creates a new log file with timestamp.
 * 
 * This implementation provides persistent logging of domain events for audit trails,
 * debugging, and event sourcing capabilities.
 */
class TextFileEventLogger(
    private val logDirectory: String = "logs/domain-events"
) : DomainEventPublisher {
    
    private val logFile: File
    private val sessionId: String
    
    init {
        // Create session ID with timestamp
        sessionId = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"))
        
        // Ensure log directory exists
        val logDir = File(logDirectory)
        if (!logDir.exists()) {
            logDir.mkdirs()
        }
        
        // Create log file for this session
        logFile = File(logDir, "domain-events_$sessionId.txt")
        
        // Write session header
        writeSessionHeader()
    }
    
    override fun publish(event: DomainEvent) {
        try {
            val logEntry = formatEventLogEntry(event)
            appendToLogFile(logEntry)
            
            // Also log to console for immediate feedback
            println("📝 Event logged: ${event.eventType} (${event.eventId})")
            
        } catch (e: IOException) {
            System.err.println("❌ Failed to log domain event: ${e.message}")
            // Don't throw exception to avoid breaking the application flow
        }
    }
    
    /**
     * Formats a domain event into a structured log entry
     */
    private fun formatEventLogEntry(event: DomainEvent): String {
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"))
        
        return buildString {
            appendLine("=" * 80)
            appendLine("DOMAIN EVENT LOGGED")
            appendLine("=" * 80)
            appendLine("Timestamp: $timestamp")
            appendLine("Event ID: ${event.eventId}")
            appendLine("Event Type: ${event.eventType}")
            appendLine("Occurred At: ${event.occurredAt}")
            appendLine("Actor ID: ${event.actorId}")
            appendLine("Created By: ${event.createdBy}")
            appendLine("Version: ${event.version}")
            appendLine("Event Details: $event")
            appendLine("=" * 80)
            appendLine()
        }
    }
    
    /**
     * Writes the session header to the log file
     */
    private fun writeSessionHeader() {
        try {
            val header = buildString {
                appendLine("*" * 100)
                appendLine("DOMAIN EVENTS LOG SESSION")
                appendLine("*" * 100)
                appendLine("Session ID: $sessionId")
                appendLine("Started At: ${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}")
                appendLine("Log File: ${logFile.absolutePath}")
                appendLine("*" * 100)
                appendLine()
            }
            
            FileWriter(logFile, true).use { writer ->
                writer.write(header)
                writer.flush()
            }
            
            println("📁 Domain events will be logged to: ${logFile.absolutePath}")
            
        } catch (e: IOException) {
            System.err.println("❌ Failed to write session header: ${e.message}")
        }
    }
    
    /**
     * Appends a log entry to the log file
     */
    private fun appendToLogFile(logEntry: String) {
        FileWriter(logFile, true).use { writer ->
            writer.write(logEntry)
            writer.flush()
        }
    }
    
    /**
     * Gets the current log file path
     */
    fun getLogFilePath(): String = logFile.absolutePath
    
    /**
     * Gets the current session ID
     */
    fun getSessionId(): String = sessionId
    
    /**
     * Writes a session footer when the application shuts down
     */
    fun closeSession() {
        try {
            val footer = buildString {
                appendLine("*" * 100)
                appendLine("SESSION ENDED")
                appendLine("Ended At: ${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}")
                appendLine("*" * 100)
                appendLine()
            }
            
            appendToLogFile(footer)
            println("📁 Domain events session closed: $sessionId")
            
        } catch (e: IOException) {
            System.err.println("❌ Failed to write session footer: ${e.message}")
        }
    }
}

/**
 * Extension function to repeat a string n times
 */
private operator fun String.times(n: Int): String = this.repeat(n) 