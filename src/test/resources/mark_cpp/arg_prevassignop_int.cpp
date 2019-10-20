
int call(int a) {
  return a + 1;
}

/*
This is the most simple data flow problem.

We do not expect the Mark ExpressionEvaluator to resolve on its own that foo==42.

This should be solved by a generic data flow solver, external to the ExpressionEvaluator.
*/
int main() {
  int foo = 41;
  foo += 1;
  call(foo); // MARK: foo == 42?
}
