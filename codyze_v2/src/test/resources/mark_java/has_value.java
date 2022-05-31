class Bar {
    public Bar(int i) {}
}

class Foo {
    public Foo(int i) {}
}


class bla {

    public void call(byte[] a) {

    }

    public void fun () {
        Bar b = new Bar(10);
    }

    public void fun () {
        Foo f = new Foo(11); // `_has_value` links reference `b directly to parameter of `Foo(...)`
    }
}