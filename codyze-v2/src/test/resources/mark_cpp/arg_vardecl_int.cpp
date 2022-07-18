class Test {

public:
int call(int a) {
  return a + 42;
}
}

int main() {
  int foo = 42;
  Test t();
  t.call(foo);
}