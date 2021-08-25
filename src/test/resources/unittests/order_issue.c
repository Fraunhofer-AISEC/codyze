#include "ABC.h"

// THIS TEST PASSES FOR THE WRONG REASON.
// Removing either of the function breaks NFA or WPDS, respectively.
// It should pass, with
// - only foo1
// - only foo2
// - both foo1 + foo2
void foo1() {
  ABC* abc = ABC_new(NULL);
  ABC_free(abc);
}

void foo2() {
  ABC* abc = NULL;
  abc = ABC_new(NULL);
  ABC_free(abc);
}
