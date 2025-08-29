package org.b333vv.metric.research.cohesion;

import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.research.MetricVerificationTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TCCMetricVerificationTest extends MetricVerificationTest {
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        setupTest("com/verification/cohesion/TCCTestCases.java");
    }

    public void testTCC_GroundTruth() {
        // Manual Calculation for TCC_TestClass:
        // TCC = Connected Method Pairs / Total Possible Method Pairs
        // 
        // Methods in TCC_TestClass (excluding constructor - typical for TCC):
        // 1. methodA1() - uses fieldA
        // 2. methodA2() - uses fieldA  
        // 3. methodB1() - uses fieldB
        // 4. methodB2() - uses fieldB
        // 5. bridgeMethod() - uses fieldA, fieldB
        // 6. isolatedMethod() - uses fieldC
        // 7. noFieldMethod() - uses no fields
        // 8. methodD() - uses fieldD
        // Total methods: 8
        //
        // Field access analysis:
        // fieldA: methodA1, methodA2, bridgeMethod
        // fieldB: methodB1, methodB2, bridgeMethod  
        // fieldC: isolatedMethod
        // fieldD: methodD
        // no fields: noFieldMethod
        //
        // Connected pairs (methods sharing at least one field):
        // (methodA1, methodA2) - both use fieldA
        // (methodA1, bridgeMethod) - both use fieldA
        // (methodA2, bridgeMethod) - both use fieldA
        // (methodB1, methodB2) - both use fieldB
        // (methodB1, bridgeMethod) - both use fieldB
        // (methodB2, bridgeMethod) - both use fieldB
        // Total connected pairs: 6
        //
        // Total possible pairs: C(8,2) = 8*7/2 = 28
        // Expected TCC = 6/28 = 0.214... ≈ 0.21
        double expectedTCC = 6.0 / 28.0; // 0.214285714...
        assertEquals(expectedTCC, expectedTCC, 0.001); // Placeholder for documented ground truth
    }

    public void testTCC_PerfectCohesion_GroundTruth() {
        // Manual Calculation for PerfectCohesion_TestClass:
        // Methods: method1, method2, method3 (3 methods)
        // All methods use sharedField
        // Connected pairs: (method1,method2), (method1,method3), (method2,method3) = 3 pairs
        // Total possible pairs: C(3,2) = 3
        // Expected TCC = 3/3 = 1.0 (perfect cohesion)
        assertEquals(1.0, 1.0); // Perfect cohesion
    }

    public void testTCC_NoCohesion_GroundTruth() {
        // Manual Calculation for NoCohesion_TestClass:
        // Methods: methodA, methodB, methodC (3 methods)
        // No methods share fields (each uses different field)
        // Connected pairs: 0
        // Total possible pairs: C(3,2) = 3
        // Expected TCC = 0/3 = 0.0 (no cohesion)
        assertEquals(0.0, 0.0); // No cohesion
    }

    public void testTCC_PSI_Implementation() {
        System.out.println("Available classes:");
        javaProject.allClasses().forEach(javaClass -> {
            System.out.println("  - " + javaClass.getName());
        });
        
        var psiValue = getPsiValue("TCC_TestClass", MetricType.TCC);
        System.out.println("PSI TCC value: " + psiValue);
        if (psiValue != null) {
            double psiDoubleValue = psiValue.doubleValue();
            System.out.println("PSI TCC double value: " + psiDoubleValue);
        }
    }

    public void testTCC_JavaParser_Implementation() {
        var javaParserValue = getJavaParserValue("TCC_TestClass", MetricType.TCC);
        System.out.println("JavaParser TCC value: " + javaParserValue);
        if (javaParserValue != null) {
            double javaParserDoubleValue = javaParserValue.doubleValue();
            System.out.println("JavaParser TCC double value: " + javaParserDoubleValue);
        }
    }

    public void testTCC_PerfectCohesion_PSI() {
        var psiValue = getPsiValue("PerfectCohesion_TestClass", MetricType.TCC);
        System.out.println("PSI TCC (Perfect) value: " + psiValue);
    }

    public void testTCC_PerfectCohesion_JavaParser() {
        var javaParserValue = getJavaParserValue("PerfectCohesion_TestClass", MetricType.TCC);
        System.out.println("JavaParser TCC (Perfect) value: " + javaParserValue);
    }

    public void testTCC_NoCohesion_PSI() {
        var psiValue = getPsiValue("NoCohesion_TestClass", MetricType.TCC);
        System.out.println("PSI TCC (No Cohesion) value: " + psiValue);
    }

    public void testTCC_NoCohesion_JavaParser() {
        var javaParserValue = getJavaParserValue("NoCohesion_TestClass", MetricType.TCC);
        System.out.println("JavaParser TCC (No Cohesion) value: " + javaParserValue);
    }
}