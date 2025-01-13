package de.busesteinkamp.domain.user

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import java.util.*

@Entity
class User(
    @Id
    @GeneratedValue
    var id: UUID? = null,
    var name: String
) {
    constructor() : this(null, "")
}