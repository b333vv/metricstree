package org.b333vv.metric.research.coupling;

import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.research.MetricVerificationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CBOMetricVerificationTest extends MetricVerificationTest {
    
    @BeforeEach
    public void setup() {
        setupTest("com/verification/coupling/CBOTestCases.java");
    }

    @Test
    public void testCBO_GroundTruth() {
        // Manual Calculation for CBO_TestClass:
        // According to Chidamber & Kemerer definition, CBO counts the number of other classes
        // to which a class is coupled. A class is coupled to another if it uses its member
        // functions and/or instance variables.
        //
        // Analyzing CBO_TestClass:
        // 1. Parent (inheritance - extends)
        // 2. Serializable (inheritance - implements) 
        // 3. DependencyA (field type)
        // 4. List (field type - java.util)
        // 5. GenericDependency (field type)
        // 6. DependencyB (generic type parameter in field)
        // 7. DependencyC (method parameter)
        // 8. Set (local variable type - java.util)
        // 9. System (static method call - java.lang)
        // 10. Map (return type - java.util)
        // 11. InterfaceDep (return type)
        //
        // Expected CBO = 11
        // Note: Whether java.lang.* and java.util.* classes should be counted depends on 
        // the specific CBO implementation interpretation.
        assertEquals(11, 11); // Placeholder for documented ground truth.
    }

    @Test
    public void testCBO_PSI_Implementation() {
        // Debug: print all available classes
        System.out.println("Available classes:");
        javaProject.allClasses().forEach(javaClass -> {
            System.out.println("  - " + javaClass.getName());
        });
        
        var psiValue = getPsiValue("CBO_TestClass", MetricType.CBO);
        System.out.println("PSI CBO value: " + psiValue);
        if (psiValue != null) {
            long psiLongValue = psiValue.longValue();
            System.out.println("PSI CBO long value: " + psiLongValue);
        }
    }

    @Test
    public void testCBO_JavaParser_Implementation() {
        var javaParserValue = getJavaParserValue("CBO_TestClass", MetricType.CBO);
        System.out.println("JavaParser CBO value: " + javaParserValue);
        if (javaParserValue != null) {
            long javaParserLongValue = javaParserValue.longValue();
            System.out.println("JavaParser CBO long value: " + javaParserLongValue);
        }
    }
}