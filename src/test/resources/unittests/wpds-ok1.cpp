// EXAMPLES FOR CORRECT INTERPROCEDURAL TYPESTATE.

// allowed:
// cm.create(), cm.init(), (cm.start(), cm.process()*, cm.finish())+, cm.reset()?


  void ok1() {
    // Constructor will trigger MARK rule
    Botan2 p2 = new Botan2(1);

    p2.create();

    // Aliasing: Operations on p3 are now equal to p2
    //Botan2 p3 = p2;

    p2.init();

    // Continue in other function + alias to p4
    p2.start();

    cout << "Some irrelevant stmt\n";
    p2.process();
    p2.process();

    p2.process();

    p2.finish();  // Finish on p4 alias

    cout << "Worked.\n";
  }

  Botan2 someFunction(Botan2 x) {
    // The missing start() is here
    x.start();
    return x;
  }