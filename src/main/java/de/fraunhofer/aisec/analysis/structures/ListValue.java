
package de.fraunhofer.aisec.analysis.structures;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;

public class ListValue extends MarkIntermediateResult implements Iterable<MarkIntermediateResult> {

	private List<MarkIntermediateResult> values = new ArrayList<>();

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
	public Iterator<MarkIntermediateResult> iterator() {
		return values.iterator();
	}

	@Override
	public Spliterator<MarkIntermediateResult> spliterator() {
		return values.spliterator();
	}

	public int size() {
		return values.size();
	}

	public List<MarkIntermediateResult> getAll() {
		return values;
	}
}
