package de.busesteinkamp.domain.process

/**
 * Value Object representing the status of content upload to a social media platform.
 * This enum encapsulates the possible states of an upload operation.
 * Value Objects are compared by their content, not identity.
 */
enum class UploadStatus {
    /** Initial state before upload starts */
    INITIAL,
    /** Upload is in progress */
    PENDING,
    /** Upload completed successfully */
    FINISHED,
    /** Upload failed */
    FAILED;
    
    /**
     * Checks if the upload is in a terminal state (finished or failed).
     */
    fun isTerminal(): Boolean {
        return this in listOf(FINISHED, FAILED)
    }
    
    /**
     * Checks if the upload was successful.
     */
    fun isSuccessful(): Boolean {
        return this == FINISHED
    }
}