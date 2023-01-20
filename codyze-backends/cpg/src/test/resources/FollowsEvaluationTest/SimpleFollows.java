import java.util.Random;

public class SimpleFollows {

	public void ok() {
		Foo f = new Foo();
		f.first();
		Bar b = new Bar();
		b.second();
	}

	public void branchOk() {
		Foo f = new Foo();
		f.first();
		Bar b = new Bar();
		if(new Random().nextBoolean())
			b.second();
		else
			b.second();
	}

	// Should be ok because the "f.f2()" branch is unreachable
//	public void shouldBeOk() {
//		Foo f = new Foo();
//		f.first();
//		Bar b = new Bar();
//		if(false)
//			f.f2();
//		else
//			b.second();
//	}

	// what behavior should follows have here?
//	public void notSure() {
//		Foo f = new Foo();
//		f.first();
//		f.first();
//		Bar b = new Bar();
//		b.second();
//	}

	// should fail because b.second is only called in one branch
	public void branchFail() {
		Foo f = new Foo();
		f.first();
		Bar b = new Bar();
		if(new Random().nextBoolean()) {
			b.second();
			f = new Foo();
		}
		f.f2();
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