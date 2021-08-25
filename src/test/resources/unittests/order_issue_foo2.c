#include "ABC.h"

void foo2() {
  ABC* abc = NULL;
  abc = ABC_new(NULL);
  ABC_free(abc);
}
