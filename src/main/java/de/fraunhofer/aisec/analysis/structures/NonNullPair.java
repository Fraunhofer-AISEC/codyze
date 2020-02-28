
package de.fraunhofer.aisec.analysis.structures;

import org.checkerframework.checker.nullness.qual.NonNull;

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
	public A getValue0() {
		return a;
	}

	@NonNull
	public B getValue1() {
		return b;
	}
}
