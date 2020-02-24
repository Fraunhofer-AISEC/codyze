import java.util.Vector;

public class WpdsVectorExample {
  void foo() {
    Vector v = new Vector();
    v.add();
    v = accessLastElement(v);
    v.clear();
    v = accessLastElement(v);
    v.add();
    v.lastElement();
  }

  Vector accessLastElement(Vector u) {
    Object o = u.lastElement();
    return u;
  }
}