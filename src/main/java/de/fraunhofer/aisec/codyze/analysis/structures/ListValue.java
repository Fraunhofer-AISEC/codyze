
package de.fraunhofer.aisec.codyze.analysis.structures;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;

public class ListValue extends MarkIntermediateResult implements Iterable<MarkIntermediateResult> {

	private final List<MarkIntermediateResult> values = new ArrayList<>();

	public ListValue() {
		super(ResultType.LIST);
	}

	public void add(MarkIntermediateResult value) {
		this.values.add(value);
	}

	public MarkIntermediateResult get(int i) {
		return values.get(i);
	}

	@Override
	public void forEach(Consumer<? super MarkIntermediateResult> action) {
		values.forEach(action);
	}

	@Override
	public @NonNull Iterator<MarkIntermediateResult> iterator() {
		return values.iterator();
	}

	@Override
	public Spliterator<MarkIntermediateResult> spliterator() {
		return values.spliterator();
	}

	public int size() {
		return values.size();
	}

	public boolean isEmpty() {
		return values.isEmpty();
	}

	public List<MarkIntermediateResult> getAll() {
		return values;
	}

	public void addAll(List<MarkIntermediateResult> add) {
		values.addAll(add);
	}
}
