#include <iostream>

class A {
  public:
    void fun() {
      std::cout << "Hello" << std:endl;
    }
}

int main() {
  A a;

  void (* f_ptr) () = &a.fun;
  (*f_ptr)();
}