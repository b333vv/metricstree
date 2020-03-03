package org.b333vv.metricsTree.model.visitor.method;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.b333vv.metricsTree.util.MetricsUtils;
import org.b333vv.metricsTree.model.code.JavaMethod;
import org.b333vv.metricsTree.model.metric.Metric;
import org.b333vv.metricsTree.util.MetricsService;

public class JavaMethodVisitorsTest extends LightJavaCodeInsightFixtureTestCase {
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MetricsUtils.setProject(getProject());
        MetricsService.init( getProject());
        myFixture.configureByFiles("Object.java", "JavaCode.java", "JavaClass.java", "JavaMethod.java",
                "JavaPackage.java", "JavaProject.java");
    }

    @Override
    protected String getTestDataPath() {
        return "testData";
    }

    public void testLinesOfCodeVisitor() {
        PsiClass psiClass = myFixture.findClass("org.jacoquev.model.code.JavaCode");
        PsiMethod psiMethod = psiClass.findMethodsByName("equals", false)[0];
        JavaMethod javaMethod = new JavaMethod(psiMethod);

        LinesOfCodeVisitor linesOfCodeVisitor = new LinesOfCodeVisitor();
        javaMethod.accept(linesOfCodeVisitor);

        Metric metric = Metric.of("LOC", "Lines Of Code",
                "/html/LinesOfCode.html", 8);

        assertEquals(metric, javaMethod.getMetrics().findFirst().get());
    }

    public void testConditionNestingDepthVisitor() {
        PsiClass psiClass = myFixture.findClass("org.jacoquev.model.code.JavaCode");
        PsiMethod psiMethod = psiClass.findMethodsByName("equals", false)[0];
        JavaMethod javaMethod = new JavaMethod(psiMethod);

        ConditionNestingDepthVisitor conditionNestingDepthVisitor = new ConditionNestingDepthVisitor();
        javaMethod.accept(conditionNestingDepthVisitor);

        Metric metric = Metric.of("CND", "Condition Nesting Depth",
                "/html/ConditionNestingDepth.html", 1);

        assertEquals(metric, javaMethod.getMetrics().findFirst().get());
    }

    public void testFanInVisitor() {
        PsiClass psiClass = myFixture.findClass("org.jacoquev.model.code.JavaCode");
        PsiMethod psiMethod = psiClass.findMethodsByName("equals", false)[0];
        JavaMethod javaMethod = new JavaMethod(psiMethod);

        FanInVisitor fanInVisitor = new FanInVisitor();
        javaMethod.accept(fanInVisitor);

        Metric metric = Metric.of("FANIN", "Fan-In",
                "/html/FanIn.html", 0);

        assertEquals(metric, javaMethod.getMetrics().findFirst().get());
    }

    public void testFanOutVisitor() {
        PsiClass psiClass = myFixture.findClass("org.jacoquev.model.code.JavaCode");
        PsiMethod psiMethod = psiClass.findMethodsByName("equals", false)[0];
        JavaMethod javaMethod = new JavaMethod(psiMethod);

        FanOutVisitor fanOutVisitor = new FanOutVisitor();
        javaMethod.accept(fanOutVisitor);

        Metric metric = Metric.of("FANOUT", "Fan-Out",
                "/html/FanOut.html", 5);

        assertEquals(metric, javaMethod.getMetrics().findFirst().get());
    }

    public void testLoopNestingDepthVisitor() {
        PsiClass psiClass = myFixture.findClass("org.jacoquev.model.code.JavaCode");
        PsiMethod psiMethod = psiClass.findMethodsByName("equals", false)[0];
        JavaMethod javaMethod = new JavaMethod(psiMethod);

        LoopNestingDepthVisitor loopNestingDepthVisitor = new LoopNestingDepthVisitor();
        javaMethod.accept(loopNestingDepthVisitor);

        Metric metric = Metric.of("LND", "Loop Nesting Depth",
                "/html/LoopNestingDepth.html", 0);

        assertEquals(metric, javaMethod.getMetrics().findFirst().get());
    }

    public void testMcCabeCyclomaticComplexityVisitor() {
        PsiClass psiClass = myFixture.findClass("org.jacoquev.model.code.JavaCode");
        PsiMethod psiMethod = psiClass.findMethodsByName("equals", false)[0];
        JavaMethod javaMethod = new JavaMethod(psiMethod);

        McCabeCyclomaticComplexityVisitor mcCabeCyclomaticComplexityVisitor = new McCabeCyclomaticComplexityVisitor();
        javaMethod.accept(mcCabeCyclomaticComplexityVisitor);

        Metric metric = Metric.of("CC", "McCabe Cyclomatic Complexity",
                "/html/McCabeCyclomaticComplexity.html", 5);

        assertEquals(metric, javaMethod.getMetrics().findFirst().get());
    }

    public void testNumberOfLoopsVisitor() {
        PsiClass psiClass = myFixture.findClass("org.jacoquev.model.code.JavaCode");
        PsiMethod psiMethod = psiClass.findMethodsByName("equals", false)[0];
        JavaMethod javaMethod = new JavaMethod(psiMethod);

        NumberOfLoopsVisitor numberOfLoopsVisitor = new NumberOfLoopsVisitor();
        javaMethod.accept(numberOfLoopsVisitor);

        Metric metric = Metric.of("NOLPS", "Number Of Loops",
                "/html/NumberOfLoops.html", 0);

        assertEquals(metric, javaMethod.getMetrics().findFirst().get());
    }
}