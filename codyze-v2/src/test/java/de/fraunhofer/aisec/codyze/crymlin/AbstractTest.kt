package de.fraunhofer.aisec.codyze.crymlin

import de.fraunhofer.aisec.codyze.config.CodyzeConfiguration
import de.fraunhofer.aisec.codyze.config.Configuration
import de.fraunhofer.aisec.codyze.config.CpgConfiguration
import de.fraunhofer.aisec.cpg.TranslationConfiguration
import de.fraunhofer.aisec.cpg.TranslationManager
import de.fraunhofer.aisec.cpg.passes.EdgeCachePass
import de.fraunhofer.aisec.cpg.passes.IdentifierPass
import java.io.File

abstract class AbstractTest {
    companion object {
        /**
         * Helper method for initializing an Analysis Run.
         *
         * @param sourceLocations
         * @return TranslationManager
         */
        @JvmStatic
        protected fun newAnalysisRun(vararg sourceLocations: File): TranslationManager {
            return TranslationManager.builder()
                .config(
                    TranslationConfiguration.builder()
                        .debugParser(true)
                        .failOnError(false)
                        .defaultPasses()
                        .registerPass(IdentifierPass())
                        .registerPass(EdgeCachePass())
                        .defaultLanguages()
                        .sourceLocations(*sourceLocations)
                        .build()
                )
                .build()
        }

        /**
         * Helper method for initializing an Analysis Run. Returned Configuration can be used to
         * initialize AnalysisServer and start the analysis with {@link
         * de.fraunhofer.aisec.codyze.analysis.AnalysisServer#analyze(String url)}.
         *
         * @param codyzeConfig
         * @return Configuration
         */
        @JvmStatic
        protected fun newAnalysisRun(codyzeConfig: CodyzeConfiguration): Configuration {
            val cpgConfig = CpgConfiguration()
            cpgConfig.debugParser = true
            cpgConfig.failOnError = false
            cpgConfig.defaultPasses = true
            cpgConfig.passes = listOf(IdentifierPass(), EdgeCachePass())
            return Configuration(codyzeConfig, cpgConfig)
        }
    }
}
