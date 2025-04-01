package de.busesteinkamp.adapters.platform

import java.util.*
import jakarta.persistence.*

@Entity
@Table(name = "platforms")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "platform_type")
abstract class PlatformDto(id: UUID?, name: String) {
    constructor() : this(null, "") {}

    @Id
    @Column(name = "id")
    val id: UUID? = id

    @Column(name = "name")
    val name: String = name
}