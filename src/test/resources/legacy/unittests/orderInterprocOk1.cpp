
// DOES NOT COMPILE
// DOES NOT MAKE REAL SENSE

// EXAMPLES FOR CORRECT INTERPROCEDURAL TYPESTATE.

// allowed:
// cm.create(), cm.init(), (cm.start(), cm.process()*, cm.finish())+, cm.reset()?


  void ok1() {
    // Constructor will trigger MARK rule
    Botan2 p2 = new Botan2(1);

    // Aliasing: Operations on p3 are now equal to p2
    Botan2 p3 = p2;

    p2.create();

    p2.init(test);

    // Continue in other function
    someFunction(p2);
    p2.process();
    p2.process();
    p2.process();

    p2.finish();
  }

  Botan2 someFunction(Botan2 x) {
    // The missing start() is here
    x.start();
    return x;
  }