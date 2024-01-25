package org.ecorous

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.TimeZone
import java.util.UUID

object Utils {
    object UUIDSerializer : KSerializer<UUID> {
        private fun serialize(uuid: UUID): String {
            return uuid.toString()
        }
        private fun deserialize(uuid: String): UUID {
            return UUID.fromString(uuid)
        }

        override val descriptor: SerialDescriptor
            get() = PrimitiveSerialDescriptor("UUID", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): UUID {
            return deserialize(decoder.decodeString())
        }

        override fun serialize(encoder: Encoder, value: UUID) {
            encoder.encodeString(serialize(value))
        }
    }
    suspend fun PipelineContext<Unit, ApplicationCall>.ensureAuthenticated() {
        val authenticated = call.request.headers["Authorization"] == authToken
        if (!authenticated) {
            // try use form parameters
            val form = call.receiveParameters()
            if (form["authToken"] != authToken) {
                call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Not authenticated."))
            }
        }
    }
    fun currentTime(): LocalDateTime {
        return Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    }

    fun uuidOrNull(uuid: String): UUID? {
        return try {
            UUID.fromString(uuid)
        } catch (e: IllegalArgumentException) {
            null
        }
    }
}