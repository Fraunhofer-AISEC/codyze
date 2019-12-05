#include <iostream>
class Test {

public:
int call(int a) {
  std::cout << a << std::endl;
}

void interspersed(int* ptr_foo) {
  (*ptr_foo) += 1;
}
}

int main() {
  int foo = 41;
  Test t();
  t.interspersed(&foo);
  t.tcall(foo); // MARK: foo == 42?
}


