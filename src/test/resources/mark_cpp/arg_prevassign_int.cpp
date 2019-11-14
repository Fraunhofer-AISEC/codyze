
int call(int a) {
  return a + 1;
}

int main() {
  int foo = 1;
  foo = 42;
  foo = 43;
  call(foo);
}