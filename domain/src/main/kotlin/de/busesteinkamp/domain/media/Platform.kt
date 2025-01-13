package de.busesteinkamp.domain.media

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import java.util.UUID

@Entity
class Platform (
    @Id
    @GeneratedValue
    var id: UUID? = null,
    var name: String,
    var apiUrl: String
) {
    constructor() : this(null, "", "")
}