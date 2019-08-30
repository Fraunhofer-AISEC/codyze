
int call(int a) {
  return a + 1;
}

int main() {
  int foo = 1;
  foo = 42;
  call(foo);
}