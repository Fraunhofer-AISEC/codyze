class Test {

public:
int call(int a) {
  return a + 1;
}
}

/*
 foo is assigned the value 42 by "constructor initialization". The ExpressionEvaluator of Mark
 should be able to resolve this.
*/
int main() {
  int foo(42);
  Test t();
  t.call(foo);
}