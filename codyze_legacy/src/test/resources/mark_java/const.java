public class CT {

    public static final int CONSTANT = 3;

    enum Color
    {
        RED, GREEN, BLUE;
    }

    public static void main(String[] args){
        CT c = new CT();

        c.foo(Color.RED);

        c.bar(CT.CONSTANT);

        c.bar(3);
    }

    private void bar(int constant) {
    }

    private void foo(Color red) {
    }
}
