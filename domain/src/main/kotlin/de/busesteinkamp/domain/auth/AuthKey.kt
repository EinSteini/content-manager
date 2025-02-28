package de.busesteinkamp.domain.auth

import java.util.Date

data class AuthKey(
    val platformName: String, 
    val key: String, 
    val createdAt: Date, 
    val expiresAt: Date
)