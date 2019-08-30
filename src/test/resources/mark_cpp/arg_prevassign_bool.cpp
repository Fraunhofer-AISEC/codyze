
bool call(bool a) {
  return !a;
}

int main() {
  bool foo = false;
  foo = true;
  call(foo);
}