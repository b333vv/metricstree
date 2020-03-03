package org.b333vv.metricsTree.model.visitor.type;

import com.intellij.psi.PsiClass;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.b333vv.metricsTree.util.MetricsUtils;
import org.b333vv.metricsTree.model.code.JavaClass;
import org.b333vv.metricsTree.model.metric.Metric;
import org.b333vv.metricsTree.util.MetricsService;

public class JavaClassVisitorsTest extends LightJavaCodeInsightFixtureTestCase {
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MetricsUtils.setProject(this.getProject());
        MetricsService.init(this.getProject());
        myFixture.configureByFiles("Object.java", "JavaCode.java", "JavaClass.java", "JavaMethod.java",
                "JavaPackage.java", "JavaProject.java");
    }

    @Override
    protected String getTestDataPath() {
        return "testData";
    }

    public void testDepthOfInheritanceTreeVisitor() {
        PsiClass psiClass = myFixture.findClass("org.jacoquev.model.code.JavaCode");
        JavaClass javaClass = new JavaClass(psiClass);

        DepthOfInheritanceTreeVisitor depthOfInheritanceTreeVisitor = new DepthOfInheritanceTreeVisitor();
        javaClass.accept(depthOfInheritanceTreeVisitor);

        Metric metric = Metric.of("DIT", "Depth Of Inheritance Tree",
                "/html/DepthOfInheritanceTree.html", 1);

        assertEquals(metric, javaClass.getMetrics().findFirst().get());
    }

    public void testNumberOfChildrenVisitor() {
        PsiClass psiClass = myFixture.findClass("org.jacoquev.model.code.JavaCode");
        JavaClass javaClass = new JavaClass(psiClass);

        NumberOfChildrenVisitor numberOfChildrenVisitor = new NumberOfChildrenVisitor();
        javaClass.accept(numberOfChildrenVisitor);

        Metric metric = Metric.of("NOC", "Number Of Children",
                "/html/NumberOfChildren.html", 4);

        assertEquals(metric, javaClass.getMetrics().findFirst().get());
    }

    public void testResponseForClassVisitor() {
        PsiClass psiClass = myFixture.findClass("org.jacoquev.model.code.JavaClass");
        JavaClass javaClass = new JavaClass(psiClass);

        ResponseForClassVisitor responseForClassVisitor = new ResponseForClassVisitor();
        javaClass.accept(responseForClassVisitor);

        Metric metric = Metric.of("RFC", "Response For Class",
                "/html/ResponseForClass.html", 9);

        assertEquals(metric, javaClass.getMetrics().findFirst().get());
    }

    public void testLackOfCohesionOfMethodsVisitor() {
        PsiClass psiClass = myFixture.findClass("org.jacoquev.model.code.JavaClass");
        JavaClass javaClass = new JavaClass(psiClass);

        LackOfCohesionOfMethodsVisitor lackOfCohesionOfMethodsVisitor = new LackOfCohesionOfMethodsVisitor();
        javaClass.accept(lackOfCohesionOfMethodsVisitor);

        Metric metric = Metric.of("LCOM", "Lack Of Cohesion Of Methods",
                "/html/LackOfCohesionOfMethods.html", 4);

        assertEquals(metric, javaClass.getMetrics().findFirst().get());
    }

    public void testWeightedMethodCountVisitor() {
        PsiClass psiClass = myFixture.findClass("org.jacoquev.model.code.JavaClass");
        JavaClass javaClass = new JavaClass(psiClass);

        WeightedMethodCountVisitor weightedMethodCountVisitor = new WeightedMethodCountVisitor();
        javaClass.accept(weightedMethodCountVisitor);

        Metric metric = Metric.of("WMC", "Weighted Method Count",
                "/html/WeightedMethodCount.html", 11);

        assertEquals(metric, javaClass.getMetrics().findFirst().get());
    }

    public void testNumberOfAddedMethodsVisitor() {
        PsiClass psiClass = myFixture.findClass("org.jacoquev.model.code.JavaClass");
        JavaClass javaClass = new JavaClass(psiClass);

        NumberOfAddedMethodsVisitor numberOfAddedMethodsVisitor = new NumberOfAddedMethodsVisitor();
        javaClass.accept(numberOfAddedMethodsVisitor);

        Metric metric = Metric.of("NOAM", "Number Of Added Methods",
                "/html/NumberOfAddedMethods.html", 7);

        assertEquals(metric, javaClass.getMetrics().findFirst().get());
    }

    public void testNumberOfAttributesVisitor() {
        PsiClass psiClass = myFixture.findClass("org.jacoquev.model.code.JavaClass");
        JavaClass javaClass = new JavaClass(psiClass);

        NumberOfAttributesVisitor numberOfAttributesVisitor = new NumberOfAttributesVisitor();
        javaClass.accept(numberOfAttributesVisitor);

        Metric metric = Metric.of("NOA", "Number Of Attributes",
                "/html/NumberOfAttributes.html", 1);

        assertEquals(metric, javaClass.getMetrics().findFirst().get());
    }

    public void testNumberOfOperationsVisitor() {
        PsiClass psiClass = myFixture.findClass("org.jacoquev.model.code.JavaClass");
        JavaClass javaClass = new JavaClass(psiClass);

        NumberOfOperationsVisitor numberOfOperationsVisitor = new NumberOfOperationsVisitor();
        javaClass.accept(numberOfOperationsVisitor);

        Metric metric = Metric.of("NOO", "Number Of Operations",
                "/html/NumberOfOperations.html", 8);

        assertEquals(metric, javaClass.getMetrics().findFirst().get());
    }

    public void testNumberOfOverriddenMethodsVisitor() {
        PsiClass psiClass = myFixture.findClass("org.jacoquev.model.code.JavaClass");
        JavaClass javaClass = new JavaClass(psiClass);

        NumberOfOverriddenMethodsVisitor numberOfOverriddenMethodsVisitor = new NumberOfOverriddenMethodsVisitor();
        javaClass.accept(numberOfOverriddenMethodsVisitor);

        Metric metric = Metric.of("NOOM", "Number Of Overridden Methods",
                "/html/NumberOfOverriddenMethods.html", 0);

        assertEquals(metric, javaClass.getMetrics().findFirst().get());
    }

    public void testNumberOfAttributesAndMethodsVisitor() {
        PsiClass psiClass = myFixture.findClass("org.jacoquev.model.code.JavaClass");
        JavaClass javaClass = new JavaClass(psiClass);

        NumberOfAttributesAndMethodsVisitor numberOfAttributesAndMethodsVisitor = new NumberOfAttributesAndMethodsVisitor();
        javaClass.accept(numberOfAttributesAndMethodsVisitor);

        Metric metric = Metric.of("SIZE2", "Number Of Attributes And Methods",
                "/html/NumberOfAttributesAndMethods.html", 9);

        assertEquals(metric, javaClass.getMetrics().findFirst().get());
    }

    public void testNumberOfMethodsVisitor() {
        PsiClass psiClass = myFixture.findClass("org.jacoquev.model.code.JavaClass");
        JavaClass javaClass = new JavaClass(psiClass);

        NumberOfMethodsVisitor numberOfMethodsVisitor = new NumberOfMethodsVisitor();
        javaClass.accept(numberOfMethodsVisitor);

        Metric metric = Metric.of("NOM", "Number Of Methods",
                "/html/NumberOfMethods.html", 8);

        assertEquals(metric, javaClass.getMetrics().findFirst().get());
    }

    public void testDataAbstractionCouplingVisitor() {
        PsiClass psiClass = myFixture.findClass("org.jacoquev.model.code.JavaClass");
        JavaClass javaClass = new JavaClass(psiClass);

        DataAbstractionCouplingVisitor dataAbstractionCouplingVisitor = new DataAbstractionCouplingVisitor();
        javaClass.accept(dataAbstractionCouplingVisitor);

        Metric metric = Metric.of("DAC", "Data Abstraction Coupling",
                "/html/DataAbstractionCoupling.html", 0);

        assertEquals(metric, javaClass.getMetrics().findFirst().get());
    }

    public void testMessagePassingCouplingVisitor() {
        PsiClass psiClass = myFixture.findClass("org.jacoquev.model.code.JavaClass");
        JavaClass javaClass = new JavaClass(psiClass);

        MessagePassingCouplingVisitor messagePassingCouplingVisitor = new MessagePassingCouplingVisitor();
        javaClass.accept(messagePassingCouplingVisitor);

        Metric metric = Metric.of("MPC", "Message Passing Coupling",
                "/html/MessagePassingCoupling.html", 13);

        assertEquals(metric, javaClass.getMetrics().findFirst().get());
    }
}