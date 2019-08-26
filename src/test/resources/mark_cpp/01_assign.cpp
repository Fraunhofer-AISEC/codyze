#include <iostream>

int call() {
  return 1;
}


int main() {
  int foo;
  foo = call();

  std::cout << foo << std::endl;
}
