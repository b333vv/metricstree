package org.b333vv.metric.research.coupling;

import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.value.Value;
import org.b333vv.metric.research.MetricVerificationTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class CBOAlignmentVerificationTest extends MetricVerificationTest {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        // Create two separate files for the test
        myFixture.addFileToProject("com/test/ClassA.java",
            "package com.test; public class ClassA { private ClassB b; }");
        myFixture.addFileToProject("com/test/ClassB.java",
            "package com.test; public class ClassB { }");

        // Use a main file to trigger the analysis scope correctly
        setupTest("com/test/ClassA.java");
    }

    public void testCBOValuesAreAlignedAfterRefactoring() {
        // Get the CBO metric for ClassA, which is coupled to ClassB
        Value psiValue = getPsiValue("ClassA", MetricType.CBO);
        Value javaParserValue = getJavaParserValue("ClassA", MetricType.CBO);

        System.out.println("CBO Alignment Test for ClassA:");
        System.out.println("PSI Value: " + psiValue);
        System.out.println("JavaParser Value: " + javaParserValue);

        // Before the fix, javaParserValue would likely be 0 or UNDEFINED.
        // After the fix, it should correctly identify the coupling and match the PSI value.
        assertNotEquals(Value.UNDEFINED, psiValue, "PSI CBO should be calculated.");
        assertNotEquals(Value.UNDEFINED, javaParserValue, "JavaParser CBO should be calculated.");
        
        // The core assertion: both engines must now produce the same result.
        assertEquals("PSI and JavaParser CBO values must be equal after the fix.", psiValue.longValue(), javaParserValue.longValue());
    }
}