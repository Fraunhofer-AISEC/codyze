package de.fraunhofer.aisec.codyze.analysis

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.File
import java.io.IOException
import org.slf4j.LoggerFactory

/** Human-readable description of a finding (i.e. of an analysis result). */
class FindingDescription private constructor() {
    private var items: Map<String, FindingDescriptionItem>? = null

    class FindingDescriptionItem(
        val helpUri: String?,
        val fullDescription: Description?,
        val shortDescription: Description?,
        val passMessage: Description?,
        val fixes: List<Fix>?
    )

    class Fix(val description: Description)
    class Description(val text: String)

    fun get(onFailId: String): FindingDescriptionItem? {
        return items?.get(onFailId)
    }

    fun getDescriptionFull(onFailId: String): String? {
        return items?.get(onFailId)?.fullDescription?.text
    }

    fun getDescriptionShort(onFailId: String): String? {
        return items?.get(onFailId)?.shortDescription?.text
    }

    fun getDescriptionPass(onFailId: String): String? {
        return items?.get(onFailId)?.passMessage?.text
    }

    fun getHelpUri(onFailId: String): String? {
        return items?.get(onFailId)?.helpUri
    }

    fun getFixes(onFailId: String): List<String>? {
        return items?.get(onFailId)?.fixes?.map { it.description.text }
    }

    fun init(descriptionFile: File) {
        log.info("Parsing MARK description file from {}", descriptionFile.absolutePath)
        try {
            val mapper = jacksonObjectMapper()
            val map = mapper.readValue<Map<String, FindingDescriptionItem>>(descriptionFile)
            log.info("Loaded {} description(s)", map.size)

            items = map
        } catch (e: IOException) {
            log.warn("Failed to load findingDescription.json, explanations will be empty", e)
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(FindingDescription::class.java)

        /**
         * Singleton.
         *
         * Currently, there can be only one set of descriptions. This may change in the future when
         * multiple MARK sources are supported.
         *
         * @return
         */
        @JvmStatic val instance: FindingDescription = FindingDescription()
    }
}
