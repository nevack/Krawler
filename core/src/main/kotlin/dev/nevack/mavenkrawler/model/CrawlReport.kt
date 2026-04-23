package dev.nevack.krawler.model

import dev.nevack.krawler.config.UpdateStrategy
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.Instant

@Serializable
data class AvailableUpdate(
    val dependency: Gav,
    val targetVersion: String,
    val repositories: List<String>,
)

@Serializable
data class CrawlReport(
    val strategy: UpdateStrategy,
    val checkedDependencies: Int,
    val updates: List<AvailableUpdate>,
    @Serializable(with = InstantIso8601Serializer::class)
    val generatedAt: Instant = Instant.now(),
)

object InstantIso8601Serializer : KSerializer<Instant> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Instant", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Instant) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): Instant = Instant.parse(decoder.decodeString())
}
