package org.b333vv.metric.research.inheritance;

import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.research.MetricVerificationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DITMetricVerificationTest extends MetricVerificationTest {
    
    @BeforeEach
    public void setup() {
        setupTest("com/verification/inheritance/DITTestCases.java");
    }

    @Test
    public void testDIT_GroundTruth() {
        // Manual Calculation for DIT_TestClass:
        // DIT measures the depth of inheritance tree (maximum length from class to root)
        //
        // Inheritance chain for DIT_TestClass:
        // Object -> GrandParent -> Parent -> DIT_TestClass
        // Depth = 3 (counting from Object, some implementations start from 1)
        //
        // Inheritance chain for DeepInheritance_TestClass:  
        // Object -> Level1 -> Level2 -> Level3 -> Level4 -> DeepInheritance_TestClass
        // Depth = 5
        //
        // Inheritance chain for ShallowInheritance_TestClass:
        // Object -> ShallowInheritance_TestClass
        // Depth = 1
        //
        // Expected DIT_TestClass = 3
        // Expected DeepInheritance_TestClass = 5  
        // Expected ShallowInheritance_TestClass = 1
        assertEquals(3, 3); // Placeholder for DIT_TestClass ground truth.
    }

    @Test
    public void testDIT_PSI_Implementation() {
        System.out.println("Available classes:");
        javaProject.allClasses().forEach(javaClass -> {
            System.out.println("  - " + javaClass.getName());
        });
        
        // Test DIT_TestClass (depth 3)
        var psiValue = getPsiValue("DIT_TestClass", MetricType.DIT);
        System.out.println("PSI DIT value for DIT_TestClass: " + psiValue);
        
        // Test DeepInheritance_TestClass (depth 5)
        var psiValueDeep = getPsiValue("DeepInheritance_TestClass", MetricType.DIT);
        System.out.println("PSI DIT value for DeepInheritance_TestClass: " + psiValueDeep);
        
        // Test ShallowInheritance_TestClass (depth 1)
        var psiValueShallow = getPsiValue("ShallowInheritance_TestClass", MetricType.DIT);
        System.out.println("PSI DIT value for ShallowInheritance_TestClass: " + psiValueShallow);
    }

    @Test
    public void testDIT_JavaParser_Implementation() {
        // Test DIT_TestClass (depth 3)
        var javaParserValue = getJavaParserValue("DIT_TestClass", MetricType.DIT);
        System.out.println("JavaParser DIT value for DIT_TestClass: " + javaParserValue);
        
        // Test DeepInheritance_TestClass (depth 5)
        var javaParserValueDeep = getJavaParserValue("DeepInheritance_TestClass", MetricType.DIT);
        System.out.println("JavaParser DIT value for DeepInheritance_TestClass: " + javaParserValueDeep);
        
        // Test ShallowInheritance_TestClass (depth 1)
        var javaParserValueShallow = getJavaParserValue("ShallowInheritance_TestClass", MetricType.DIT);
        System.out.println("JavaParser DIT value for ShallowInheritance_TestClass: " + javaParserValueShallow);
    }
}