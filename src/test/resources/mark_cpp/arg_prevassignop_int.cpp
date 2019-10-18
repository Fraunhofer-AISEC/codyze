
int call(int a) {
  return a + 1;
}

int main() {
  int foo = 41;
  foo += 1;
  call(foo); // MARK: foo == 42?
}
