package coupling;

import inheritance.A;
import inheritance.B;
import inheritance.C;

public class CdispTest {
    public void test() {
        A a = new A();
        B b = new B();
        C c = new C();
        a.m1();
        b.m2();
        c.m3();
        c.m1();
    }
}
