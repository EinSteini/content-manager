package de.busesteinkamp.plugins.user

import de.busesteinkamp.domain.user.User
import de.busesteinkamp.domain.user.UserRepository
import jakarta.persistence.EntityManager
import jakarta.persistence.EntityManagerFactory
import jakarta.persistence.Persistence
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class JpaUserRepository : UserRepository {

    private lateinit var entityManager: EntityManager
    private var entityManagerFactory: EntityManagerFactory = Persistence.createEntityManagerFactory("user-unit")

    override fun findById(id: UUID): User? {
        return entityManager.find(User::class.java, id)
    }

    init {
        entityManager = entityManagerFactory.createEntityManager()
    }

    override fun save(user: User): User {
        entityManager.persist(user)
        return user
    }

    override fun update(user: User) {
        entityManager.merge(user)
    }

    override fun delete(id: UUID) {
        val user = entityManager.find(User::class.java, id)
        if (user != null) {
            entityManager.remove(user)
        }
    }
}