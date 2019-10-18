
int call(int a) {
  return a + 1;
}

int main() {
  int foo = 1;
  foo = (1,2,3,4,42);
  call(foo);
}