package org.b333vv.metric.verification.kotlin;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.visitor.kotlin.method.KotlinConditionNestingDepthVisitor;
import org.b333vv.metric.model.visitor.kotlin.method.KotlinLoopNestingDepthVisitor;
import org.b333vv.metric.model.visitor.kotlin.method.KotlinNumberOfParametersVisitor;
import org.b333vv.metric.model.visitor.kotlin.method.KotlinLocalityOfAttributeAccessesVisitor;
import org.b333vv.metric.model.visitor.kotlin.method.KotlinForeignDataProvidersVisitor;
import org.b333vv.metric.model.visitor.kotlin.method.KotlinNumberOfAccessedVariablesVisitor;
import org.jetbrains.kotlin.psi.KtClass;
import org.jetbrains.kotlin.psi.KtDeclaration;
import org.jetbrains.kotlin.psi.KtFile;
import org.jetbrains.kotlin.psi.KtNamedFunction;

import static org.junit.Assert.assertEquals;

public class KotlinMethodVisitorsTest extends LightJavaCodeInsightFixtureTestCase {
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        myFixture.configureByFiles(
                "kotlin/MethodMetrics.kt"
        );
    }

    @Override
    protected String getTestDataPath() {
        return "testData";
    }

    private KtNamedFunction openAndFindKtFunction(String relativePath, String className, String functionName) {
        PsiFile psi = myFixture.configureByFile(relativePath);
        if (psi instanceof KtFile) {
            for (PsiElement child : psi.getChildren()) {
                if (child instanceof KtClass && className.equals(((KtClass) child).getName())) {
                    KtClass cls = (KtClass) child;
                    for (KtDeclaration decl : cls.getDeclarations()) {
                        if (decl instanceof KtNamedFunction && functionName.equals(((KtNamedFunction) decl).getName())) {
                            return (KtNamedFunction) decl;
                        }
                    }
                }
            }
        }
        return null;
    }

    public void testCND_if() {
        KtNamedFunction fn = openAndFindKtFunction("kotlin/MethodMetrics.kt", "MethodMetrics", "cndIf");
        assertNotNull(fn);
        KotlinConditionNestingDepthVisitor v = new KotlinConditionNestingDepthVisitor();
        v.visitNamedFunction(fn);
        Metric expected = Metric.of(org.b333vv.metric.model.metric.MetricType.CND, 2);
        assertEquals(expected, v.getMetric());
    }

    public void testCND_when() {
        KtNamedFunction fn = openAndFindKtFunction("kotlin/MethodMetrics.kt", "MethodMetrics", "cndWhen");
        assertNotNull(fn);
        KotlinConditionNestingDepthVisitor v = new KotlinConditionNestingDepthVisitor();
        v.visitNamedFunction(fn);
        Metric expected = Metric.of(org.b333vv.metric.model.metric.MetricType.CND, 2);
        assertEquals(expected, v.getMetric());
    }

    public void testLND() {
        KtNamedFunction fn = openAndFindKtFunction("kotlin/MethodMetrics.kt", "MethodMetrics", "lndTriple");
        assertNotNull(fn);
        KotlinLoopNestingDepthVisitor v = new KotlinLoopNestingDepthVisitor();
        v.visitNamedFunction(fn);
        Metric expected = Metric.of(org.b333vv.metric.model.metric.MetricType.LND, 3);
        assertEquals(expected, v.getMetric());
    }

    public void testNOPM() {
        KtNamedFunction fn = openAndFindKtFunction("kotlin/MethodMetrics.kt", "MethodMetrics", "nopmFun");
        assertNotNull(fn);
        KotlinNumberOfParametersVisitor v = new KotlinNumberOfParametersVisitor();
        v.visitNamedFunction(fn);
        Metric expected = Metric.of(org.b333vv.metric.model.metric.MetricType.NOPM, 3);
        assertEquals(expected, v.getMetric());
    }

    public void testLAA() {
        KtNamedFunction fn = openAndFindKtFunction("kotlin/MethodMetrics.kt", "MethodMetrics", "laaFun");
        assertNotNull(fn);
        KotlinLocalityOfAttributeAccessesVisitor v = new KotlinLocalityOfAttributeAccessesVisitor();
        v.visitNamedFunction(fn);
        String expected = org.b333vv.metric.model.metric.Metric.of(org.b333vv.metric.model.metric.MetricType.LAA, 3.0/5.0).getValue().toString();
        assertEquals(expected, v.getMetric().getValue().toString());
    }

    public void testFDP() {
        KtNamedFunction fn = openAndFindKtFunction("kotlin/MethodMetrics.kt", "MethodMetrics", "fdpFun");
        assertNotNull(fn);
        KotlinForeignDataProvidersVisitor v = new KotlinForeignDataProvidersVisitor();
        v.visitNamedFunction(fn);
        Metric expected = Metric.of(org.b333vv.metric.model.metric.MetricType.FDP, 2);
        assertEquals(expected, v.getMetric());
    }

    public void testNOAV() {
        KtNamedFunction fn = openAndFindKtFunction("kotlin/MethodMetrics.kt", "MethodMetrics", "noavFun");
        assertNotNull(fn);
        KotlinNumberOfAccessedVariablesVisitor v = new KotlinNumberOfAccessedVariablesVisitor();
        v.visitNamedFunction(fn);
        Metric expected = Metric.of(org.b333vv.metric.model.metric.MetricType.NOAV, 4);
        assertEquals(expected, v.getMetric());
    }
}
