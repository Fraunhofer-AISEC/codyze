
package de.fraunhofer.aisec.analysis.utils;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.*;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

@Plugin(name = "TestAppender", category = Core.CATEGORY_NAME, elementType = Appender.ELEMENT_TYPE)
public class TestAppender extends AbstractAppender {

	private final ArrayList<LogEvent> events = new ArrayList<>();

	public TestAppender(String name, Filter filter) {
		super(name, filter, null);
	}

	@PluginFactory
	public static TestAppender createAppender(
			@PluginAttribute("name") String name, @PluginElement("Filter") Filter filter) {
		return new TestAppender(name, filter);
	}

	@Override
	public void append(LogEvent event) {
		synchronized (events) {
			events.add(event.toImmutable());
		}
	}

	public ArrayList<LogEvent> getLog(Level... levels) {
		HashSet<Level> levelHashSet = new HashSet<>(Arrays.asList(levels));
		ArrayList<LogEvent> ret = new ArrayList<>();
		for (LogEvent e : events) {
			if (levels.length == 0 || levelHashSet.contains(e.getLevel())) {
				ret.add(e);
			}
		}
		return ret;
	}

	public void injectIntoLogger() {
		LoggerContext loggerContext = (LoggerContext) LogManager.getContext(false);

		Configuration configuration = loggerContext.getConfiguration();
		LoggerConfig rootLoggerConfig = configuration.getLoggerConfig("");

		rootLoggerConfig.addAppender(this, Level.ALL, null);

		this.start();
	}

	public void reset() {
		this.events.clear();
	}
}
