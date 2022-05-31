class Inner {
public:
    int value;

    int init(int a) {
      value = a;
      return a;
    }
};

class Outer {
public:
    Inner inner;

    int init(Inner i) {
        inner = i;
        return i.value;
    }
};



int main() {
  Outer o;
  Inner i;

  i.init(17);

  return o.init(i);
}
