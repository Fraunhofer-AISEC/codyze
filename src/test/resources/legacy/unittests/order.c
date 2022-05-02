#include "ABC.h"

void foo1() {
  ABC* abc = ABC_new(NULL);
  ABC_free(abc);
}

void foo2() {
  ABC* abc = NULL;
  abc = ABC_new(NULL);
  ABC_free(abc);
}
