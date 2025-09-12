package org.b333vv.metric.model.visitor.type;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.visitor.kotlin.type.*;
import org.jetbrains.kotlin.psi.KtClass;
import org.jetbrains.kotlin.psi.KtFile;
import org.jetbrains.kotlin.psi.KtNamedFunction;

import static org.junit.Assert.assertEquals;

public class KotlinClassVisitorsTest extends LightJavaCodeInsightFixtureTestCase {
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        myFixture.configureByFiles(
                "kotlin/Base.kt",
                "kotlin/Derived.kt",
                "kotlin/Cohesion.kt",
                "kotlin/RfcSample.kt"
        );
    }

    @Override
    protected String getTestDataPath() {
        return "testData";
    }

    private KtClass openAndFindKtClass(String relativePath, String className) {
        PsiFile psi = myFixture.configureByFile(relativePath);
        if (psi instanceof KtFile) {
            for (PsiElement child : psi.getChildren()) {
                if (child instanceof KtClass && className.equals(((KtClass) child).getName())) {
                    return (KtClass) child;
                }
            }
        }
        return null;
    }

    public void testKotlinDIT() {
        KtClass derived = openAndFindKtClass("kotlin/Derived.kt", "Derived");
        assertNotNull(derived);
        KotlinDepthOfInheritanceTreeVisitor dit = new KotlinDepthOfInheritanceTreeVisitor();
        dit.visitClass(derived);
        Metric expected = Metric.of(org.b333vv.metric.model.metric.MetricType.DIT, 1);
        assertEquals(expected, dit.getMetric());

        KtClass base = openAndFindKtClass("kotlin/Base.kt", "Base");
        assertNotNull(base);
        dit = new KotlinDepthOfInheritanceTreeVisitor();
        dit.visitClass(base);
        expected = Metric.of(org.b333vv.metric.model.metric.MetricType.DIT, 0);
        assertEquals(expected, dit.getMetric());
    }

    public void testKotlinNOC() {
        KtClass base = openAndFindKtClass("kotlin/Base.kt", "Base");
        assertNotNull(base);
        KotlinNumberOfChildrenVisitor noc = new KotlinNumberOfChildrenVisitor();
        noc.visitClass(base);
        Metric expected = Metric.of(org.b333vv.metric.model.metric.MetricType.NOC, 0);
        assertEquals(expected, noc.getMetric());
    }

    public void testKotlinRFC() {
        KtClass cls = openAndFindKtClass("kotlin/RfcSample.kt", "RfcSample");
        assertNotNull(cls);
        KotlinResponseForClassVisitor rfc = new KotlinResponseForClassVisitor();
        rfc.visitClass(cls);
        Metric expected = Metric.of(org.b333vv.metric.model.metric.MetricType.RFC, 2);
        assertEquals(expected, rfc.getMetric());
    }

    public void testKotlinTCC() {
        KtClass cls = openAndFindKtClass("kotlin/Cohesion.kt", "Cohesion");
        assertNotNull(cls);
        KotlinTightClassCohesionVisitor tcc = new KotlinTightClassCohesionVisitor();
        tcc.visitClass(cls);
        // Expected: methods f1, f2, f3 -> connected pairs: (f1,f3), (f2,f3) = 2; total pairs = 3 => 2/3
        String expected = org.b333vv.metric.model.metric.Metric.of(org.b333vv.metric.model.metric.MetricType.TCC, 2.0/3.0).getValue().toString();
        assertEquals(expected, tcc.getMetric().getValue().toString());
    }
}
