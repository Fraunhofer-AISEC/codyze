
package de.fraunhofer.aisec.crymlin.utils;

import org.checkerframework.checker.nullness.qual.Nullable;

public class Pair<A, B> {
	A a;
	@Nullable
	B b;

	public Pair(A a, @Nullable B b) {
		this.a = a;
		this.b = b;
	}

	public A getValue0() {
		return a;
	}

	@Nullable
	public B getValue1() {
		return b;
	}
}
