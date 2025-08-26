package variables;

public class NoavTest {
    private int field1;
    private int field2;

    public void test(int param1, int param2) {
        int local1 = param1 + field1;
        int local2 = param2 + field2;
        int local3 = local1 + local2;
    }
}
