package org.ecorous.types

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import org.ecorous.Utils
import java.util.UUID

@Serializable
data class Post(
    @Serializable(with = Utils.UUIDSerializer::class)
    val id: UUID,
    val title: String,
    val content: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

@Serializable
data class CreatePost(
    val title: String,
    val content: String
)