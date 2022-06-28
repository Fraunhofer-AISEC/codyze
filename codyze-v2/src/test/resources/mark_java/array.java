
class MyArray {
    public void fun(String[] array) {
        System.out.println(array);
    }
}

public class Array {
    public void fun() {
        MyArray a = new MyArray();
        a.fun(new String[]{"1", "2", "3"});
    }
}
