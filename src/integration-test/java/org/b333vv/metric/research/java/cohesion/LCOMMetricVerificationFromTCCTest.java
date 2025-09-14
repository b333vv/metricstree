package org.b333vv.metric.research.java.cohesion;

import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.value.Value;
import org.b333vv.metric.research.java.MetricVerificationTest;

/**
 * Verification tests for LCOM using classes from TCCTestCases.java
 * Expected values are computed as the number of connected components
 * among methods that access at least one instance field.
 */
public class LCOMMetricVerificationFromTCCTest extends MetricVerificationTest {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        // Use the TCC testcases file as the source under test
        setupTest("com/verification/cohesion/TCCTestCases.java");
    }

    public void testLCOM_TCC_TestClass() {
        // Methods using fields:
        //  - {methodA1, methodA2, bridgeMethod, methodB1, methodB2} are connected via fieldA/fieldB through bridgeMethod
        //  - {isolatedMethod} uses only fieldC (isolated)
        //  - {methodD} uses only fieldD (isolated)
        //  - noFieldMethod uses no fields and is ignored
        long expected = 3L;
        assertLcomEquals("TCC_TestClass", expected);
    }

    public void testLCOM_PerfectCohesion_TestClass() {
        // All methods share sharedField => one component
        long expected = 1L;
        assertLcomEquals("PerfectCohesion_TestClass", expected);
    }

    public void testLCOM_NoCohesion_TestClass() {
        // Each method uses its own field => three isolated components
        long expected = 3L;
        assertLcomEquals("NoCohesion_TestClass", expected);
    }

    public void testLCOM_SingleMethod_TestClass() {
        // Single method accessing a field => one component
        long expected = 1L;
        assertLcomEquals("SingleMethod_TestClass", expected);
    }

    private void assertLcomEquals(String className, long expectedLcom) {
        var psiValue = getPsiValue(className, MetricType.LCOM);
        System.out.println("[LCOM][PSI] class=" + className + ", expected=" + expectedLcom + ", actual=" + psiValue);
        if (psiValue != null && psiValue != Value.UNDEFINED) {
            assertEquals(expectedLcom, psiValue.longValue());
        } else {
            fail("PSI LCOM value for " + className + " should not be null or undefined");
        }

        var javaParserValue = getJavaParserValue(className, MetricType.LCOM);
        System.out.println("[LCOM][JavaParser] class=" + className + ", expected=" + expectedLcom + ", actual=" + javaParserValue);
        if (javaParserValue != null && javaParserValue != Value.UNDEFINED) {
            assertEquals(expectedLcom, javaParserValue.longValue());
        } else {
            fail("JavaParser LCOM value for " + className + " should not be null or undefined");
        }
    }
}
