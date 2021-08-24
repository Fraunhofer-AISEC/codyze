#include "ABC.h"

int main() {
  ABC* abc; // direct assignment in variable declaraion seem to be broken in WPDS
  abc = ABC_new(NULL);
  ABC_free(abc);
}
