package org.b333vv.metric.research.java.cohesion;

import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.value.Value;
import org.b333vv.metric.research.java.MetricVerificationTest;

/**
 * Verification tests for LCOM (Lack of Cohesion of Methods) metric.
 * Tests both PSI and JavaParser implementations against documented ground truth values.
 */
public class LCOMMetricVerificationTest extends MetricVerificationTest {
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        setupTest("com/verification/cohesion/LCOMTestCases.java");
    }

    public void testLCOM_PerfectCohesion() {
        // Expected LCOM: 1 (all methods share the same field)
        long expectedLcom = 1;
        
        var psiValue = getPsiValue("LCOM_PerfectCohesion", MetricType.LCOM);
        if (psiValue != null && psiValue != Value.UNDEFINED) {
            assertEquals(expectedLcom, psiValue.longValue());
        } else {
            fail("PSI LCOM value for LCOM_PerfectCohesion should not be null or undefined");
        }

        var javaParserValue = getJavaParserValue("LCOM_PerfectCohesion", MetricType.LCOM);
        if (javaParserValue != null && javaParserValue != Value.UNDEFINED) {
            assertEquals(expectedLcom, javaParserValue.longValue());
        } else {
            fail("JavaParser LCOM value for LCOM_PerfectCohesion should not be null or undefined");
        }
    }
    
    public void testLCOM_NoCohesion() {
        // Expected LCOM: 4 (4 disconnected components using different fields)
        long expectedLcom = 4;
        
        var psiValue = getPsiValue("LCOM_NoCohesion", MetricType.LCOM);
        if (psiValue != null && psiValue != Value.UNDEFINED) {
            assertEquals(expectedLcom, psiValue.longValue());
        } else {
            fail("PSI LCOM value for LCOM_NoCohesion should not be null or undefined");
        }

        var javaParserValue = getJavaParserValue("LCOM_NoCohesion", MetricType.LCOM);
        if (javaParserValue != null && javaParserValue != Value.UNDEFINED) {
            assertEquals(expectedLcom, javaParserValue.longValue());
        } else {
            fail("JavaParser LCOM value for LCOM_NoCohesion should not be null or undefined");
        }
    }
    
    public void testLCOM_PartialCohesion() {
        // Expected LCOM: 2 (two disconnected components)
        long expectedLcom = 2;
        
        var psiValue = getPsiValue("LCOM_PartialCohesion", MetricType.LCOM);
        if (psiValue != null && psiValue != Value.UNDEFINED) {
            assertEquals(expectedLcom, psiValue.longValue());
        } else {
            fail("PSI LCOM value for LCOM_PartialCohesion should not be null or undefined");
        }

        var javaParserValue = getJavaParserValue("LCOM_PartialCohesion", MetricType.LCOM);
        if (javaParserValue != null && javaParserValue != Value.UNDEFINED) {
            assertEquals(expectedLcom, javaParserValue.longValue());
        } else {
            fail("JavaParser LCOM value for LCOM_PartialCohesion should not be null or undefined");
        }
    }
    
    public void testLCOM_ComplexSharing() {
        // Expected LCOM: 2 (methods connected through field sharing chain)
        long expectedLcom = 2;
        
        var psiValue = getPsiValue("LCOM_ComplexSharing", MetricType.LCOM);
        if (psiValue != null && psiValue != Value.UNDEFINED) {
            assertEquals(expectedLcom, psiValue.longValue());
        } else {
            fail("PSI LCOM value for LCOM_ComplexSharing should not be null or undefined");
        }

        var javaParserValue = getJavaParserValue("LCOM_ComplexSharing", MetricType.LCOM);
        if (javaParserValue != null && javaParserValue != Value.UNDEFINED) {
            assertEquals(expectedLcom, javaParserValue.longValue());
        } else {
            fail("JavaParser LCOM value for LCOM_ComplexSharing should not be null or undefined");
        }
    }
    
    public void testLCOM_StaticOnly() {
        // Expected LCOM: 0 (no instance methods using instance fields)
        long expectedLcom = 0;
        
        var psiValue = getPsiValue("LCOM_StaticOnly", MetricType.LCOM);
        if (psiValue != null && psiValue != Value.UNDEFINED) {
            assertEquals(expectedLcom, psiValue.longValue());
        } else {
            fail("PSI LCOM value for LCOM_StaticOnly should not be null or undefined");
        }

        var javaParserValue = getJavaParserValue("LCOM_StaticOnly", MetricType.LCOM);
        if (javaParserValue != null && javaParserValue != Value.UNDEFINED) {
            assertEquals(expectedLcom, javaParserValue.longValue());
        } else {
            fail("JavaParser LCOM value for LCOM_StaticOnly should not be null or undefined");
        }
    }
    
    public void testLCOM_NoFieldAccess() {
        // Expected LCOM: 0 (no field access)
        long expectedLcom = 0;
        
        var psiValue = getPsiValue("LCOM_NoFieldAccess", MetricType.LCOM);
        if (psiValue != null && psiValue != Value.UNDEFINED) {
            assertEquals(expectedLcom, psiValue.longValue());
        } else {
            fail("PSI LCOM value for LCOM_NoFieldAccess should not be null or undefined");
        }

        var javaParserValue = getJavaParserValue("LCOM_NoFieldAccess", MetricType.LCOM);
        if (javaParserValue != null && javaParserValue != Value.UNDEFINED) {
            assertEquals(expectedLcom, javaParserValue.longValue());
        } else {
            fail("JavaParser LCOM value for LCOM_NoFieldAccess should not be null or undefined");
        }
    }
    
    public void testLCOM_Empty() {
        // Expected LCOM: 0 (no methods, no fields)
        long expectedLcom = 0;
        
        var psiValue = getPsiValue("LCOM_Empty", MetricType.LCOM);
        if (psiValue != null && psiValue != Value.UNDEFINED) {
            assertEquals(expectedLcom, psiValue.longValue());
        } else {
            fail("PSI LCOM value for LCOM_Empty should not be null or undefined");
        }

        var javaParserValue = getJavaParserValue("LCOM_Empty", MetricType.LCOM);
        if (javaParserValue != null && javaParserValue != Value.UNDEFINED) {
            assertEquals(expectedLcom, javaParserValue.longValue());
        } else {
            fail("JavaParser LCOM value for LCOM_Empty should not be null or undefined");
        }
    }
    
    public void testLCOM_ChildClass() {
        // Expected LCOM: 1 (all methods connected through field sharing)
        long expectedLcom = 1;
        
        var psiValue = getPsiValue("LCOM_ChildClass", MetricType.LCOM);
        if (psiValue != null && psiValue != Value.UNDEFINED) {
            assertEquals(expectedLcom, psiValue.longValue());
        } else {
            fail("PSI LCOM value for LCOM_ChildClass should not be null or undefined");
        }

        var javaParserValue = getJavaParserValue("LCOM_ChildClass", MetricType.LCOM);
        if (javaParserValue != null && javaParserValue != Value.UNDEFINED) {
            assertEquals(expectedLcom, javaParserValue.longValue());
        } else {
            fail("JavaParser LCOM value for LCOM_ChildClass should not be null or undefined");
        }
    }
    
    public void testLCOM_ReadOnlyAccess() {
        // Expected LCOM: 1 (all methods connected through field2)
        long expectedLcom = 1;
        
        var psiValue = getPsiValue("LCOM_ReadOnlyAccess", MetricType.LCOM);
        if (psiValue != null && psiValue != Value.UNDEFINED) {
            assertEquals(expectedLcom, psiValue.longValue());
        } else {
            fail("PSI LCOM value for LCOM_ReadOnlyAccess should not be null or undefined");
        }

        var javaParserValue = getJavaParserValue("LCOM_ReadOnlyAccess", MetricType.LCOM);
        if (javaParserValue != null && javaParserValue != Value.UNDEFINED) {
            assertEquals(expectedLcom, javaParserValue.longValue());
        } else {
            fail("JavaParser LCOM value for LCOM_ReadOnlyAccess should not be null or undefined");
        }
    }
    
    public void testLCOM_MixedAccess() {
        // Expected LCOM: 2 (two disconnected components)
        long expectedLcom = 2;
        
        var psiValue = getPsiValue("LCOM_MixedAccess", MetricType.LCOM);
        if (psiValue != null && psiValue != Value.UNDEFINED) {
            assertEquals(expectedLcom, psiValue.longValue());
        } else {
            fail("PSI LCOM value for LCOM_MixedAccess should not be null or undefined");
        }

        var javaParserValue = getJavaParserValue("LCOM_MixedAccess", MetricType.LCOM);
        if (javaParserValue != null && javaParserValue != Value.UNDEFINED) {
            assertEquals(expectedLcom, javaParserValue.longValue());
        } else {
            fail("JavaParser LCOM value for LCOM_MixedAccess should not be null or undefined");
        }
    }
    
    public void testLCOM_GroundTruth_Summary() {
        // Summary test to display all available classes for debugging
        System.out.println("Available classes:");
        javaProject.allClasses().forEach(javaClass -> {
            System.out.println("  - " + javaClass.getName());
        });
        
        // List some sample PSI and JavaParser values for debugging
        System.out.println("\nSample metric values:");
        var psiValue = getPsiValue("LCOM_PerfectCohesion", MetricType.LCOM);
        var javaParserValue = getJavaParserValue("LCOM_PerfectCohesion", MetricType.LCOM);
        System.out.println("LCOM_PerfectCohesion - PSI: " + psiValue + ", JavaParser: " + javaParserValue);
        
        psiValue = getPsiValue("LCOM_PartialCohesion", MetricType.LCOM);
        javaParserValue = getJavaParserValue("LCOM_PartialCohesion", MetricType.LCOM);
        System.out.println("LCOM_PartialCohesion - PSI: " + psiValue + ", JavaParser: " + javaParserValue);
    }
}