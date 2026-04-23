package dev.nevack.krawler.maven

import org.w3c.dom.Element
import org.xml.sax.InputSource
import java.io.StringReader
import javax.xml.XMLConstants
import javax.xml.parsers.DocumentBuilderFactory

class MavenMetadataParser {
    fun parse(xml: String): MavenMetadata {
        val documentBuilderFactory = DocumentBuilderFactory.newInstance().apply {
            isNamespaceAware = true
            setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true)
            setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
            setFeature("http://xml.org/sax/features/external-general-entities", false)
            setFeature("http://xml.org/sax/features/external-parameter-entities", false)
            isExpandEntityReferences = false
            setXIncludeAware(false)
        }

        val document = documentBuilderFactory.newDocumentBuilder().parse(InputSource(StringReader(xml)))
        val root = document.documentElement ?: return MavenMetadata()
        val versioning = root.child("versioning") ?: return MavenMetadata()
        val versions = versioning.child("versions")
            ?.children("version")
            ?.map { it.textContent.trim() }
            .orEmpty()
            .filter { it.isNotEmpty() }

        return MavenMetadata(
            versions = versions,
            latest = versioning.child("latest")?.textContent?.trim()?.ifEmpty { null },
            release = versioning.child("release")?.textContent?.trim()?.ifEmpty { null },
        )
    }

    private fun Element.child(name: String): Element? = children(name).firstOrNull()

    private fun Element.children(name: String): List<Element> = buildList {
        val nodes = childNodes
        for (index in 0 until nodes.length) {
            val child = nodes.item(index)
            if (child is Element && child.localNameOrNodeName() == name) {
                add(child)
            }
        }
    }

    private fun Element.localNameOrNodeName(): String = localName ?: nodeName.substringAfter(':')
}
