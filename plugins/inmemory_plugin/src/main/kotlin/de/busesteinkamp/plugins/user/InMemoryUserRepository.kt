package de.busesteinkamp.plugins.user

import de.busesteinkamp.domain.user.User
import de.busesteinkamp.domain.user.UserRepository
import java.util.UUID

class InMemoryUserRepository : UserRepository {

    private val users: MutableList<User> = mutableListOf()

    override fun findById(id: UUID): User? {
        return users.find { it.id == id }
    }

    override fun save(user: User): User {
        val id = user.id ?: UUID.randomUUID()
        user.id = id
        users.add(user)
        return user
    }

    override fun update(user: User) {
        val index = users.indexOfFirst { it.id == user.id }
        if (index != -1) {
            users[index] = user
        }
    }

    override fun delete(id: UUID) {
        users.removeIf { it.id == id }
    }

    override fun findAll(): List<User> {
        return users.toList()
    }
}