package de.fraunhofer.aisec.bouncycastle;

public class Tuple<A, B> {
    private A first;
    private B second;

    public A getFirst() {
        return first;
    }

    public B getSecond() {
        return second;
    }

    public Tuple(A first, B second) {
        this.first = first;
        this.second = second;
    }
}
