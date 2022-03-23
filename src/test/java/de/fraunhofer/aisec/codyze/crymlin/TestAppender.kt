package de.fraunhofer.aisec.codyze.crymlin

import java.util.*
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.*
import org.apache.logging.log4j.core.appender.AbstractAppender
import org.apache.logging.log4j.core.config.Property
import org.apache.logging.log4j.core.config.plugins.Plugin
import org.apache.logging.log4j.core.config.plugins.PluginAttribute
import org.apache.logging.log4j.core.config.plugins.PluginElement
import org.apache.logging.log4j.core.config.plugins.PluginFactory

@Plugin(name = "TestAppender", category = Core.CATEGORY_NAME, elementType = Appender.ELEMENT_TYPE)
class TestAppender(name: String?, filter: Filter?) :
    AbstractAppender(name, filter, null, true, Property.EMPTY_ARRAY) {
    private val events = ArrayList<LogEvent>()
    override fun append(event: LogEvent) {
        synchronized(events) { events.add(event.toImmutable()) }
    }

    fun getLog(vararg levels: Level?): List<LogEvent> {
        val levelHashSet = HashSet(listOf(*levels))
        val ret: MutableList<LogEvent> = ArrayList()
        for (e in events) {
            if (levels.isEmpty() || levelHashSet.contains(e.level)) {
                ret.add(e)
            }
        }
        return ret
    }

    fun injectIntoLogger() {
        val loggerContext = LogManager.getContext(false) as LoggerContext
        val configuration = loggerContext.configuration
        val rootLoggerConfig = configuration.getLoggerConfig("")
        rootLoggerConfig.addAppender(this, Level.ALL, null)
        start()
    }

    fun reset() {
        events.clear()
    }

    companion object {
        @PluginFactory
        fun createAppender(
            @PluginAttribute("name") name: String?,
            @PluginElement("Filter") filter: Filter?
        ): TestAppender {
            return TestAppender(name, filter)
        }
    }
}
