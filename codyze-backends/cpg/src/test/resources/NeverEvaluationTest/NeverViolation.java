public class NeverViolation {

	public void ok() {
		Foo f = new Foo();
		f.first(2);
		f.first(6);
	}

	public void fail() {
		Foo f = new Foo();
		f.first(-1);
		f.first(1234);
	}

}


public class Foo {
	public int first(int i) {}
}
