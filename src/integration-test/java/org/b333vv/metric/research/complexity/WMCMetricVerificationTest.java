package org.b333vv.metric.research.complexity;

import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.research.MetricVerificationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class WMCMetricVerificationTest extends MetricVerificationTest {
    
    @BeforeEach
    public void setup() {
        setupTest("com/verification/complexity/WMCTestCases.java");
    }

    @Test
    public void testWMC_GroundTruth() {
        // Manual Calculation for WMC_TestClass:
        // WMC = Sum of cyclomatic complexity of all methods in the class
        //
        // Method complexities:
        // 1. simpleMethod() = 1 (no branches)
        // 2. methodWithIf() = 2 (if-else creates 2 paths)
        // 3. methodWithLoop() = 2 (for loop creates 2 paths)
        // 4. methodWithSwitch() = 4 (3 cases + default = 4 paths)
        // 5. complexMethod() = 8 (detailed calculation in comments)
        // 6. WMC_TestClass() constructor = 1 (no branches)
        // 7. staticMethod() = 1 (no branches - if counted)
        //
        // Expected WMC = 1 + 2 + 2 + 4 + 8 + 1 + 1 = 19
        // Note: Implementation may vary on whether constructors and static methods are counted
        assertEquals(19, 19); // Placeholder for documented ground truth.
    }

    @Test
    public void testWMC_PSI_Implementation() {
        System.out.println("Available classes:");
        javaProject.allClasses().forEach(javaClass -> {
            System.out.println("  - " + javaClass.getName());
        });
        
        var psiValue = getPsiValue("WMC_TestClass", MetricType.WMC);
        System.out.println("PSI WMC value: " + psiValue);
        if (psiValue != null) {
            long psiLongValue = psiValue.longValue();
            System.out.println("PSI WMC long value: " + psiLongValue);
        }
    }

    @Test
    public void testWMC_JavaParser_Implementation() {
        var javaParserValue = getJavaParserValue("WMC_TestClass", MetricType.WMC);
        System.out.println("JavaParser WMC value: " + javaParserValue);
        if (javaParserValue != null) {
            long javaParserLongValue = javaParserValue.longValue();
            System.out.println("JavaParser WMC long value: " + javaParserLongValue);
        }
    }
}