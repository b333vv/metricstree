package org.b333vv.metric.model.visitor.method;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.b333vv.metric.util.MetricsUtils;
import org.b333vv.metric.model.code.JavaMethod;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.util.MetricsService;

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

        Metric metric = Metric.of("LOC", "Lines Of Code",
                "/html/LOC.html", 50);

        assertEquals(metric, javaMethod.getMetrics().findFirst().get());
    }

    public void testConditionNestingDepthVisitor() {
        PsiClass psiClass = myFixture.findClass("java.util.HashMap");
        PsiMethod psiMethod = psiClass.findMethodsByName("removeNode", false)[0];
        JavaMethod javaMethod = new JavaMethod(psiMethod);

        ConditionNestingDepthVisitor conditionNestingDepthVisitor = new ConditionNestingDepthVisitor();
        javaMethod.accept(conditionNestingDepthVisitor);

        Metric metric = Metric.of("CND", "Condition Nesting Depth",
                "/html/CND.html", 4);

        assertEquals(metric, javaMethod.getMetrics().findFirst().get());
    }

    public void testLoopNestingDepthVisitor() {
        PsiClass psiClass = myFixture.findClass("java.util.HashMap");
        PsiMethod psiMethod = psiClass.findMethodsByName("removeNode", false)[0];
        JavaMethod javaMethod = new JavaMethod(psiMethod);

        LoopNestingDepthVisitor loopNestingDepthVisitor = new LoopNestingDepthVisitor();
        javaMethod.accept(loopNestingDepthVisitor);

        Metric metric = Metric.of("LND", "Loop Nesting Depth",
                "/html/LND.html", 1);

        assertEquals(metric, javaMethod.getMetrics().findFirst().get());
    }

    public void testMcCabeCyclomaticComplexityVisitor() {
        PsiClass psiClass = myFixture.findClass("java.util.HashMap");
        PsiMethod psiMethod = psiClass.findMethodsByName("removeNode", false)[0];
        JavaMethod javaMethod = new JavaMethod(psiMethod);

        McCabeCyclomaticComplexityVisitor mcCabeCyclomaticComplexityVisitor = new McCabeCyclomaticComplexityVisitor();
        javaMethod.accept(mcCabeCyclomaticComplexityVisitor);

        Metric metric = Metric.of("CC", "McCabe Cyclomatic Complexity",
                "/html/CC.html", 22);

        assertEquals(metric, javaMethod.getMetrics().findFirst().get());
    }

    public void testNumberOfLoopsVisitor() {
        PsiClass psiClass = myFixture.findClass("java.util.HashMap");
        PsiMethod psiMethod = psiClass.findMethodsByName("removeNode", false)[0];
        JavaMethod javaMethod = new JavaMethod(psiMethod);

        NumberOfLoopsVisitor numberOfLoopsVisitor = new NumberOfLoopsVisitor();
        javaMethod.accept(numberOfLoopsVisitor);

        Metric metric = Metric.of("NOL", "Number Of Loops",
                "/html/NOL.html", 1);

        assertEquals(metric, javaMethod.getMetrics().findFirst().get());
    }
}