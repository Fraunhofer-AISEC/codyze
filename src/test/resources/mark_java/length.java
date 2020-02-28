
class bla {

    public void call(byte[] a) {

    }

    public void fun () {
        byte[] arr = new byte[10];
        call(arr); // success, because arr.length == 10

        byte[] arr2 = new byte[25];
        call(arr2); // fail, because arr.length != 10
    }


}