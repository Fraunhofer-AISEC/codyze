import java.util.List;
import java.lang.String;

public class Length {

	public void one(int[] e) {
		Foo f = new Foo();

		int[] a = {1, 2, 3};
		int[] b = { };
		String[] c = {"a", "b", "c"};
		String[] d = {"ab", "c"};

		f.fun(a);
		f.fun(b);
		f.fun(c);
		f.fun(d);
		f.fun(e);
	}

	public void two(List<String> e) {
		Foo f = new Foo();

		List<Integer> a = List.of(1, 2, 3);
		List<Integer> b = List.of();
		List<String> c = List.of("a", "b", "c");
		List<String> c = List.of("ab", "c");

		f.bar(a);
		f.bar(b);
		f.bar(c);
		f.bar(d);
		f.bar(e);
	}
}


public class Foo {
	public void fun(int[] a) {}

	public void bar(List<Object> l) {}
}
