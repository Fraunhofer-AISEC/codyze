package de.fraunhofer.aisec.crymlin.server;

import java.util.HashMap;
import java.util.Map;

import de.fraunhofer.aisec.crymlin.structures.Method;

public class AnalysisContext {
	
	/**
	 * Map of method signatures to {@code Method}s.
	 */
	public final Map<String, Method> methods = new HashMap<>();
	
	
}
