#include <iostream>

class Test {

public:
    std::string call(std::string s) {
      return "";
    }
}


int main() {
  std::string s = "AES/CBC/123";
  std::string j;

  Test t();
  j = t.call(s);
}

