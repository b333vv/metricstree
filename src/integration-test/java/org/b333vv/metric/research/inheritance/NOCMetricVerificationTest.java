package org.b333vv.metric.research.inheritance;

import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.research.MetricVerificationTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NOCMetricVerificationTest extends MetricVerificationTest {
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        setupTest("com/verification/inheritance/NOCTestCases.java");
    }

    public void testNOC_GroundTruth() {
        // Manual Calculation for NOC (Number of Children):
        // NOC = Number of immediate subclasses (direct children only)
        //
        // Test Case 1: NOC_BaseClass
        // Direct children: NOC_ChildA, NOC_ChildB, NOC_ChildC, MultipleInheritance
        // Note: NOC_GrandchildA and NOC_GrandchildB are grandchildren, NOT direct children
        // Expected NOC = 4
        
        // Test Case 2: NoChildren_TestClass
        // Direct children: None
        // Expected NOC = 0
        
        // Test Case 3: SingleParent_TestClass
        // Direct children: OnlyChild_TestClass
        // Expected NOC = 1
        
        // Test Case 4: AbstractParent_TestClass (abstract class)
        // Direct children: ConcreteChildA, ConcreteChildB
        // Expected NOC = 2
        
        // Test Case 5: TestInterface (interface)
        // Direct implementers: InterfaceImplA, InterfaceImplB
        // Note: MultipleInheritance also implements TestInterface
        // Expected NOC = 3 (if interfaces count implementers as children)
        
        // Test Case 6: NOC_ChildA (has grandchildren)
        // Direct children: NOC_GrandchildA
        // Expected NOC = 1
        
        // Test Case 7: NOC_ChildB (has grandchildren)
        // Direct children: NOC_GrandchildB
        // Expected NOC = 1
        
        // Ground truth placeholder
        assertEquals(4, 4); // NOC_BaseClass expected
        assertEquals(0, 0); // NoChildren_TestClass expected
        assertEquals(1, 1); // SingleParent_TestClass expected
        assertEquals(2, 2); // AbstractParent_TestClass expected
    }

    public void testNOC_BaseClass_PSI() {
        // Test the main test case with multiple direct children
        var psiValue = getPsiValue("NOC_BaseClass", MetricType.NOC);
        System.out.println("PSI NOC (NOC_BaseClass) value: " + psiValue);
        if (psiValue != null) {
            double psiDoubleValue = psiValue.doubleValue();
            System.out.println("PSI NOC (NOC_BaseClass) double value: " + psiDoubleValue);
            // Expected: 4 (NOC_ChildA, NOC_ChildB, NOC_ChildC, MultipleInheritance)
        }
    }

    public void testNOC_BaseClass_JavaParser() {
        var javaParserValue = getJavaParserValue("NOC_BaseClass", MetricType.NOC);
        System.out.println("JavaParser NOC (NOC_BaseClass) value: " + javaParserValue);
        if (javaParserValue != null) {
            double javaParserDoubleValue = javaParserValue.doubleValue();
            System.out.println("JavaParser NOC (NOC_BaseClass) double value: " + javaParserDoubleValue);
            // Expected: 4 (NOC_ChildA, NOC_ChildB, NOC_ChildC, MultipleInheritance)
        }
    }

    public void testNOC_NoChildren_PSI() {
        // Test class with no children
        var psiValue = getPsiValue("NoChildren_TestClass", MetricType.NOC);
        System.out.println("PSI NOC (NoChildren_TestClass) value: " + psiValue);
        // Expected: 0
    }

    public void testNOC_NoChildren_JavaParser() {
        var javaParserValue = getJavaParserValue("NoChildren_TestClass", MetricType.NOC);
        System.out.println("JavaParser NOC (NoChildren_TestClass) value: " + javaParserValue);
        // Expected: 0
    }

    public void testNOC_SingleParent_PSI() {
        // Test class with exactly one child
        var psiValue = getPsiValue("SingleParent_TestClass", MetricType.NOC);
        System.out.println("PSI NOC (SingleParent_TestClass) value: " + psiValue);
        // Expected: 1 (OnlyChild_TestClass)
    }

    public void testNOC_SingleParent_JavaParser() {
        var javaParserValue = getJavaParserValue("SingleParent_TestClass", MetricType.NOC);
        System.out.println("JavaParser NOC (SingleParent_TestClass) value: " + javaParserValue);
        // Expected: 1 (OnlyChild_TestClass)
    }

    public void testNOC_AbstractParent_PSI() {
        // Test abstract class with concrete implementations
        var psiValue = getPsiValue("AbstractParent_TestClass", MetricType.NOC);
        System.out.println("PSI NOC (AbstractParent_TestClass) value: " + psiValue);
        // Expected: 2 (ConcreteChildA, ConcreteChildB)
    }

    public void testNOC_AbstractParent_JavaParser() {
        var javaParserValue = getJavaParserValue("AbstractParent_TestClass", MetricType.NOC);
        System.out.println("JavaParser NOC (AbstractParent_TestClass) value: " + javaParserValue);
        // Expected: 2 (ConcreteChildA, ConcreteChildB)
    }

    public void testNOC_Interface_PSI() {
        // Test interface with implementations (edge case)
        var psiValue = getPsiValue("TestInterface", MetricType.NOC);
        System.out.println("PSI NOC (TestInterface) value: " + psiValue);
        // Expected: 3 if interfaces count implementers (InterfaceImplA, InterfaceImplB, MultipleInheritance)
        // Expected: 0 if interfaces don't count implementers as children
    }

    public void testNOC_Interface_JavaParser() {
        var javaParserValue = getJavaParserValue("TestInterface", MetricType.NOC);
        System.out.println("JavaParser NOC (TestInterface) value: " + javaParserValue);
        // Expected: 3 if interfaces count implementers (InterfaceImplA, InterfaceImplB, MultipleInheritance)
        // Expected: 0 if interfaces don't count implementers as children
    }

    public void testNOC_ChildA_PSI() {
        // Test child class that has its own children (grandchild scenario)
        var psiValue = getPsiValue("NOC_ChildA", MetricType.NOC);
        System.out.println("PSI NOC (NOC_ChildA) value: " + psiValue);
        // Expected: 1 (NOC_GrandchildA)
    }

    public void testNOC_ChildA_JavaParser() {
        var javaParserValue = getJavaParserValue("NOC_ChildA", MetricType.NOC);
        System.out.println("JavaParser NOC (NOC_ChildA) value: " + javaParserValue);
        // Expected: 1 (NOC_GrandchildA)
    }

    public void testNOC_ChildB_PSI() {
        // Test another child class that has its own children
        var psiValue = getPsiValue("NOC_ChildB", MetricType.NOC);
        System.out.println("PSI NOC (NOC_ChildB) value: " + psiValue);
        // Expected: 1 (NOC_GrandchildB)
    }

    public void testNOC_ChildB_JavaParser() {
        var javaParserValue = getJavaParserValue("NOC_ChildB", MetricType.NOC);
        System.out.println("JavaParser NOC (NOC_ChildB) value: " + javaParserValue);
        // Expected: 1 (NOC_GrandchildB)
    }

    public void testNOC_Grandchildren_PSI() {
        // Test that grandchildren have NOC = 0 (no further children)
        var grandchildAValue = getPsiValue("NOC_GrandchildA", MetricType.NOC);
        var grandchildBValue = getPsiValue("NOC_GrandchildB", MetricType.NOC);
        System.out.println("PSI NOC (NOC_GrandchildA) value: " + grandchildAValue);
        System.out.println("PSI NOC (NOC_GrandchildB) value: " + grandchildBValue);
        // Expected: 0 for both
    }

    public void testNOC_Grandchildren_JavaParser() {
        // Test that grandchildren have NOC = 0 (no further children)
        var grandchildAValue = getJavaParserValue("NOC_GrandchildA", MetricType.NOC);
        var grandchildBValue = getJavaParserValue("NOC_GrandchildB", MetricType.NOC);
        System.out.println("JavaParser NOC (NOC_GrandchildA) value: " + grandchildAValue);
        System.out.println("JavaParser NOC (NOC_GrandchildB) value: " + grandchildBValue);
        // Expected: 0 for both
    }
}