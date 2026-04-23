package dev.nevack.krawler.config

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.annotation.JsonDeserialize

@JsonDeserialize(using = ConfigValueDeserializer::class)
data class ConfigValue(
    val value: String? = null,
    val env: String? = null,
) {
    fun resolve(): String {
        value?.let { return it }
        val variableName = env ?: error("Config value must define either 'value' or 'env'")
        return System.getenv(variableName)
            ?: error("Environment variable '$variableName' is not defined")
    }
}

class ConfigValueDeserializer : JsonDeserializer<ConfigValue>() {
    override fun deserialize(parser: JsonParser, context: DeserializationContext): ConfigValue {
        val node = parser.codec.readTree<com.fasterxml.jackson.databind.JsonNode>(parser)
        return when {
            node.isTextual -> ConfigValue(value = node.asText())
            node.isObject -> ConfigValue(
                value = node.path("value").takeIf { !it.isMissingNode && !it.isNull }?.asText(),
                env = node.path("env").takeIf { !it.isMissingNode && !it.isNull }?.asText(),
            )

            else -> context.reportInputMismatch(ConfigValue::class.java, "Expected string or object for config value")
        }
    }
}
