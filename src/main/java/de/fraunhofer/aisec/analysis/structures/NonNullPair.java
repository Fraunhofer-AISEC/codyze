
package de.fraunhofer.aisec.analysis.structures;

import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Same as Pair, but with @NonNull annotations for value 0 and value 1.
 *
 * @param <A>
 * @param <B>
 */
public class NonNullPair<A, B> extends Pair {
	@NonNull
	A a;
	@NonNull
	B b;

	public NonNullPair(@NonNull A a, @NonNull B b) {
		super(a, b);
		this.a = a;
		this.b = b;
	}

	@NonNull
	@Override
	public A getValue0() {
		return a;
	}

	@NonNull
	@Override
	public B getValue1() {
		return b;
	}
}
