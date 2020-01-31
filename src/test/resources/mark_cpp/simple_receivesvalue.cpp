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


int main() {
  std::string s = "AES/CBC/123";
  std::string j;

  Test t();
  j = t.source(s);
  t.sink(j);
}

