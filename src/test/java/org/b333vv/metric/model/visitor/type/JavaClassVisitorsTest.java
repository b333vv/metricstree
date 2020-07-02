package org.b333vv.metric.model.visitor.type;

import com.intellij.psi.PsiClass;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.b333vv.metric.util.MetricsUtils;
import org.b333vv.metric.model.code.JavaClass;
import org.b333vv.metric.model.metric.Metric;

import static org.b333vv.metric.model.metric.MetricType.*;

public class JavaClassVisitorsTest extends LightJavaCodeInsightFixtureTestCase {
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MetricsUtils.setCurrentProject(this.getProject());
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

        Metric metric = Metric.of(DIT, 1);

        assertEquals(metric, javaClass.metrics().findFirst().get());
    }

    public void testNumberOfChildrenVisitor() {
        PsiClass psiClass = myFixture.findClass("java.util.AbstractMap");
        JavaClass javaClass = new JavaClass(psiClass);

        NumberOfChildrenVisitor numberOfChildrenVisitor = new NumberOfChildrenVisitor();
        javaClass.accept(numberOfChildrenVisitor);

        Metric metric = Metric.of(NOC, 1);

        assertEquals(metric, javaClass.metrics().findFirst().get());
    }

    public void testResponseForClassVisitor() {
        PsiClass psiClass = myFixture.findClass("java.util.HashMap");
        JavaClass javaClass = new JavaClass(psiClass);

        ResponseForClassVisitor responseForClassVisitor = new ResponseForClassVisitor();
        javaClass.accept(responseForClassVisitor);

        Metric metric = Metric.of(RFC, 59);

        assertEquals(metric, javaClass.metrics().findFirst().get());
    }

    public void testLackOfCohesionOfMethodsVisitor() {
        PsiClass psiClass = myFixture.findClass("java.util.HashMap");
        JavaClass javaClass = new JavaClass(psiClass);

        LackOfCohesionOfMethodsVisitor lackOfCohesionOfMethodsVisitor = new LackOfCohesionOfMethodsVisitor();
        javaClass.accept(lackOfCohesionOfMethodsVisitor);

        Metric metric = Metric.of(LCOM, 5);

        assertEquals(metric, javaClass.metrics().findFirst().get());
    }

    public void testWeightedMethodCountVisitor() {
        PsiClass psiClass = myFixture.findClass("java.util.HashMap");
        JavaClass javaClass = new JavaClass(psiClass);

        WeightedMethodCountVisitor weightedMethodCountVisitor = new WeightedMethodCountVisitor();
        javaClass.accept(weightedMethodCountVisitor);

        Metric metric = Metric.of(WMC, 263);

        assertEquals(metric, javaClass.metrics().findFirst().get());
    }

    public void testNumberOfAddedMethodsVisitor() {
        PsiClass psiClass = myFixture.findClass("java.util.HashMap");
        JavaClass javaClass = new JavaClass(psiClass);

        NumberOfAddedMethodsVisitor numberOfAddedMethodsVisitor = new NumberOfAddedMethodsVisitor();
        javaClass.accept(numberOfAddedMethodsVisitor);

        Metric metric = Metric.of(NOAM, 47);

        assertEquals(metric, javaClass.metrics().findFirst().get());
    }

    public void testNumberOfAttributesVisitor() {
        PsiClass psiClass = myFixture.findClass("java.util.HashMap");
        JavaClass javaClass = new JavaClass(psiClass);

        NumberOfAttributesVisitor numberOfAttributesVisitor = new NumberOfAttributesVisitor();
        javaClass.accept(numberOfAttributesVisitor);

        Metric metric = Metric.of(NOA, 13);

        assertEquals(metric, javaClass.metrics().findFirst().get());
    }

    public void testNumberOfOperationsVisitor() {
        PsiClass psiClass = myFixture.findClass("java.util.HashMap");
        JavaClass javaClass = new JavaClass(psiClass);

        NumberOfOperationsVisitor numberOfOperationsVisitor = new NumberOfOperationsVisitor();
        javaClass.accept(numberOfOperationsVisitor);

        Metric metric = Metric.of(NOO, 51);

        assertEquals(metric, javaClass.metrics().findFirst().get());
    }

    public void testNumberOfOverriddenMethodsVisitor() {
        PsiClass psiClass = myFixture.findClass("java.util.HashMap");
        JavaClass javaClass = new JavaClass(psiClass);

        NumberOfOverriddenMethodsVisitor numberOfOverriddenMethodsVisitor = new NumberOfOverriddenMethodsVisitor();
        javaClass.accept(numberOfOverriddenMethodsVisitor);

        Metric metric = Metric.of(NOOM, 0);

        assertEquals(metric, javaClass.metrics().findFirst().get());
    }

    public void testNumberOfAttributesAndMethodsVisitor() {
        PsiClass psiClass = myFixture.findClass("java.util.HashMap");
        JavaClass javaClass = new JavaClass(psiClass);

        NumberOfAttributesAndMethodsVisitor numberOfAttributesAndMethodsVisitor = new NumberOfAttributesAndMethodsVisitor();
        javaClass.accept(numberOfAttributesAndMethodsVisitor);

        Metric metric = Metric.of(SIZE2, 64);

        assertEquals(metric, javaClass.metrics().findFirst().get());
    }

    public void testNumberOfMethodsVisitor() {
        PsiClass psiClass = myFixture.findClass("java.util.HashMap");
        JavaClass javaClass = new JavaClass(psiClass);

        NumberOfMethodsVisitor numberOfMethodsVisitor = new NumberOfMethodsVisitor();
        javaClass.accept(numberOfMethodsVisitor);

        Metric metric = Metric.of(NOM, 51);

        assertEquals(metric, javaClass.metrics().findFirst().get());
    }

    public void testDataAbstractionCouplingVisitor() {
        PsiClass psiClass = myFixture.findClass("java.util.HashMap");
        JavaClass javaClass = new JavaClass(psiClass);

        DataAbstractionCouplingVisitor dataAbstractionCouplingVisitor = new DataAbstractionCouplingVisitor();
        javaClass.accept(dataAbstractionCouplingVisitor);

        Metric metric = Metric.of(DAC, 1);

        assertEquals(metric, javaClass.metrics().findFirst().get());
    }

    public void testMessagePassingCouplingVisitor() {
        PsiClass psiClass = myFixture.findClass("java.util.HashMap");
        JavaClass javaClass = new JavaClass(psiClass);

        MessagePassingCouplingVisitor messagePassingCouplingVisitor = new MessagePassingCouplingVisitor();
        javaClass.accept(messagePassingCouplingVisitor);

        Metric metric = Metric.of(MPC, 51);

        assertEquals(metric, javaClass.metrics().findFirst().get());
    }

    public void testAccessToForeignDataVisitor() {
        PsiClass psiClass = myFixture.findClass("java.util.HashMap");
        JavaClass javaClass = new JavaClass(psiClass);

        AccessToForeignDataVisitor accessToForeignDataVisitor = new AccessToForeignDataVisitor();
        javaClass.accept(accessToForeignDataVisitor);

        Metric metric = Metric.of(ATFD, 0);

        assertEquals(metric, javaClass.metrics().findFirst().get());
    }

    public void testNumberOfPublicAttributesVisitor() {
        PsiClass psiClass = myFixture.findClass("java.util.HashMap");
        JavaClass javaClass = new JavaClass(psiClass);

        NumberOfPublicAttributesVisitor numberOfPublicAttributesVisitor = new NumberOfPublicAttributesVisitor();
        javaClass.accept(numberOfPublicAttributesVisitor);

        Metric metric = Metric.of(NOPA, 0);

        assertEquals(metric, javaClass.metrics().findFirst().get());
    }

    public void testNumberOfAccessorMethodsVisitor() {
        PsiClass psiClass = myFixture.findClass("java.util.HashMap");
        JavaClass javaClass = new JavaClass(psiClass);

        NumberOfAccessorMethodsVisitor numberOfAccessorMethodsVisitor = new NumberOfAccessorMethodsVisitor();
        javaClass.accept(numberOfAccessorMethodsVisitor);

        Metric metric = Metric.of(NOAC, 0);

        assertEquals(metric, javaClass.metrics().findFirst().get());
    }

    public void testTightClassCohesionVisitor() {
        PsiClass psiClass = myFixture.findClass("java.util.HashMap");
        JavaClass javaClass = new JavaClass(psiClass);

        TightClassCohesionVisitor tightClassCohesionVisitor = new TightClassCohesionVisitor();
        javaClass.accept(tightClassCohesionVisitor);

        Metric metric = Metric.of(TCC, 0.277);

        assertEquals(metric.getValue().toString(), javaClass.metrics().findFirst().get().getValue().toString());
    }

    public void testWeightOfAClassVisitor() {
        PsiClass psiClass = myFixture.findClass("java.util.HashMap");
        JavaClass javaClass = new JavaClass(psiClass);

        WeightOfAClassVisitor weightOfAClassVisitor = new WeightOfAClassVisitor();
        javaClass.accept(weightOfAClassVisitor);

        Metric metric = Metric.of(WOC, 1.0);

        assertEquals(metric, javaClass.metrics().findFirst().get());
    }
}