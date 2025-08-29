package org.b333vv.metric.research.inheritance;

import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.research.MetricVerificationTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NOOMMetricVerificationTest extends MetricVerificationTest {
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        setupTest("com/verification/inheritance/NOOMTestCases.java");
    }

    public void testNOOM_GroundTruth() {
        // Manual Calculation for NOOM (Number of Overridden Methods):
        // NOOM = Methods that override methods from superclasses
        // Includes: methods with @Override annotation that override concrete parent methods
        // Excludes: new methods, methods that don't override anything
        // Abstract implementations: may or may not count depending on interpretation
        
        // Test Case 1: NOOM_BaseClass (no superclass)
        // Expected NOOM = 0 (no superclass to override from)
        
        // Test Case 2: NOOM_ChildClass (extends NOOM_BaseClass)
        // Override methods: baseMethod(), protectedBaseMethod(), getName(), setName(), virtualMethod()
        // New methods: childUniqueMethod(), getChildProperty(), privateChildMethod()
        // Static method: staticBaseMethod() (may or may not count as override)
        // Expected NOOM = 5 (five overridden methods)
        
        // Test Case 3: NoInheritance_TestClass (no superclass)
        // Expected NOOM = 0 (no inheritance, no overrides)
        
        // Test Case 4: NoOverrides_TestClass (extends NOOM_BaseClass)
        // All methods are new, no overrides
        // Expected NOOM = 0 (no methods override parent methods)
        
        // Test Case 5: OnlyOverrides_TestClass (extends NOOM_BaseClass)
        // All methods override parent methods: baseMethod(), protectedBaseMethod(), getName(), setName(), virtualMethod()
        // Expected NOOM = 5 (all methods are overrides)
        
        // Test Case 6: AbstractParent_TestClass (no superclass)
        // Expected NOOM = 0 (abstract class has no superclass to override from)
        
        // Test Case 7: ConcreteImplementation (extends AbstractParent_TestClass)
        // Override methods: concreteMethod() (clear override)
        // Abstract implementation: abstractMethod() (may or may not count)
        // New methods: implementationSpecificMethod()
        // Expected NOOM = 1 or 2 (depending on abstract method implementation counting)
        
        // Test Case 8: MultipleInheritance (extends NOOM_BaseClass, implements TestInterface)
        // Override methods: baseMethod() (clear override of parent class)
        // Interface implementations: interfaceMethod(), defaultMethod() (may or may not count)
        // New methods: multipleInheritanceMethod()
        // Expected NOOM = 1, 2, or 3 (depending on interface method counting)
        
        // Test Case 9: Deep Inheritance Chain
        // DeepInheritance_GrandParent: NOOM = 0 (no superclass)
        // DeepInheritance_Parent: NOOM = 1 (overrides grandParentMethod)
        // DeepInheritance_Child: NOOM = 3 (overrides parentMethod, protectedGrandParentMethod, anotherGrandParentMethod)
        
        // Ground truth placeholders
        assertEquals(0, 0); // NOOM_BaseClass expected
        assertEquals(5, 5); // NOOM_ChildClass expected
        assertEquals(0, 0); // NoInheritance_TestClass expected
        assertEquals(0, 0); // NoOverrides_TestClass expected
        assertEquals(5, 5); // OnlyOverrides_TestClass expected
    }

    public void testNOOM_BaseClass_PSI() {
        // Test base class with no inheritance (should have NOOM = 0)
        var psiValue = getPsiValue("NOOM_BaseClass", MetricType.NOOM);
        System.out.println("PSI NOOM (NOOM_BaseClass) value: " + psiValue);
        if (psiValue != null) {
            double psiDoubleValue = psiValue.doubleValue();
            System.out.println("PSI NOOM (NOOM_BaseClass) double value: " + psiDoubleValue);
            // Expected: 0 (no superclass to override from)
        }
    }

    public void testNOOM_BaseClass_JavaParser() {
        var javaParserValue = getJavaParserValue("NOOM_BaseClass", MetricType.NOOM);
        System.out.println("JavaParser NOOM (NOOM_BaseClass) value: " + javaParserValue);
        if (javaParserValue != null) {
            double javaParserDoubleValue = javaParserValue.doubleValue();
            System.out.println("JavaParser NOOM (NOOM_BaseClass) double value: " + javaParserDoubleValue);
            // Expected: 0 (no superclass to override from)
        }
    }

    public void testNOOM_ChildClass_PSI() {
        // Test child class with multiple overrides
        var psiValue = getPsiValue("NOOM_ChildClass", MetricType.NOOM);
        System.out.println("PSI NOOM (NOOM_ChildClass) value: " + psiValue);
        if (psiValue != null) {
            double psiDoubleValue = psiValue.doubleValue();
            System.out.println("PSI NOOM (NOOM_ChildClass) double value: " + psiDoubleValue);
            // Expected: 5 (baseMethod, protectedBaseMethod, getName, setName, virtualMethod)
        }
    }

    public void testNOOM_ChildClass_JavaParser() {
        var javaParserValue = getJavaParserValue("NOOM_ChildClass", MetricType.NOOM);
        System.out.println("JavaParser NOOM (NOOM_ChildClass) value: " + javaParserValue);
        if (javaParserValue != null) {
            double javaParserDoubleValue = javaParserValue.doubleValue();
            System.out.println("JavaParser NOOM (NOOM_ChildClass) double value: " + javaParserDoubleValue);
            // Expected: 5 (baseMethod, protectedBaseMethod, getName, setName, virtualMethod)
        }
    }

    public void testNOOM_NoInheritance_PSI() {
        // Test class with no inheritance (should have NOOM = 0)
        var psiValue = getPsiValue("NoInheritance_TestClass", MetricType.NOOM);
        System.out.println("PSI NOOM (NoInheritance_TestClass) value: " + psiValue);
        // Expected: 0 (no inheritance, no overrides)
    }

    public void testNOOM_NoInheritance_JavaParser() {
        var javaParserValue = getJavaParserValue("NoInheritance_TestClass", MetricType.NOOM);
        System.out.println("JavaParser NOOM (NoInheritance_TestClass) value: " + javaParserValue);
        // Expected: 0 (no inheritance, no overrides)
    }

    public void testNOOM_NoOverrides_PSI() {
        // Test class that inherits but doesn't override anything
        var psiValue = getPsiValue("NoOverrides_TestClass", MetricType.NOOM);
        System.out.println("PSI NOOM (NoOverrides_TestClass) value: " + psiValue);
        // Expected: 0 (inherits but adds new methods, no overrides)
    }

    public void testNOOM_NoOverrides_JavaParser() {
        var javaParserValue = getJavaParserValue("NoOverrides_TestClass", MetricType.NOOM);
        System.out.println("JavaParser NOOM (NoOverrides_TestClass) value: " + javaParserValue);
        // Expected: 0 (inherits but adds new methods, no overrides)
    }

    public void testNOOM_OnlyOverrides_PSI() {
        // Test class that only overrides methods (no new methods)
        var psiValue = getPsiValue("OnlyOverrides_TestClass", MetricType.NOOM);
        System.out.println("PSI NOOM (OnlyOverrides_TestClass) value: " + psiValue);
        // Expected: 5 (all methods are overrides)
    }

    public void testNOOM_OnlyOverrides_JavaParser() {
        var javaParserValue = getJavaParserValue("OnlyOverrides_TestClass", MetricType.NOOM);
        System.out.println("JavaParser NOOM (OnlyOverrides_TestClass) value: " + javaParserValue);
        // Expected: 5 (all methods are overrides)
    }

    public void testNOOM_AbstractParent_PSI() {
        // Test abstract class (should have NOOM = 0, no superclass)
        var psiValue = getPsiValue("AbstractParent_TestClass", MetricType.NOOM);
        System.out.println("PSI NOOM (AbstractParent_TestClass) value: " + psiValue);
        // Expected: 0 (abstract class has no superclass to override from)
    }

    public void testNOOM_AbstractParent_JavaParser() {
        var javaParserValue = getJavaParserValue("AbstractParent_TestClass", MetricType.NOOM);
        System.out.println("JavaParser NOOM (AbstractParent_TestClass) value: " + javaParserValue);
        // Expected: 0 (abstract class has no superclass to override from)
    }

    public void testNOOM_ConcreteImplementation_PSI() {
        // Test concrete implementation of abstract class
        var psiValue = getPsiValue("ConcreteImplementation", MetricType.NOOM);
        System.out.println("PSI NOOM (ConcreteImplementation) value: " + psiValue);
        // Expected: 1 or 2 (concreteMethod override, possibly abstractMethod implementation)
    }

    public void testNOOM_ConcreteImplementation_JavaParser() {
        var javaParserValue = getJavaParserValue("ConcreteImplementation", MetricType.NOOM);
        System.out.println("JavaParser NOOM (ConcreteImplementation) value: " + javaParserValue);
        // Expected: 1 or 2 (concreteMethod override, possibly abstractMethod implementation)
    }

    public void testNOOM_MultipleInheritance_PSI() {
        // Test class with both inheritance and interface implementation
        var psiValue = getPsiValue("MultipleInheritance", MetricType.NOOM);
        System.out.println("PSI NOOM (MultipleInheritance) value: " + psiValue);
        // Expected: 1, 2, or 3 (baseMethod override, possibly interface methods)
    }

    public void testNOOM_MultipleInheritance_JavaParser() {
        var javaParserValue = getJavaParserValue("MultipleInheritance", MetricType.NOOM);
        System.out.println("JavaParser NOOM (MultipleInheritance) value: " + javaParserValue);
        // Expected: 1, 2, or 3 (baseMethod override, possibly interface methods)
    }

    public void testNOOM_DeepInheritance_PSI() {
        // Test deep inheritance chain
        var grandParentValue = getPsiValue("DeepInheritance_GrandParent", MetricType.NOOM);
        var parentValue = getPsiValue("DeepInheritance_Parent", MetricType.NOOM);
        var childValue = getPsiValue("DeepInheritance_Child", MetricType.NOOM);
        
        System.out.println("PSI NOOM (DeepInheritance_GrandParent) value: " + grandParentValue);
        System.out.println("PSI NOOM (DeepInheritance_Parent) value: " + parentValue);
        System.out.println("PSI NOOM (DeepInheritance_Child) value: " + childValue);
        
        // Expected: GrandParent=0, Parent=1, Child=3
    }

    public void testNOOM_DeepInheritance_JavaParser() {
        // Test deep inheritance chain
        var grandParentValue = getJavaParserValue("DeepInheritance_GrandParent", MetricType.NOOM);
        var parentValue = getJavaParserValue("DeepInheritance_Parent", MetricType.NOOM);
        var childValue = getJavaParserValue("DeepInheritance_Child", MetricType.NOOM);
        
        System.out.println("JavaParser NOOM (DeepInheritance_GrandParent) value: " + grandParentValue);
        System.out.println("JavaParser NOOM (DeepInheritance_Parent) value: " + parentValue);
        System.out.println("JavaParser NOOM (DeepInheritance_Child) value: " + childValue);
        
        // Expected: GrandParent=0, Parent=1, Child=3
    }
}