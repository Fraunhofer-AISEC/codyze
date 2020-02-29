class Bar {
    public Bar(int i) {}
}

class Foo {
    public Foo(Bar b) {}
}


class bla {

    public void call(byte[] a) {

    }

    public void fun () {
        Bar b = new Bar(10);
        Foo f = new Foo(b); // `_is` links reference `b directly to parameter of `Foo(...)`
    }

    public void fun () {
        Bar b = new Bar(0);
        Foo f = new Foo(b); // `_is` links reference `b directly to parameter of `Foo(...)`
    }
}