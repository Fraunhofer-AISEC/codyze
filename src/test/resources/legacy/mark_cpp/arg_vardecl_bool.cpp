class Test {

public:
bool call(bool a) {
  return !a;
}
}

int main() {
  bool foo = true;
  Test t();
  t.call(foo);
}