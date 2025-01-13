package de.busesteinkamp.domain.user

import java.util.UUID

interface UserRepository {
    fun findById(id: UUID): User?
    fun save(user: User): User
    fun update(user: User)
    fun delete(id: UUID)
}