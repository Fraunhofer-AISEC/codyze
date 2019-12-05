
class bla {

  java.lang.String call(java.lang.String s) {
    return "";
  }


  int test1() {
    java.lang.String s = "AES/CBC/123";

    bla b = new bla();

    b.call(s);
    return 9;
  }

  int test2_ok() {
      java.lang.String s = "SIV/SIV/123";

      bla b = new bla();

      b.call(s);
      return 9;
  }

}