package de.busesteinkamp.domain.auth

import java.util.Date

/**
 * Value Object representing authentication credentials for a social media platform.
 * This is immutable and represents a complete set of authentication information.
 * Value Objects are compared by their content, not identity.
 */
data class AuthKey(
    val platformName: String, 
    val key: String, 
    val createdAt: Date, 
    val expiresAt: Date
) {
    init {
        require(platformName.isNotBlank()) { "Platform name cannot be blank" }
        require(key.isNotBlank()) { "Authentication key cannot be blank" }
        require(expiresAt.after(createdAt)) { "Expiration date must be after creation date" }
    }
    
    /**
     * Checks if the authentication key has expired.
     */
    fun isExpired(): Boolean {
        return Date().after(expiresAt)
    }
    
    /**
     * Checks if the authentication key is still valid.
     */
    fun isValid(): Boolean {
        return !isExpired()
    }
    
    companion object {
        /**
         * Factory method to create a new AuthKey with validation.
         */
        fun create(
            platformName: String,
            key: String,
            createdAt: Date = Date(),
            expiresAt: Date
        ): AuthKey {
            return AuthKey(platformName, key, createdAt, expiresAt)
        }
    }
}