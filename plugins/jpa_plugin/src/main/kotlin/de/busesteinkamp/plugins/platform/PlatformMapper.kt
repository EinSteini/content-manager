package de.busesteinkamp.plugins.platform

import de.busesteinkamp.adapters.platform.PlatformDto
import de.busesteinkamp.application.process.OpenUrlUseCase
import de.busesteinkamp.domain.auth.AuthKeyRepository
import de.busesteinkamp.domain.auth.EnvRetriever
import de.busesteinkamp.domain.platform.Platform
import de.busesteinkamp.domain.server.Server

class PlatformMapper(
    private val server: Server,
    private val authKeyRepository: AuthKeyRepository,
    private val openUrlUseCase: OpenUrlUseCase,
    private val envRetriever: EnvRetriever
) {
    fun toDomain(dto: PlatformDto): Platform {
        return when (dto) {
            is ThreadsPlatformDto -> ThreadsPlatform(
                id = dto.id,
                name = dto.name,
                server = server,
                authKeyRepository = authKeyRepository,
                openUrlUseCase = openUrlUseCase,
                envRetriever = envRetriever
            )
            is BlueskyPlatformDto -> BlueskyPlatform(
                id = dto.id,
                name = dto.name,
                envRetriever = envRetriever
            )
            is TwitterPlatformDto -> TwitterPlatform(
                id = dto.id,
                name = dto.name,
                envRetriever = envRetriever,
                openUrlUseCase = openUrlUseCase,
                server = server,
                authKeyRepository = authKeyRepository
            )
            else -> throw IllegalArgumentException("Unknown platform type")
        }
    }

    fun toDto(domain: Platform): PlatformDto {
        return when (domain) {
            is ThreadsPlatform -> ThreadsPlatformDto(
                id = domain.id,
                name = domain.name
            )
            is BlueskyPlatform -> BlueskyPlatformDto(
                id = domain.id,
                name = domain.name
            )
            is TwitterPlatform -> TwitterPlatformDto(
                id = domain.id,
                name = domain.name
            )
            else -> throw IllegalArgumentException("Unknown platform type")
        }
    }
}