public class NeverPass {

	public void ok() {
		Foo f = new Foo();
		f.first(2);
		f.first(6);
	}

	public void ok2() {
		Foo f = new Foo();
		f.first(1);
		f.first(123);
	}

}


public class Foo {
	public int first(int i) {}
}
