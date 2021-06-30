#include <stdlib.h>

struct s {
    int a;
};


struct s * S_new(int a) {
    struct s *s = malloc(sizeof(struct s));
    s->a = a;
    return s;
}

void fun() {
    struct s *s_ptr;
    s_ptr = S_new(1);
}
