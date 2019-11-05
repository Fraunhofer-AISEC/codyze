
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
    //Botan2 p3 = p2;

    p2.init(test);
    p2.start();

    p2.process();
    p2.process();

    p2.process();

    p2.finish();

    //NOT OK: Calling process() again
    p2.process();

    printf("%s", "Blubb");
  }
