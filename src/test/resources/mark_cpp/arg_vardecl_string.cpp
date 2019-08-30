
std::string call(std::string a) {
  return a + "42";
}

int main() {
  std::string foo = "42";
  call(foo);
}