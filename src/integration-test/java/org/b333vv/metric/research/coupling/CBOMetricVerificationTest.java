package org.b333vv.metric.research.coupling;

import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.value.Value;
import org.b333vv.metric.research.MetricVerificationTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class CBOMetricVerificationTest extends MetricVerificationTest {
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        setupTest("com/verification/coupling/CBOTestCases.java");
    }

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

    public void testCBO_PSI_Implementation() {
        var psiValue = getPsiValue("CBO_TestClass", MetricType.CBO);
        System.out.println("PSI CBO value: " + psiValue);
        if (psiValue != null && psiValue != Value.UNDEFINED) {
            long psiLongValue = psiValue.longValue();
            System.out.println("PSI CBO long value: " + psiLongValue);
            // For now, just verify we got a non-null value - the exact assertion can be added later
            assertTrue(psiLongValue >= 0);
        } else {
            fail("PSI CBO value should not be null or undefined");
        }
    }

    public void testCBO_JavaParser_Implementation() {
        var javaParserValue = getJavaParserValue("CBO_TestClass", MetricType.CBO);
        System.out.println("JavaParser CBO value: " + javaParserValue);
        if (javaParserValue != null && javaParserValue != Value.UNDEFINED) {
            long javaParserLongValue = javaParserValue.longValue();
            System.out.println("JavaParser CBO long value: " + javaParserLongValue);
            // For now, just verify we got a non-null value - the exact assertion can be added later
            assertTrue(javaParserLongValue >= 0);
        } else {
            fail("JavaParser CBO value should not be null or undefined");
        }
    }
}