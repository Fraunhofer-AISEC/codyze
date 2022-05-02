class Test {

public:
int call(int a) {
  return a + 1;
}
}

/*
foo is assigned the value 42 by "uniform initialization", which allows only one value in curly braces.
The Mark ExpressionEvaluator should resolve this.
*/
int main() {
  int foo{ 42 };
  Test t();
  t.call(foo);
}