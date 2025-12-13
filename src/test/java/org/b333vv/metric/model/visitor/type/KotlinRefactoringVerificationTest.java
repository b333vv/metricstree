
package org.b333vv.metric.model.visitor.type;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.visitor.kotlin.method.KotlinCognitiveComplexityVisitor;
import org.b333vv.metric.model.visitor.kotlin.method.KotlinMcCabeCyclomaticComplexityVisitor;
import org.b333vv.metric.model.visitor.kotlin.type.*;
import org.jetbrains.kotlin.psi.KtClass;
import org.jetbrains.kotlin.psi.KtFile;
import org.jetbrains.kotlin.psi.KtNamedFunction;

import static org.b333vv.metric.model.metric.MetricType.*;

public class KotlinRefactoringVerificationTest extends LightJavaCodeInsightFixtureTestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        myFixture.configureByFiles("kotlin/RefactoringCheck.kt");
    }

    @Override
    protected String getTestDataPath() {
        return "testData";
    }

    private KtClass findClass(String className) {
        PsiFile psi = myFixture.getFile();
        if (psi instanceof KtFile) {
            for (PsiElement child : psi.getChildren()) {
                if (child instanceof KtClass && className.equals(((KtClass) child).getName())) {
                    return (KtClass) child;
                }
            }
        }
        return null;
    }

    public void testDataClassMetrics() {
        KtClass cls = findClass("DataClass");
        assertNotNull(cls);

        KotlinNumberOfMethodsVisitor nom = new KotlinNumberOfMethodsVisitor();
        nom.visitClass(cls);
        // 1 (ctor) + 3 (accessors: x get, y get/set) + 4 (equals, hash, string, copy) +
        // 2 (componentN) = 10
        assertEquals("NOM should include implicit methods", 10.0, nom.getMetric().getValue().doubleValue(), 0.1);

        KotlinWeightedMethodCountVisitor wmc = new KotlinWeightedMethodCountVisitor();
        wmc.visitClass(cls);
        // 10 methods * 1 complexity each = 10
        assertEquals("WMC should sum complexity of implicit methods", 10.0, wmc.getMetric().getValue().doubleValue(),
                0.1);

        KotlinResponseForClassVisitor rfc = new KotlinResponseForClassVisitor();
        rfc.visitClass(cls);
        // All 10 implicit methods are in the response set
        assertEquals("RFC should include implicit signatures", 10.0, rfc.getMetric().getValue().doubleValue(), 0.1);
    }

    public void testPropertyClassMetrics() {
        KtClass cls = findClass("PropertyClass");
        assertNotNull(cls);

        // NOM
        KotlinNumberOfMethodsVisitor nom = new KotlinNumberOfMethodsVisitor();
        nom.visitClass(cls);
        // 1 (explicit) + 3 (prop accessors) = 4 (Implicit constructor not counted by
        // visitor currently)
        assertEquals("NOM", 4.0, nom.getMetric().getValue().doubleValue(), 0.1);

        // LCOM
        KotlinLackOfCohesionOfMethodsVisitor lcom = new KotlinLackOfCohesionOfMethodsVisitor();
        lcom.visitClass(cls);
        // Component count should be 1 (high cohesion) because explicitMethod uses
        // props, linking them
        assertEquals("LCOM", 1.0, lcom.getMetric().getValue().doubleValue(), 0.1);
    }

    public void testComplexity() {
        KtClass cls = findClass("ComplexityClass");
        assertNotNull(cls);

        KtNamedFunction elvisMethod = cls.getDeclarations().stream()
                .filter(d -> d instanceof KtNamedFunction && "elvis".equals(((KtNamedFunction) d).getName()))
                .map(d -> (KtNamedFunction) d)
                .findFirst()
                .orElse(null);
        assertNotNull(elvisMethod);

        // CC
        KotlinMcCabeCyclomaticComplexityVisitor cc = new KotlinMcCabeCyclomaticComplexityVisitor();
        cc.visitNamedFunction(elvisMethod);
        // Base 1 + Elvis 1 = 2 (Safe call +0)
        assertEquals("CC should count Elvis but not Safe Call in assignment", 2.0,
                cc.getMetric().getValue().doubleValue(), 0.1);

        // CCM
        KotlinCognitiveComplexityVisitor ccm = new KotlinCognitiveComplexityVisitor();
        ccm.visitNamedFunction(elvisMethod);
        // Base 0 + Elvis 1 = 1 (Safe call +0)
        assertEquals("CCM should count Elvis but not Safe Call", 1.0, ccm.getMetric().getValue().doubleValue(), 0.1);
    }

    public void testCustomPropertyComplexity() {
        KtClass cls = findClass("CustomPropertyClass");
        assertNotNull(cls);

        KotlinWeightedMethodCountVisitor wmc = new KotlinWeightedMethodCountVisitor();
        wmc.visitClass(cls);

        // Getter: Base 1 + If 1 = 2
        // Setter: Base 1 + If 1 = 2
        // Total: 4 (Implicit constructor not counted)
        assertEquals("WMC should account for custom accessor complexity", 4.0, wmc.getMetric().getValue().doubleValue(),
                0.1);
    }

    public void testVisibility() {
        KtClass cls = findClass("InternalClass");
        assertNotNull(cls);

        KotlinNumberOfPublicAttributesVisitor nopa = new KotlinNumberOfPublicAttributesVisitor();
        nopa.visitClass(cls);

        // internalProp excluded, publicProp included => 1
        assertEquals("NOPA should exclude internal properties", 1.0, nopa.getMetric().getValue().doubleValue(), 0.1);
    }

    public void testFinality() {
        KtClass cls = findClass("FinalClass");
        assertNotNull(cls);

        KotlinNumberOfChildrenVisitor noc = new KotlinNumberOfChildrenVisitor();
        noc.visitClass(cls);

        // Final class => 0
        assertEquals("NOC should be 0 for final class", 0.0, noc.getMetric().getValue().doubleValue(), 0.1);
    }
}
