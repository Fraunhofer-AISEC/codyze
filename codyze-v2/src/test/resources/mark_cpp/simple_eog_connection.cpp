#include <iostream>

class Test {

public:
    std::string source(std::string s) {
      return "";
    }

    std::string sink(std::string bla) {
        return "foo";
    }
}


int no_connection() {
  std::string s = "AES/CBC/123";
  std::string j;

  Test t();
  if (true) {
    j = t.source(s);
  } else {
    t.sink(s);
  }
}

int connection() {
  std::string s = "AES/CBC/123";
  std::string j;

  Test t();
  j = t.source(s);
  if (true) {
    Test t2();
  }
  t.sink(s);
}

int direct_connection() {
  std::string s = "AES/CBC/123";
  std::string j;

  Test t();
  j = t.source(s);
  t.sink(s);
}

