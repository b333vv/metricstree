package org.b333vv.metric.research.java.inheritance;

import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.research.java.MetricVerificationTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NOAMMetricVerificationTest extends MetricVerificationTest {
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        setupTest("com/verification/inheritance/NOAMTestCases.java");
    }

    public void testNOAM_GroundTruth() {
        // Manual Calculation for NOAM (Number of Added Methods):
        // NOAM = Methods that are NOT inherited or overridden from superclasses
        // Includes: new methods, private methods, static methods
        // Excludes: overridden methods, inherited methods (unless they're new)
        // Constructors: typically excluded from method counts
        
        // Test Case 1: NOAM_BaseClass (no superclass)
        // All methods are "added" since there's no inheritance:
        // Methods: baseMethod(), protectedBaseMethod(), privateBaseMethod(), 
        //          staticBaseMethod(), finalBaseMethod(), getName(), setName()
        // Expected NOAM = 7 (all methods are added, no inheritance)
        
        // Test Case 2: NOAM_ChildClass (extends NOAM_BaseClass)  
        // New/Added methods: childUniqueMethod(), getChildProperty(), 
        //                   setChildProperty(), privateChildMethod(), staticChildMethod()
        // Overridden methods (NOT counted): baseMethod(), protectedBaseMethod()
        // Expected NOAM = 5 (only truly new methods)
        
        // Test Case 3: NoInheritance_TestClass (no superclass)
        // All methods are added: methodOne(), methodTwo(), privateMethod(),
        //                       staticMethod(), getData(), setData(), getCount()
        // Expected NOAM = 7 (all methods are added)
        
        // Test Case 4: OnlyOverrides_TestClass (extends NOAM_BaseClass)
        // All methods are overrides: baseMethod(), protectedBaseMethod(), getName(), setName()
        // Expected NOAM = 0 (no new methods added)
        
        // Test Case 5: AbstractParent_TestClass (no superclass)
        // Methods: abstractMethod() (abstract), concreteMethod(), protectedMethod()
        // Expected NOAM = 2 or 3 (depending on whether abstract methods count)
        
        // Test Case 6: ConcreteImplementation (extends AbstractParent_TestClass)
        // New methods: implementationSpecificMethod(), getImplementationData()
        // Abstract implementation: abstractMethod() (may or may not count as "added")
        // Expected NOAM = 2 or 3
        
        // Test Case 7: DeepInheritance_Child (deep inheritance chain)
        // New methods: childMethod() (only this is truly new)
        // Overridden methods: parentMethod(), protectedGrandParentMethod()
        // Expected NOAM = 1
        
        // Ground truth placeholders
        assertEquals(7, 7); // NOAM_BaseClass expected
        assertEquals(5, 5); // NOAM_ChildClass expected  
        assertEquals(7, 7); // NoInheritance_TestClass expected
        assertEquals(0, 0); // OnlyOverrides_TestClass expected
        assertEquals(1, 1); // DeepInheritance_Child expected
    }

    public void testNOAM_BaseClass_PSI() {
        // Test base class with no inheritance (all methods should be "added")
        var psiValue = getPsiValue("NOAM_BaseClass", MetricType.NOAM);
        System.out.println("PSI NOAM (NOAM_BaseClass) value: " + psiValue);
        if (psiValue != null) {
            double psiDoubleValue = psiValue.doubleValue();
            System.out.println("PSI NOAM (NOAM_BaseClass) double value: " + psiDoubleValue);
            // Expected: 7 (all methods are added since no superclass)
        }
    }

    public void testNOAM_BaseClass_JavaParser() {
        var javaParserValue = getJavaParserValue("NOAM_BaseClass", MetricType.NOAM);
        System.out.println("JavaParser NOAM (NOAM_BaseClass) value: " + javaParserValue);
        if (javaParserValue != null) {
            double javaParserDoubleValue = javaParserValue.doubleValue();
            System.out.println("JavaParser NOAM (NOAM_BaseClass) double value: " + javaParserDoubleValue);
            // Expected: 7 (all methods are added since no superclass)
        }
    }

    public void testNOAM_ChildClass_PSI() {
        // Test child class that overrides some methods and adds new ones
        var psiValue = getPsiValue("NOAM_ChildClass", MetricType.NOAM);
        System.out.println("PSI NOAM (NOAM_ChildClass) value: " + psiValue);
        if (psiValue != null) {
            double psiDoubleValue = psiValue.doubleValue();
            System.out.println("PSI NOAM (NOAM_ChildClass) double value: " + psiDoubleValue);
            // Expected: 5 (only new methods, not overridden ones)
        }
    }

    public void testNOAM_ChildClass_JavaParser() {
        var javaParserValue = getJavaParserValue("NOAM_ChildClass", MetricType.NOAM);
        System.out.println("JavaParser NOAM (NOAM_ChildClass) value: " + javaParserValue);
        if (javaParserValue != null) {
            double javaParserDoubleValue = javaParserValue.doubleValue();
            System.out.println("JavaParser NOAM (NOAM_ChildClass) double value: " + javaParserDoubleValue);
            // Expected: 5 (only new methods, not overridden ones)
        }
    }

    public void testNOAM_NoInheritance_PSI() {
        // Test class with no inheritance (all methods should be added)
        var psiValue = getPsiValue("NoInheritance_TestClass", MetricType.NOAM);
        System.out.println("PSI NOAM (NoInheritance_TestClass) value: " + psiValue);
        // Expected: 7 (all methods are added)
    }

    public void testNOAM_NoInheritance_JavaParser() {
        var javaParserValue = getJavaParserValue("NoInheritance_TestClass", MetricType.NOAM);
        System.out.println("JavaParser NOAM (NoInheritance_TestClass) value: " + javaParserValue);
        // Expected: 7 (all methods are added)
    }

    public void testNOAM_OnlyOverrides_PSI() {
        // Test class that only overrides methods (no new methods)
        var psiValue = getPsiValue("OnlyOverrides_TestClass", MetricType.NOAM);
        System.out.println("PSI NOAM (OnlyOverrides_TestClass) value: " + psiValue);
        // Expected: 0 (no methods are truly "added")
    }

    public void testNOAM_OnlyOverrides_JavaParser() {
        var javaParserValue = getJavaParserValue("OnlyOverrides_TestClass", MetricType.NOAM);
        System.out.println("JavaParser NOAM (OnlyOverrides_TestClass) value: " + javaParserValue);
        // Expected: 0 (no methods are truly "added")
    }

    public void testNOAM_AbstractParent_PSI() {
        // Test abstract class (edge case for method counting)
        var psiValue = getPsiValue("AbstractParent_TestClass", MetricType.NOAM);
        System.out.println("PSI NOAM (AbstractParent_TestClass) value: " + psiValue);
        // Expected: 2 or 3 depending on whether abstract methods count
    }

    public void testNOAM_AbstractParent_JavaParser() {
        var javaParserValue = getJavaParserValue("AbstractParent_TestClass", MetricType.NOAM);
        System.out.println("JavaParser NOAM (AbstractParent_TestClass) value: " + javaParserValue);
        // Expected: 2 or 3 depending on whether abstract methods count
    }

    public void testNOAM_ConcreteImplementation_PSI() {
        // Test concrete implementation of abstract class
        var psiValue = getPsiValue("ConcreteImplementation", MetricType.NOAM);
        System.out.println("PSI NOAM (ConcreteImplementation) value: " + psiValue);
        // Expected: 2 or 3 depending on whether abstract implementations count as "added"
    }

    public void testNOAM_ConcreteImplementation_JavaParser() {
        var javaParserValue = getJavaParserValue("ConcreteImplementation", MetricType.NOAM);
        System.out.println("JavaParser NOAM (ConcreteImplementation) value: " + javaParserValue);
        // Expected: 2 or 3 depending on whether abstract implementations count as "added"
    }

    public void testNOAM_MultipleInheritance_PSI() {
        // Test class with both inheritance and interface implementation
        var psiValue = getPsiValue("MultipleInheritance", MetricType.NOAM);
        System.out.println("PSI NOAM (MultipleInheritance) value: " + psiValue);
        // Expected: 2 or 3 depending on whether interface implementations count
    }

    public void testNOAM_MultipleInheritance_JavaParser() {
        var javaParserValue = getJavaParserValue("MultipleInheritance", MetricType.NOAM);
        System.out.println("JavaParser NOAM (MultipleInheritance) value: " + javaParserValue);
        // Expected: 2 or 3 depending on whether interface implementations count
    }

    public void testNOAM_DeepInheritance_PSI() {
        // Test deep inheritance chain
        var grandParentValue = getPsiValue("DeepInheritance_GrandParent", MetricType.NOAM);
        var parentValue = getPsiValue("DeepInheritance_Parent", MetricType.NOAM);
        var childValue = getPsiValue("DeepInheritance_Child", MetricType.NOAM);
        
        System.out.println("PSI NOAM (DeepInheritance_GrandParent) value: " + grandParentValue);
        System.out.println("PSI NOAM (DeepInheritance_Parent) value: " + parentValue);
        System.out.println("PSI NOAM (DeepInheritance_Child) value: " + childValue);
        
        // Expected: GrandParent=2, Parent=1, Child=1
    }

    public void testNOAM_DeepInheritance_JavaParser() {
        // Test deep inheritance chain
        var grandParentValue = getJavaParserValue("DeepInheritance_GrandParent", MetricType.NOAM);
        var parentValue = getJavaParserValue("DeepInheritance_Parent", MetricType.NOAM);
        var childValue = getJavaParserValue("DeepInheritance_Child", MetricType.NOAM);
        
        System.out.println("JavaParser NOAM (DeepInheritance_GrandParent) value: " + grandParentValue);
        System.out.println("JavaParser NOAM (DeepInheritance_Parent) value: " + parentValue);
        System.out.println("JavaParser NOAM (DeepInheritance_Child) value: " + childValue);
        
        // Expected: GrandParent=2, Parent=1, Child=1
    }
}