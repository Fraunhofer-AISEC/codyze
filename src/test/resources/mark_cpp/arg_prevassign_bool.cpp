
class Test {

public:
bool call(bool a) {
  return !a;
}
}

int main() {
  bool foo = false;
  foo = true;
  Test t();
  t.call(foo);
}