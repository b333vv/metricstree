package cohesion;

public class TccTest {
    private int a;
    private int b;
    private int c;

    public void m1() { a = 1; }
    public void m2() { b = 2; }
    public void m3() { a = 3; c = 3;}
    void m4() { b = 4; }
}
