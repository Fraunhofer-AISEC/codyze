#include <iostream>

class Test {

public:
    int call(string s) {
      return 1;
    }
}


int pass() {
  string s = "AES/CBC/123";

  Test t();

  t.call(s);
}


int test2_ok() {
  string s = "SIV/SIV/123";

  Test t();

  t.call(s);
  return 9;
}