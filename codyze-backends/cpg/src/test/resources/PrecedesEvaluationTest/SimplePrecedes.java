import java.util.Random;

public class SimpleFollows {

    public void ok() {
        Foo f = new Foo();
        Bar b = new Bar();
        f.first();
        b.second();
    }

    public void branchOk() {
        Foo f = new Foo();
        Bar b = new Bar();
        if(new Random().nextBoolean())
            f.first();
        else
            f.first();
        b.second();
    }

    public void unreachableSecondNotApplicable() {
        Bar b = new Bar();
        if(false) {
            b.second(); // unreachable -> never executed so no `first()` is needed
        }
    }

    // Should be ok because the `f.f2()` branch is unreachable
    public void unreachableOk() {
        Foo f = new Foo();
        Bar b = new Bar();
        if(false)
            f.f2();
        else
            f.first();
        b.second();
    }

    // should fail because `f.first()` is only called in one branch
    public void branchFail() {
        Foo f = new Foo();
        Bar b = new Bar();
        if(new Random().nextBoolean()) {
            f.first();
        }
        b.second();
    }

    public void fail() {
        Bar b = new Bar();
        b.second();
    }

    // There is no `Bar.second()` so there should be no finding
    public void noFinding() {
        Foo f = new Foo();
        f.first();
    }

}

public class Foo {
    public int first() {}

    public void f2() {}
}

public class Bar {
    public void second() {}
}