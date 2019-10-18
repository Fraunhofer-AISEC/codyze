#include <iostream>

int call(int a) {
  std::cout << a << std::endl;
}

void interspersed(int* ptr_foo) {
  (*ptr_foo) += 1;
}

int main() {
  int foo = 41;
  interspersed(&foo);
  call(foo); // MARK: foo == 42?
}


