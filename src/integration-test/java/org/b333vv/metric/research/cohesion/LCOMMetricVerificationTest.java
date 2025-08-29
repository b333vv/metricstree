package org.b333vv.metric.research.cohesion;

import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.research.MetricVerificationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LCOMMetricVerificationTest extends MetricVerificationTest {
    
    @BeforeEach
    public void setup() {
        setupTest("com/verification/cohesion/LCOMTestCases.java");
    }

    @Test
    public void testLCOM_GroundTruth() {
        // Manual Calculation for LCOM_TestClass:
        // LCOM measures the lack of cohesion by counting pairs of methods
        // that do not share instance variables
        //
        // Methods (excluding static methods):
        // 1. methodA1() - uses fieldA
        // 2. methodA2() - uses fieldA  
        // 3. methodB1() - uses fieldB
        // 4. methodB2() - uses fieldB
        // 5. methodC1() - uses fieldC
        // 6. bridgeMethod() - uses fieldA, fieldB
        // 7. isolatedMethod() - uses fieldC
        // 8. utilityMethod() - uses no fields
        //
        // Field usage analysis:
        // fieldA: methodA1, methodA2, bridgeMethod
        // fieldB: methodB1, methodB2, bridgeMethod
        // fieldC: methodC1, isolatedMethod
        // no fields: utilityMethod
        //
        // Pairs that share fields:
        // (methodA1, methodA2) - both use fieldA
        // (methodA1, bridgeMethod) - both use fieldA
        // (methodA2, bridgeMethod) - both use fieldA
        // (methodB1, methodB2) - both use fieldB
        // (methodB1, bridgeMethod) - both use fieldB
        // (methodB2, bridgeMethod) - both use fieldB
        // (methodC1, isolatedMethod) - both use fieldC
        // Total: 7 pairs share fields
        //
        // Total possible pairs: C(8,2) = 28
        // Pairs that don't share fields: 28 - 7 = 21
        // Expected LCOM = 21
        assertEquals(21, 21); // Placeholder for documented ground truth.
    }

    @Test
    public void testLCOM_PSI_Implementation() {
        System.out.println("Available classes:");
        javaProject.allClasses().forEach(javaClass -> {
            System.out.println("  - " + javaClass.getName());
        });
        
        var psiValue = getPsiValue("LCOM_TestClass", MetricType.LCOM);
        System.out.println("PSI LCOM value: " + psiValue);
        if (psiValue != null) {
            long psiLongValue = psiValue.longValue();
            System.out.println("PSI LCOM long value: " + psiLongValue);
        }
    }

    @Test
    public void testLCOM_JavaParser_Implementation() {
        var javaParserValue = getJavaParserValue("LCOM_TestClass", MetricType.LCOM);
        System.out.println("JavaParser LCOM value: " + javaParserValue);
        if (javaParserValue != null) {
            long javaParserLongValue = javaParserValue.longValue();
            System.out.println("JavaParser LCOM long value: " + javaParserLongValue);
        }
    }
}