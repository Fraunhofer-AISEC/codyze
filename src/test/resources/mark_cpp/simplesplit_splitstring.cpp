#include <iostream>

int call(string s, int a) {
  return 1;
}


int main() {
  std::string s = "AES/CBC/123";

  int a = call(s, 1);
}
