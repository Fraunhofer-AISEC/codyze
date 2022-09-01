package de.fraunhofer.aisec.codyze_core.helper

import java.io.IOException
import java.net.URL
import java.util.*
import java.util.jar.Attributes
import java.util.jar.Manifest
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/** Provides the version for Codyze modules */
class VersionProvider(private val IMPLEMENTATION_TITLE: String) {

    /** Default version to use, when no explicit version has been set. */
    private val defaultVersion: String = "0.0.0-SNAPSHOT"

    /**
     * Determine version from system property `codyze-v3-version` or from Codyzes' JAR file
     * manifests.
     *
     * Note: Using `lazy` delegate to calculate property once, when it is needed. Reduces
     * overhead by not reading in all manifests on the classpath multiple times.
     */
    val version: String by lazy {
        System.getProperty("codyze-v3-version")
            ?: run {
                val resources: Enumeration<URL> =
                    VersionProvider::class.java.classLoader.getResources("META-INF/MANIFEST.MF")
                while (resources.hasMoreElements()) {
                    val url = resources.nextElement()
                    try {
                        val manifest = Manifest(url.openStream())
                        val mainAttributes = manifest.mainAttributes
                        if (
                            IMPLEMENTATION_TITLE ==
                                mainAttributes.getValue(Attributes.Name.IMPLEMENTATION_TITLE)
                        ) {
                            return@run mainAttributes.getValue(
                                Attributes.Name.IMPLEMENTATION_VERSION
                            )
                        }
                    } catch (ex: IOException) {
                        logger.trace { "Unable to read from $url: $ex" }
                    }
                }
                defaultVersion
            }
    }

}