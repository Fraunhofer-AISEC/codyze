
// DOES NOT COMPILE
// DOES NOT MAKE REAL SENSE

// EXAMPLES FOR CORRECT INTERPROCEDURAL TYPESTATE.

// allowed:
// cm.create(), cm.init(), (cm.start(), cm.process()*, cm.finish())+, cm.reset()?


  void ok1() {
    // Constructor will trigger MARK rule
    Botan2 p2 = new Botan2(1);

    p2.create();

    // Aliasing: Operations on p3 are now equal to p2
    Botan2 p3 = p2;

    p2.init(test);

    // Continue in other function + alias to p4
    Botan2 p4 = someFunction(p2);

    p2.process();
    p3.process();

    // NOT OK: Calling start() again.
    Botan2 p5 = someFunction(p4);

    p5.process();

    p4.finish();
  }

  Botan2 someFunction(Botan2 x) {
    // The missing start() is here
    x.start();
    return x;
  }