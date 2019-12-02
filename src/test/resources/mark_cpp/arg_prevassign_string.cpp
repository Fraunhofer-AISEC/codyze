
class Test {

public:

    std::string call(std::string a) {
      return a + "42";
    }
}

int main() {
  std::string foo ("Hello");
  foo = "42";
  Test t();
  t.call(foo);
}