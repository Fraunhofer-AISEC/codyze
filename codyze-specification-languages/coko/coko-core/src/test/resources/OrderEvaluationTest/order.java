public class Someclass {
    // DOES NOT COMPILE
    // DOES NOT MAKE REAL SENSE

    char[] cipher;
    int key;
    int iv;
    Cipher_Dir direction;
    char[] buf;

    void nok1() {
        Botan p = new Botan(1);
        // p.set_key(key); // not allowed as start
//        p.start(iv);
//        p.finish(buf);
//        p = new Botan(2)
//        p.start(iv)
//        p.finish()
//        p.foo(); // not in the entity and therefore ignored
//        p.set_key(key);
    }

    void nok2() {
        Botan p2 = new Botan(2);
        p2.start(iv);
        // missing p2.finish(buf);
    }

    void nok3() {
        Botan p3 = new Botan(2);
        if (3 < 4) {
            p3.start(iv);
        }
        p3.finish(buf);
        // potentially wrong path which only calls p3.finish without p3.start
    }

    void ok() {
        // ok:
        Botan p4 = new Botan(2);
        p4.start(iv);
        p4.finish(buf);
    }

    void nok4() {
        Botan p4 = new Botan(2);
        if (true) {
            p4.start(iv);
            p4.finish(buf);
        }
        p4.start(iv); // not ok, p4 is already finished
        p4.finish(buf);
    }

    void nok5() {
        // ok:
        {
            Botan p5 = new Botan(2);
            p5.start(iv);
        }
        {
            Botan p5 = new Botan(2);
            p5.finish(buf);
        }
    }

    void nok2() {
        Botan p2 = new Botan(2);
        p2.start(iv); //CODYZE-IGNORE-WrongUseOfBotan_CipherMode
        // missing p2.finish(buf);
    }
}

public class Botan {
    public Botan(int i) {}

    public void create() {}

    public void finish(char[] b) {}

    public void init() {}

    public void process() {}

    public void reset() {}

    public void start(int i) {}

    public void set_key(int i) {}
}
