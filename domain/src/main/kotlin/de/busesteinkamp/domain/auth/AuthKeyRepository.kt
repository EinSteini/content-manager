package de.busesteinkamp.domain.auth

interface AuthKeyRepository {
    fun find(platformName: String): AuthKey?
    fun save(authKey: AuthKey): AuthKey
    fun update(authKey: AuthKey): AuthKey
    fun delete(platformName: String)
}