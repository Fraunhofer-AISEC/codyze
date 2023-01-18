
public class SimpleFollows {

	public void ok() {
		Foo f = new Foo();
		f.first();
		Bar b = new Bar();
		b.second();
	}

	public void notSure() {
		Foo f = new Foo();
		f.first();
		Bar b = new Bar();
		if(false)
			b.second();
	}

	public void fail() {
		Foo f = new Foo();
		f.first();
	}

	public void noFinding() {
		Bar b = new Bar();
		b.second();
	}

}

public class Foo {
	public int first() {}

	public void f2() {}
}

public class Bar {
	public void second() {}
}