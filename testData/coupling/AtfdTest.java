package coupling;

public class AtfdTest {
    private ForeignData data1 = new ForeignData();
    private ForeignData data2 = new ForeignData();
    public void m() {
        int a = data1.foreignField;
        int b = data2.foreignField;
        int c = data1.foreignField;
    }
}
