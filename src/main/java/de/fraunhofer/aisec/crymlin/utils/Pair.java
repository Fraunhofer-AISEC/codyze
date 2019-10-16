
package de.fraunhofer.aisec.crymlin.utils;

public class Pair<A, B> {
	A a;
	B b;

	public Pair(A a, B b) {
		this.a = a;
		this.b = b;
	}

	public A getValue0() {
		return a;
	}

	public B getValue1() {
		return b;
	}
}
