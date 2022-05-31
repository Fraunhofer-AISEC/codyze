#include <iostream>


enum Color{
    red=5,
    black
};


class Test {

public:
	static const int CONSTANT = 8;


    int foo(Color c) {
        return c;
    }

    int bar(int i) {
    	return i;
    }
};


int main() {
  int foo;
  Test t;

  t.foo(Color::red);
  t.bar(Test::CONSTANT);
  t.bar(8);
  t.bar(7);
}

