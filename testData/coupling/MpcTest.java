package coupling;

public class MpcTest {
    private Other other = new Other();
    public void m() {
        other.otherMethod();
        other.otherMethod();
        this.m2();
    }
    public void m2() {}
}
