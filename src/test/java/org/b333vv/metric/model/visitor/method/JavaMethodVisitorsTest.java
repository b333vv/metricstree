package org.b333vv.metric.model.visitor.method;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.b333vv.metric.model.code.ClassElement;
import org.b333vv.metric.model.code.MethodElement;
import org.b333vv.metric.model.metric.Metric;

import static org.b333vv.metric.model.metric.MetricType.*;

public class JavaMethodVisitorsTest extends LightJavaCodeInsightFixtureTestCase {
    private static final double DELTA = 1e-15;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        myFixture.configureByFiles("Object.java", "HashMap.java", "AbstractMap.java");
    }

    @Override
    protected String getTestDataPath() {
        return "testData";
    }

    public void testLinesOfCodeVisitor() {
        PsiClass psiClass = myFixture.findClass("java.util.HashMap");
        ClassElement javaClass = new ClassElement(psiClass);
        PsiMethod psiMethod = psiClass.findMethodsByName("removeNode", false)[0];
        MethodElement javaMethod = new MethodElement(psiMethod, javaClass);

        LinesOfCodeVisitor linesOfCodeVisitor = new LinesOfCodeVisitor();
        javaMethod.accept(linesOfCodeVisitor);

        Metric metric = Metric.of(LOC, 50);

        assertEquals(metric, javaMethod.metrics().findFirst().get());
    }

    public void testConditionNestingDepthVisitor() {
        PsiClass psiClass = myFixture.findClass("java.util.HashMap");
        ClassElement javaClass = new ClassElement(psiClass);
        PsiMethod psiMethod = psiClass.findMethodsByName("removeNode", false)[0];
        MethodElement javaMethod = new MethodElement(psiMethod, javaClass);

        ConditionNestingDepthVisitor conditionNestingDepthVisitor = new ConditionNestingDepthVisitor();
        javaMethod.accept(conditionNestingDepthVisitor);

        Metric metric = Metric.of(CND, 4);

        assertEquals(metric, javaMethod.metrics().findFirst().get());
    }

    public void testLoopNestingDepthVisitor() {
        PsiClass psiClass = myFixture.findClass("java.util.HashMap");
        ClassElement javaClass = new ClassElement(psiClass);
        PsiMethod psiMethod = psiClass.findMethodsByName("removeNode", false)[0];
        MethodElement javaMethod = new MethodElement(psiMethod, javaClass);

        LoopNestingDepthVisitor loopNestingDepthVisitor = new LoopNestingDepthVisitor();
        javaMethod.accept(loopNestingDepthVisitor);

        Metric metric = Metric.of(LND, 1);

        assertEquals(metric, javaMethod.metrics().findFirst().get());
    }

    public void testMcCabeCyclomaticComplexityVisitor() {
        PsiClass psiClass = myFixture.findClass("java.util.HashMap");
        ClassElement javaClass = new ClassElement(psiClass);
        PsiMethod psiMethod = psiClass.findMethodsByName("removeNode", false)[0];
        MethodElement javaMethod = new MethodElement(psiMethod, javaClass);

        McCabeCyclomaticComplexityVisitor mcCabeCyclomaticComplexityVisitor = new McCabeCyclomaticComplexityVisitor();
        javaMethod.accept(mcCabeCyclomaticComplexityVisitor);

        Metric metric = Metric.of(CC, 22);

        assertEquals(metric, javaMethod.metrics().findFirst().get());
    }

    public void testNumberOfLoopsVisitor() {
        PsiClass psiClass = myFixture.findClass("java.util.HashMap");
        ClassElement javaClass = new ClassElement(psiClass);
        PsiMethod psiMethod = psiClass.findMethodsByName("removeNode", false)[0];
        MethodElement javaMethod = new MethodElement(psiMethod, javaClass);

        NumberOfLoopsVisitor numberOfLoopsVisitor = new NumberOfLoopsVisitor();
        javaMethod.accept(numberOfLoopsVisitor);

        Metric metric = Metric.of(NOL, 1);

        assertEquals(metric, javaMethod.metrics().findFirst().get());
    }

    public void testNumberOfParametersVisitor() {
        PsiClass psiClass = myFixture.findClass("java.util.HashMap");
        ClassElement javaClass = new ClassElement(psiClass);
        PsiMethod psiMethod = psiClass.findMethodsByName("removeNode", false)[0];
        MethodElement javaMethod = new MethodElement(psiMethod, javaClass);

        NumberOfParametersVisitor numberOfParametersVisitor = new NumberOfParametersVisitor();
        javaMethod.accept(numberOfParametersVisitor);

        Metric metric = Metric.of(NOPM, 5);

        assertEquals(metric, javaMethod.metrics().findFirst().get());
    }

    public void testLocalityOfAttributeAccessesVisitor() {
        PsiClass psiClass = myFixture.findClass("java.util.HashMap");
        ClassElement javaClass = new ClassElement(psiClass);
        PsiMethod psiMethod = psiClass.findMethodsByName("removeNode", false)[0];
        MethodElement javaMethod = new MethodElement(psiMethod, javaClass);

        LocalityOfAttributeAccessesVisitor localityOfAttributeAccessesVisitor = new LocalityOfAttributeAccessesVisitor();
        javaMethod.accept(localityOfAttributeAccessesVisitor);

        Metric metric = Metric.of(LAA, 0.375);

        assertEquals(metric, javaMethod.metrics().findFirst().get());
    }

    public void testForeignDataProvidersVisitor() {
        PsiClass psiClass = myFixture.findClass("java.util.HashMap");
        ClassElement javaClass = new ClassElement(psiClass);
        PsiMethod psiMethod = psiClass.findMethodsByName("removeNode", false)[0];
        MethodElement javaMethod = new MethodElement(psiMethod, javaClass);

        ForeignDataProvidersVisitor foreignDataProvidersVisitor = new ForeignDataProvidersVisitor();
        javaMethod.accept(foreignDataProvidersVisitor);

        Metric metric = Metric.of(FDP, 2);

        assertEquals(metric, javaMethod.metrics().findFirst().get());
    }

    public void testNumberOfAccessedVariablesVisitor() {
        PsiClass psiClass = myFixture.findClass("java.util.HashMap");
        ClassElement javaClass = new ClassElement(psiClass);
        PsiMethod psiMethod = psiClass.findMethodsByName("removeNode", false)[0];
        MethodElement javaMethod = new MethodElement(psiMethod, javaClass);

        NumberOfAccessedVariablesVisitor numberOfAccessedVariablesVisitor = new NumberOfAccessedVariablesVisitor();
        javaMethod.accept(numberOfAccessedVariablesVisitor);

        Metric metric = Metric.of(NOAV, 21);

        assertEquals(metric, javaMethod.metrics().findFirst().get());
    }

    public void testMaximumNestingDepthVisitor() {
        PsiClass psiClass = myFixture.findClass("java.util.HashMap");
        ClassElement javaClass = new ClassElement(psiClass);
        PsiMethod psiMethod = psiClass.findMethodsByName("removeNode", false)[0];
        MethodElement javaMethod = new MethodElement(psiMethod, javaClass);

        MaximumNestingDepthVisitor maximumNestingDepthVisitor = new MaximumNestingDepthVisitor();
        javaMethod.accept(maximumNestingDepthVisitor);

        Metric metric = Metric.of(MND, 5);

        assertEquals(metric, javaMethod.metrics().findFirst().get());
    }

    public void testCouplingIntensityVisitor() {
        PsiClass psiClass = myFixture.findClass("java.util.HashMap");
        ClassElement javaClass = new ClassElement(psiClass);
        PsiMethod psiMethod = psiClass.findMethodsByName("removeNode", false)[0];
        MethodElement javaMethod = new MethodElement(psiMethod, javaClass);

        CouplingIntensityVisitor couplingIntensityVisitor = new CouplingIntensityVisitor();
        javaMethod.accept(couplingIntensityVisitor);

        Metric metric = Metric.of(CINT, 2);

        assertEquals(metric, javaMethod.metrics().findFirst().get());
    }

    public void testCouplingDispersionVisitor() {
        PsiClass psiClass = myFixture.findClass("java.util.HashMap");
        ClassElement javaClass = new ClassElement(psiClass);
        PsiMethod psiMethod = psiClass.findMethodsByName("removeNode", false)[0];
        MethodElement javaMethod = new MethodElement(psiMethod, javaClass);

        CouplingDispersionVisitor couplingDispersionVisitor = new CouplingDispersionVisitor();
        javaMethod.accept(couplingDispersionVisitor);

        Metric metric = Metric.of(CDISP, 0.3333);

        assertEquals(metric.getValue().toString(), (javaMethod.metrics().findFirst().get().getValue().toString()));
    }
}