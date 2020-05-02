package org.b333vv.metric.model.visitor.type;

import com.intellij.psi.PsiClass;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.b333vv.metric.util.MetricsUtils;
import org.b333vv.metric.model.code.JavaClass;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.util.MetricsService;

public class JavaClassVisitorsTest extends LightJavaCodeInsightFixtureTestCase {
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MetricsUtils.setProject(this.getProject());
        MetricsService.init(this.getProject());
        myFixture.configureByFiles("Object.java", "HashMap.java", "AbstractMap.java");
    }

    @Override
    protected String getTestDataPath() {
        return "testData";
    }

    public void testDepthOfInheritanceTreeVisitor() {
        PsiClass psiClass = myFixture.findClass("java.util.AbstractMap");
        JavaClass javaClass = new JavaClass(psiClass);

        DepthOfInheritanceTreeVisitor depthOfInheritanceTreeVisitor = new DepthOfInheritanceTreeVisitor();
        javaClass.accept(depthOfInheritanceTreeVisitor);

        Metric metric = Metric.of("DIT", "Depth Of Inheritance Tree",
                "/html/DIT.html", 1);

        assertEquals(metric, javaClass.getMetrics().findFirst().get());
    }

    public void testNumberOfChildrenVisitor() {
        PsiClass psiClass = myFixture.findClass("java.util.AbstractMap");
        JavaClass javaClass = new JavaClass(psiClass);

        NumberOfChildrenVisitor numberOfChildrenVisitor = new NumberOfChildrenVisitor();
        javaClass.accept(numberOfChildrenVisitor);

        Metric metric = Metric.of("NOC", "Number Of Children",
                "/html/NOC.html", 1);

        assertEquals(metric, javaClass.getMetrics().findFirst().get());
    }

    public void testResponseForClassVisitor() {
        PsiClass psiClass = myFixture.findClass("java.util.HashMap");
        JavaClass javaClass = new JavaClass(psiClass);

        ResponseForClassVisitor responseForClassVisitor = new ResponseForClassVisitor();
        javaClass.accept(responseForClassVisitor);

        Metric metric = Metric.of("RFC", "Response For Class",
                "/html/RFC.html", 59);

        assertEquals(metric, javaClass.getMetrics().findFirst().get());
    }

    public void testLackOfCohesionOfMethodsVisitor() {
        PsiClass psiClass = myFixture.findClass("java.util.HashMap");
        JavaClass javaClass = new JavaClass(psiClass);

        LackOfCohesionOfMethodsVisitor lackOfCohesionOfMethodsVisitor = new LackOfCohesionOfMethodsVisitor();
        javaClass.accept(lackOfCohesionOfMethodsVisitor);

        Metric metric = Metric.of("LCOM", "Lack Of Cohesion Of Methods",
                "/html/LCOM.html", 5);

        assertEquals(metric, javaClass.getMetrics().findFirst().get());
    }

    public void testWeightedMethodCountVisitor() {
        PsiClass psiClass = myFixture.findClass("java.util.HashMap");
        JavaClass javaClass = new JavaClass(psiClass);

        WeightedMethodCountVisitor weightedMethodCountVisitor = new WeightedMethodCountVisitor();
        javaClass.accept(weightedMethodCountVisitor);

        Metric metric = Metric.of("WMC", "Weighted Method Count",
                "/html/WMC.html", 263);

        assertEquals(metric, javaClass.getMetrics().findFirst().get());
    }

    public void testNumberOfAddedMethodsVisitor() {
        PsiClass psiClass = myFixture.findClass("java.util.HashMap");
        JavaClass javaClass = new JavaClass(psiClass);

        NumberOfAddedMethodsVisitor numberOfAddedMethodsVisitor = new NumberOfAddedMethodsVisitor();
        javaClass.accept(numberOfAddedMethodsVisitor);

        Metric metric = Metric.of("NOAM", "Number Of Added Methods",
                "/html/NOAM.html", 47);

        assertEquals(metric, javaClass.getMetrics().findFirst().get());
    }

    public void testNumberOfAttributesVisitor() {
        PsiClass psiClass = myFixture.findClass("java.util.HashMap");
        JavaClass javaClass = new JavaClass(psiClass);

        NumberOfAttributesVisitor numberOfAttributesVisitor = new NumberOfAttributesVisitor();
        javaClass.accept(numberOfAttributesVisitor);

        Metric metric = Metric.of("NOA", "Number Of Attributes",
                "/html/NOA.html", 13);

        assertEquals(metric, javaClass.getMetrics().findFirst().get());
    }

    public void testNumberOfOperationsVisitor() {
        PsiClass psiClass = myFixture.findClass("java.util.HashMap");
        JavaClass javaClass = new JavaClass(psiClass);

        NumberOfOperationsVisitor numberOfOperationsVisitor = new NumberOfOperationsVisitor();
        javaClass.accept(numberOfOperationsVisitor);

        Metric metric = Metric.of("NOO", "Number Of Operations",
                "/html/NOO.html", 51);

        assertEquals(metric, javaClass.getMetrics().findFirst().get());
    }

    public void testNumberOfOverriddenMethodsVisitor() {
        PsiClass psiClass = myFixture.findClass("java.util.HashMap");
        JavaClass javaClass = new JavaClass(psiClass);

        NumberOfOverriddenMethodsVisitor numberOfOverriddenMethodsVisitor = new NumberOfOverriddenMethodsVisitor();
        javaClass.accept(numberOfOverriddenMethodsVisitor);

        Metric metric = Metric.of("NOOM", "Number Of Overridden Methods",
                "/html/NOOM.html", 0);

        assertEquals(metric, javaClass.getMetrics().findFirst().get());
    }

    public void testNumberOfAttributesAndMethodsVisitor() {
        PsiClass psiClass = myFixture.findClass("java.util.HashMap");
        JavaClass javaClass = new JavaClass(psiClass);

        NumberOfAttributesAndMethodsVisitor numberOfAttributesAndMethodsVisitor = new NumberOfAttributesAndMethodsVisitor();
        javaClass.accept(numberOfAttributesAndMethodsVisitor);

        Metric metric = Metric.of("SIZE2", "Number Of Attributes And Methods",
                "/html/SIZE2.html", 64);

        assertEquals(metric, javaClass.getMetrics().findFirst().get());
    }

    public void testNumberOfMethodsVisitor() {
        PsiClass psiClass = myFixture.findClass("java.util.HashMap");
        JavaClass javaClass = new JavaClass(psiClass);

        NumberOfMethodsVisitor numberOfMethodsVisitor = new NumberOfMethodsVisitor();
        javaClass.accept(numberOfMethodsVisitor);

        Metric metric = Metric.of("NOM", "Number Of Methods",
                "/html/NOM.html", 51);

        assertEquals(metric, javaClass.getMetrics().findFirst().get());
    }

    public void testDataAbstractionCouplingVisitor() {
        PsiClass psiClass = myFixture.findClass("java.util.HashMap");
        JavaClass javaClass = new JavaClass(psiClass);

        DataAbstractionCouplingVisitor dataAbstractionCouplingVisitor = new DataAbstractionCouplingVisitor();
        javaClass.accept(dataAbstractionCouplingVisitor);

        Metric metric = Metric.of("DAC", "Data Abstraction Coupling",
                "/html/DAC.html", 1);

        assertEquals(metric, javaClass.getMetrics().findFirst().get());
    }

    public void testMessagePassingCouplingVisitor() {
        PsiClass psiClass = myFixture.findClass("java.util.HashMap");
        JavaClass javaClass = new JavaClass(psiClass);

        MessagePassingCouplingVisitor messagePassingCouplingVisitor = new MessagePassingCouplingVisitor();
        javaClass.accept(messagePassingCouplingVisitor);

        Metric metric = Metric.of("MPC", "Message Passing Coupling",
                "/html/MPC.html", 51);

        assertEquals(metric, javaClass.getMetrics().findFirst().get());
    }
}