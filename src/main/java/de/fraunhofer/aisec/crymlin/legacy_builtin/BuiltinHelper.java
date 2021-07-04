
package de.fraunhofer.aisec.crymlin.legacy_builtin;

import de.fraunhofer.aisec.analysis.structures.ConstantValue;
import de.fraunhofer.aisec.analysis.structures.ErrorValue;
import de.fraunhofer.aisec.analysis.structures.ListValue;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BuiltinHelper {

	private BuiltinHelper() {
		// do not call
	}

	public static List<Vertex> extractResponsibleVertices(@NonNull ListValue argumentList, int numberOfExpectedArguments) throws InvalidArgumentException {

		if (argumentList.size() != numberOfExpectedArguments) {
			throw new InvalidArgumentException(String.format("Invalid number of arguments: %s", argumentList.size()));
		}

		for (int i = 0; i < numberOfExpectedArguments; i++) {
			if (!(argumentList.get(i) instanceof ConstantValue)) {
				throw new InvalidArgumentException(String.format("Argument %s is not a ConstantValue", i));
			}
		}

		List<Vertex> ret = new ArrayList<>();

		for (int i = 0; i < numberOfExpectedArguments; i++) {
			Set<Vertex> responsibleVertices = ((ConstantValue) argumentList.get(i)).getResponsibleVertices();

			if (responsibleVertices.size() != 1) {
				throw new InvalidArgumentException("Vertices for arguments not available or invalid");
			}

			Vertex arg = responsibleVertices.iterator().next();

			if (arg == null) {
				throw new InvalidArgumentException("Vertices for arguments are invalid");
			}
			ret.add(arg);
		}

		return ret;
	}

	public static void verifyArgumentTypesOrThrow(ListValue arguments, Class... expectedClasses) throws InvalidArgumentException {

		if (arguments.size() != expectedClasses.length) {
			throw new InvalidArgumentException(String.format("Invalid number of arguments: %d, %d arguments were expected", arguments.size(), expectedClasses.length));
		}

		for (int i = 0; i < arguments.size(); i++) {
			if (!arguments.get(i).getClass().equals(expectedClasses[i]) && !arguments.get(i).getClass().equals(ErrorValue.class)) {
				throw new InvalidArgumentException(
					String.format("Argument %d is not the correct type. Expected: %s, was: %s", i, expectedClasses[i].getName(), arguments.get(i).getClass().getName()));
			}
		}
	}
}
