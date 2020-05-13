package org.b333vv.metric.model.visitor.method;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.util.MetricsUtils;
import org.b333vv.metric.model.code.JavaMethod;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.util.MetricsService;

import static org.b333vv.metric.model.metric.MetricType.*;

public class JavaMethodVisitorsTest extends LightJavaCodeInsightFixtureTestCase {
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MetricsUtils.setProject(getProject());
        MetricsService.init(getProject());
        myFixture.configureByFiles("Object.java", "HashMap.java", "AbstractMap.java");
    }

    @Override
    protected String getTestDataPath() {
        return "testData";
    }

    public void testLinesOfCodeVisitor() {
        PsiClass psiClass = myFixture.findClass("java.util.HashMap");
        PsiMethod psiMethod = psiClass.findMethodsByName("removeNode", false)[0];
        JavaMethod javaMethod = new JavaMethod(psiMethod);

        LinesOfCodeVisitor linesOfCodeVisitor = new LinesOfCodeVisitor();
        javaMethod.accept(linesOfCodeVisitor);

        Metric metric = Metric.of(LOC, 50);

        assertEquals(metric, javaMethod.metrics().findFirst().get());
    }

    public void testConditionNestingDepthVisitor() {
        PsiClass psiClass = myFixture.findClass("java.util.HashMap");
        PsiMethod psiMethod = psiClass.findMethodsByName("removeNode", false)[0];
        JavaMethod javaMethod = new JavaMethod(psiMethod);

        ConditionNestingDepthVisitor conditionNestingDepthVisitor = new ConditionNestingDepthVisitor();
        javaMethod.accept(conditionNestingDepthVisitor);

        Metric metric = Metric.of(CND, 4);

        assertEquals(metric, javaMethod.metrics().findFirst().get());
    }

    public void testLoopNestingDepthVisitor() {
        PsiClass psiClass = myFixture.findClass("java.util.HashMap");
        PsiMethod psiMethod = psiClass.findMethodsByName("removeNode", false)[0];
        JavaMethod javaMethod = new JavaMethod(psiMethod);

        LoopNestingDepthVisitor loopNestingDepthVisitor = new LoopNestingDepthVisitor();
        javaMethod.accept(loopNestingDepthVisitor);

        Metric metric = Metric.of(LND, 1);

        assertEquals(metric, javaMethod.metrics().findFirst().get());
    }

    public void testMcCabeCyclomaticComplexityVisitor() {
        PsiClass psiClass = myFixture.findClass("java.util.HashMap");
        PsiMethod psiMethod = psiClass.findMethodsByName("removeNode", false)[0];
        JavaMethod javaMethod = new JavaMethod(psiMethod);

        McCabeCyclomaticComplexityVisitor mcCabeCyclomaticComplexityVisitor = new McCabeCyclomaticComplexityVisitor();
        javaMethod.accept(mcCabeCyclomaticComplexityVisitor);

        Metric metric = Metric.of(CC, 22);

        assertEquals(metric, javaMethod.metrics().findFirst().get());
    }

    public void testNumberOfLoopsVisitor() {
        PsiClass psiClass = myFixture.findClass("java.util.HashMap");
        PsiMethod psiMethod = psiClass.findMethodsByName("removeNode", false)[0];
        JavaMethod javaMethod = new JavaMethod(psiMethod);

        NumberOfLoopsVisitor numberOfLoopsVisitor = new NumberOfLoopsVisitor();
        javaMethod.accept(numberOfLoopsVisitor);

        Metric metric = Metric.of(NOL, 1);

        assertEquals(metric, javaMethod.metrics().findFirst().get());
    }
}