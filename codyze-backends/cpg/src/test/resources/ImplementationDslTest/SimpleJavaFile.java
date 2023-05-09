import java.util.Random;

public class SimpleJavaFile {

	public void one() {
		Foo f = new Foo();
		f.fun(2);
		f.bar("hello", "Test");
		f.bar("hello", "world");
		f.fun(6);
	}

	public void two() {
		Foo f = new Foo();
		f.fun(-1);
		f.fun(1234);
		f.bar("Test!", "my class!");
		f.fun(-127);
		f.bar("my Test", "Test");
	}

}


public class Foo {
	public int fun(int i) {}

	public void bar(String s1, String s2) {}
}
