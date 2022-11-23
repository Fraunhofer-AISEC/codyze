public class Someclass {
    // DOES NOT COMPILE
    // DOES NOT MAKE REAL SENSE

    // allowed:
    // cm.create(), cm.init(), (cm.start(), cm.process()*, cm.finish())+, cm.reset()?

    void ok_minimal() {
        Botan2 p1 = new Botan2(1);
        p1.create();
        p1.init();
        p1.start();
        p1.process();
        p1.finish();
        p1.reset();
    }

    void ok2() {
        Botan2 p2 = new Botan2(1);
        p2.create();
        p2.init();
        p2.start();
        p2.process();
        p2.process();
        p2.process();
        p2.process();
        p2.finish();
    }

    void ok3() {
        Botan2 p3 = new Botan2(1);
        p3.create();
        p3.init();
        p3.start();
        p3.process();
        p3.finish();
    }

    void ok4() {
        Botan2 p4 = new Botan2(1);
        p4.create();
        p4.init();
        p4.start();
        p4.process();
        p4.finish();
        p4.start();
        p4.process();
        p4.finish();
    }

    void nok1() {
        Botan2 p5 = new Botan2(1);
        p5.init();
        p5.start();
        p5.process();
        p5.finish();
    }

    void nok2() {
        Botan2 p6 = new Botan2(1);
        p6.create();
        p6.init();
        if (false) {
            p6.start();
            p6.process();
            p6.finish();
        }
        p6.reset();
    }

    void nok3() {
        Botan2 p6 = new Botan2(1);
        while (true) {
            p6.create();
            p6.init();
            p6.start();
            p6.process();
            p6.finish();
        }
        p6.reset();
    }
}

public class Botan2 {
    public void create() {}

    public void finish() {}

    public void init() {}

    public void process() {}

    public void reset() {}

    public void start() {}
}
