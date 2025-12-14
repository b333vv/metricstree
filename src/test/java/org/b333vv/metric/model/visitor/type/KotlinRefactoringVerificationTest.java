
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

    public void testOpenClassInheritors() {
        KtClass cls = findClass("OpenClass");
        assertNotNull(cls);

        KotlinNumberOfChildrenVisitor noc = new KotlinNumberOfChildrenVisitor();
        noc.visitClass(cls);

        // OpenClass has 1 child: FinalClass
        assertEquals("NOC should be 1 for OpenClass", 1.0, noc.getMetric().getValue().doubleValue(), 0.1);
    }

    public void testDepthOfInheritanceTree() {
        KtClass openClass = findClass("OpenClass");
        assertNotNull(openClass);
        KotlinDepthOfInheritanceTreeVisitor ditOpen = new KotlinDepthOfInheritanceTreeVisitor();
        ditOpen.visitClass(openClass);
        // OpenClass -> Any/Object.
        // In this test environment, resolution of Any/Object results in null
        // superclass, effectively acting as root (Depth 0).
        assertEquals("DIT for OpenClass should be 0 (Root behavior in test)", 0.0,
                ditOpen.getMetric().getValue().doubleValue(), 0.1);

        KtClass finalClass = findClass("FinalClass");
        assertNotNull(finalClass);
        KotlinDepthOfInheritanceTreeVisitor ditFinal = new KotlinDepthOfInheritanceTreeVisitor();
        ditFinal.visitClass(finalClass);
        // FinalClass -> OpenClass -> Root. Depth 1.
        assertEquals("DIT for FinalClass should be 1", 1.0, ditFinal.getMetric().getValue().doubleValue(), 0.1);
    }

    public void testCouplingBetweenObjects() {
        KtClass cls = findClass("CboCheck");
        assertNotNull(cls);

        KotlinCouplingBetweenObjectsVisitor cbo = new KotlinCouplingBetweenObjectsVisitor();
        cbo.visitClass(cls);

        // Referenced types:
        // java.lang.String -> Ignored (standard)
        // CboTarget1 -> Counted (1)
        // Expected Value: 1.0
        assertEquals("CBO should resolve types and exclude standard libraries", 1.0,
                cbo.getMetric().getValue().doubleValue(), 0.1);
    }

    public void testShadowCheckLCOM() {
        KtClass cls = findClass("ShadowCheck");
        assertNotNull(cls);

        KotlinLackOfCohesionOfMethodsVisitor lcom = new KotlinLackOfCohesionOfMethodsVisitor();
        lcom.visitClass(cls);

        // method1 uses local x, not instance x -> isolated.
        // method2 uses instance x.
        // getX (implicit) uses instance x.
        // Component 1: method2, getX (share x)
        // Component 2: method1 (no shared fields)
        // Expected LCOM: 2
        assertEquals("LCOM should account for shadowing", 2.0, lcom.getMetric().getValue().doubleValue(), 0.1);
    }

    public void testMessagePassingCoupling() {
        KtClass cls = findClass("MpcCheck");
        assertNotNull(cls);

        KotlinMessagePassingCouplingVisitor mpc = new KotlinMessagePassingCouplingVisitor();
        mpc.visitClass(cls);

        // m():
        // MpcTarget() -> 1
        // t.foo() -> 1
        // this.m() -> 0 (self)
        // print("s") -> 0 (standard lib)
        // Total: 2.0

        assertEquals("MPC should resolve calls and exclude self/standard", 2.0,
                mpc.getMetric().getValue().doubleValue(), 0.1);
    }

    public void testAccessToForeignData() {
        KtClass cls = findClass("AtfdCheck");
        assertNotNull(cls);

        KotlinAccessToForeignDataVisitor atfd = new KotlinAccessToForeignDataVisitor();
        atfd.visitClass(cls);

        // foreign.property -> Counted (AtfdForeign)
        // foreign.property= -> Counted (AtfdForeign, same class)
        // foreign.getAccessor() -> Counted (AtfdForeign, same class)
        // foreign.setAccessor(3) -> Counted (AtfdForeign, same class)
        // foreign.behavior() -> Not counted (behavior)
        // behaviorOnly.doAction() -> Not counted
        // Distinct classes: 1 (AtfdForeign)
        assertEquals("ATFD should count accesses to foreign attributes/accessors", 1.0,
                atfd.getMetric().getValue().doubleValue(), 0.1);
    }

    public void testDataAbstractionCoupling() {
        KtClass cls = findClass("DacCheck");
        assertNotNull(cls);

        KotlinDataAbstractionCouplingVisitor dac = new KotlinDataAbstractionCouplingVisitor();
        dac.visitClass(cls);

        // p1: DacTarget1 -> 1
        // p2: Int -> 0 (builtin)
        // p3: List<DacTarget2> -> List (builtin), DacTarget2 -> 1
        // p4: DacCheck -> 0 (self)
        // p5: String -> 0 (builtin)
        // p6: test.pkg.List -> 1 (custom List)
        // Total: 3
        assertEquals("DAC should count distinctive user types in properties", 3.0,
                dac.getMetric().getValue().doubleValue(), 0.1);
    }
}
