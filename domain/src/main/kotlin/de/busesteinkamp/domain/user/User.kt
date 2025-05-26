package de.busesteinkamp.domain.user

import de.busesteinkamp.domain.platform.SocialMediaPlatform
import java.util.*

/**
 * Aggregate Root representing a user in the content management domain.
 * This entity maintains consistency boundaries for user-related operations.
 * A user can have multiple social media platforms associated with them.
 */
class User private constructor(
    val id: UUID,
    var name: String,
    private val _platforms: MutableList<SocialMediaPlatform> = mutableListOf()
) {
    // Encapsulated collection - external access only through methods
    val platforms: List<SocialMediaPlatform> get() = _platforms.toList()
    
    init {
        require(name.isNotBlank()) { "User name cannot be blank" }
    }
    
    /**
     * Adds a platform to the user's platform list.
     * Ensures no duplicate platforms are added.
     */
    fun addPlatform(platform: SocialMediaPlatform) {
        require(!_platforms.any { it.name == platform.name }) { 
            "Platform ${platform.name} is already associated with this user" 
        }
        _platforms.add(platform)
    }
    
    /**
     * Removes a platform from the user's platform list.
     */
    fun removePlatform(platformName: String) {
        _platforms.removeIf { it.name == platformName }
    }
    
    /**
     * Updates the user's name.
     */
    fun updateName(newName: String) {
        require(newName.isNotBlank()) { "User name cannot be blank" }
        this.name = newName
    }
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is User) return false
        return id == other.id
    }
    
    override fun hashCode(): Int {
        return id.hashCode()
    }
    
    companion object {
        /**
         * Factory method to create a new user.
         */
        fun create(name: String, platforms: List<SocialMediaPlatform> = emptyList()): User {
            val user = User(
                id = UUID.randomUUID(),
                name = name
            )
            platforms.forEach { user.addPlatform(it) }
            return user
        }
        
        /**
         * Factory method to reconstruct a user from persistence.
         */
        fun reconstruct(
            id: UUID,
            name: String,
            platforms: List<SocialMediaPlatform>
        ): User {
            return User(id, name, platforms.toMutableList())
        }
    }
}