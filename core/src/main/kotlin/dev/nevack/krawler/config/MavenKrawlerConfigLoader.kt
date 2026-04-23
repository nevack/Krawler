package dev.nevack.krawler.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.nio.file.Path
import kotlin.io.path.readText

class MavenKrawlerConfigLoader {
    private val mapper = ObjectMapper(YAMLFactory())
        .registerKotlinModule()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)

    fun load(path: Path): MavenKrawlerConfig {
        val rootNode = mapper.readTree(path.readText()) as? ObjectNode
            ?: error("Config file must contain a YAML object")

        val normalized = rootNode.deepCopy().apply {
            put("strategy", UpdateStrategy.fromConfigValue(path("strategy").asText("latest")).name)
            set<JsonNode>("output", normalizeOutput(path("output")))
            set<JsonNode>("repositories", normalizeRepositories(path("repositories")))
        }

        return mapper.treeToValue(normalized, MavenKrawlerConfig::class.java)
    }

    private fun normalizeRepositories(node: JsonNode): ArrayNode {
        require(node is ArrayNode && node.size() > 0) { "Config must define at least one repository" }

        return mapper.createArrayNode().also { repositories ->
            node.forEach { repositoryNode ->
                require(repositoryNode is ObjectNode) { "Repository entries must be YAML objects" }
                val normalized = repositoryNode.deepCopy()
                if (!normalized.has("includeGroups")) {
                    normalized.set<JsonNode>("includeGroups", mapper.createArrayNode())
                }
                repositories.add(normalized)
            }
        }
    }

    private fun normalizeOutput(node: JsonNode): ObjectNode {
        val outputNode = (node as? ObjectNode)?.deepCopy() ?: mapper.createObjectNode()
        val normalizedTargets = when (val targetsNode = outputNode.path("targets")) {
            is ArrayNode -> normalizeOutputTargets(targetsNode)
            else -> mapper.createArrayNode().add(normalizeLegacyOutputTarget(outputNode))
        }

        return mapper.createObjectNode().apply {
            set<JsonNode>("targets", normalizedTargets)
        }
    }

    private fun normalizeOutputTargets(node: ArrayNode): ArrayNode {
        require(node.size() > 0) { "Output targets must not be empty" }

        return mapper.createArrayNode().also { targets ->
            node.forEach { targetNode ->
                require(targetNode is ObjectNode) { "Output targets must be YAML objects" }
                targets.add(normalizeLegacyOutputTarget(targetNode))
            }
        }
    }

    private fun normalizeLegacyOutputTarget(node: ObjectNode): ObjectNode = node.deepCopy().apply {
        put("format", OutputFormat.fromConfigValue(path("format").asText("table")).name)
    }
}
