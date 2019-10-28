#include <iostream>

class A {
  public:
    static void fun() {
      std::cout << "Hello" << std::endl;
    }

    void bar() {
      std::cout << "Bello" << std::endl;
    }
}

int main() {
  A::fun();

  A a;
  a.fun();
  a.bar();
}
