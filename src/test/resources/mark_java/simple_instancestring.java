class bla {

  java.lang.String call(java.lang.String s) {
    return "";
  }


  int main() {
    java.lang.String s = "AES/CBC/123";
    java.lang.String j;

    j = call(s);

    bla b = new bla();
    j = b.call(s);
  }
}