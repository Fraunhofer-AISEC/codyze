public class Someclass {
// DOES NOT COMPILE
// DOES NOT MAKE REAL SENSE

// allowed:
// cm.create(), cm.init(), (cm.start(), cm.process()*, cm.finish())+, cm.reset()?

//  void ok_minimal() {
//    Botan p = new Botan(1);
//    p.create();
//    p.init();
//    p.start();
//    p.process();
//    p.finish();
//    p.reset();
//  }
//
//  void ok2() {
//    Botan p = new Botan(1);
//    p.create();
//    p.init();
//    p.start();
//    p.process();
//    p.process();
//    p.process();
//    p.process();
//    p.finish();
//  }
//
//  void ok3() {
//    Botan p = new Botan(1);
//    p.create();
//    p.init();
//    p.start();
//    p.process();
//    p.finish();
//  }
//
//  void ok2() {
//    Botan p = new Botan(1);
//    p.create();
//    p.init();
//    p.start();
//    p.process();
//    p.finish();
//    p.start();
//    p.process();
//    p.finish();
//  }

void ok_minimal() {
    Botan p = new Botan(1);
    p.create();
    p.init();
    if (true) {
      p.start();
    } else {
      p.start();
    }
    p.process();
    p.finish();
    p.reset();
}

//  void nok1() {
//    Botan p = new Botan(1);
//    p.init();
//    p.start();
//    p.process();
//    p.finish();
//  }
//
//  void nok2() {
//    Botan p = new Botan(1);
//    p.create();
//    p.init();
//    if (false) {
//      p.start();
//      p.process();
//      p.finish();
//    }
//    p.reset();
//  }
}