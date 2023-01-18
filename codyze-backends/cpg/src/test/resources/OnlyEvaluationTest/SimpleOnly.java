public class SimpleOnly {

	public void ok() {
		Foo f = new Foo();
		f.fun(2);
		f.fun(6);
	}

	public void fail() {
		Foo f = new Foo();
		f.fun(-1);
		f.fun(1234);
	}

}








public class Foo {
	public int fun(int i) {}
}
