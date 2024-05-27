import java.util.Random;

public class SimpleArgument {

    public void ok() {
        Foo f = new Foo();
        Bar b = new Bar();

        Object strong = f.strong();
        b.critical(strong);
    }

    public void branchOk() {
        Foo f = new Foo();
        Bar b = new Bar();

        Object strong;
        if(new Random().nextBoolean())
            strong = f.strong();
        else
            strong = f.strong();

        b.critical(strong);
    }

    public void unreachableFirstNotApplicable() {
        Foo f = new Foo();
        Bar b = new Bar();

        if(false) {
            b.critical(null) // unreachable -> never executed so no `strong()` is needed
        }
    }

    // Should be ok because the `f.weak()` branch is unreachable
    public void unreachableOk() {
        Foo f = new Foo();
        Bar b = new Bar();

        Object strong;
        if(false)
            strong = f.weak();
        else
            strong = f.strong();

        b.critical(strong);
    }

    // Should be ok as the final value is strong
	public void overwriteOk() {
		Foo f = new Foo();
		Bar b = new Bar();

        Object strong;
        strong = f.weak();
        strong = f.strong();

		b.critical(strong);
	}

    // Ok since the value is only overwritten later
    public void repurposeOk() {
        Foo f = new Foo();
        Bar b = new Bar();

        Object strong;
        strong = f.strong();

        b.critical(strong);

        strong = f.weak();
    }

    public void fail() {
        Foo f = new Foo();
        Bar b = new Bar();

        Object weak = f.weak();
        b.critical(weak);
    }

    // should fail because `f.strong()` is only called in one branch TODO
    public void branchFail() {
        Foo f = new Foo();
        Bar b = new Bar();

        Object unknown;
        if(new Random().nextBoolean()) {
            unknown = f.strong();
        } else {
            unknown = f.weak();
        }

        b.critical(unknown);
    }

    // Should fail as the final value is weak TODO
    public void overwriteFail() {
        Foo f = new Foo();
        Bar b = new Bar();

        Object weak;
        weak = f.strong();
        weak = f.weak();

        b.critical(weak);
    }

    // FIXME: implementation only considers CallExpressions as of now!
//    // Should fail wven though we do not use a CallExpression to overwrite
//    public void overwrite2Fail() {
//        Foo f = new Foo();
//        Bar b = new Bar();
//
//        Object weak;
//        weak = f.strong();
//        weak = 2;
//
//        b.critical(weak);
//    }

    // Fail since the value is only set correctly afterwards
    public void repurposeFail() {
        Foo f = new Foo();
        Bar b = new Bar();

        Object weak;
        weak = f.weak();

        b.critical(weak);

        weak = f.strong();
    }

    // There is no `Bar.critical()` so there should be no finding
    public void noFinding() {
        Foo f = new Foo();
        Object strong = f.strong();
    }

}

public class Foo {
    public Object strong() {}

    public Object weak() {}
}

public class Bar {
    public void critical(Object foundation) {}
}