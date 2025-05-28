package de.busesteinkamp.application.user

import de.busesteinkamp.domain.platform.SocialMediaPlatform
import de.busesteinkamp.domain.user.User
import de.busesteinkamp.domain.user.UserRepository

/**
 * Application Service (Use Case) for creating new users.
 * This orchestrates the creation of a user with associated platforms.
 */
class CreateNewUserUseCase(
    private val userRepository: UserRepository
) {
    /**
     * Creates a new user with the specified name and platforms.
     * 
     * @param name The user's name
     * @param platforms List of social media platforms to associate with the user
     * @return The created User entity
     */
    fun execute(name: String, platforms: List<SocialMediaPlatform>): User {
        val user = User.create(name, platforms)
        return userRepository.save(user)
    }
}