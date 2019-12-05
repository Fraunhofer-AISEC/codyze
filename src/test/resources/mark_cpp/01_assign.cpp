#include <iostream>
class Test {

public:
int call() {
  return 1;
}
}


int main() {
  int foo;
  Test t();
  foo = t.call();

  std::cout << foo << std::endl;
}
