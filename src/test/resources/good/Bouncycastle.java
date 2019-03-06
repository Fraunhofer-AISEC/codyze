package good;

/**
 * The most simple Java class for testing the CPG Analysis Server.
 */
public class Bouncycastle {

    private static String myfield;

    public static void main(String... args) {
        String mylocal = "Test";

        String myalias = mylocal;  // for points-to-analysis

        myfield = myalias;
    }

}