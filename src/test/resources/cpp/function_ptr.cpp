#include <iostream>

class A {
  public:
    void fun() {
      std::cout << "Hello" << std::endl;
    }
};

int main() {
  A a;

  void (A::* f_ptr) () = &A::fun;
  (a.*f_ptr)();
}