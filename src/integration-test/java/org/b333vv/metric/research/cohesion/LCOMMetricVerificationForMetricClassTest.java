package org.b333vv.metric.research.cohesion;

import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.value.Value;
import org.b333vv.metric.research.MetricVerificationTest;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verification test for LCOM on org.b333vv.metric.model.metric.Metric.
 *
 * After aligning JavaParser to exclude boilerplate methods, PSI and JavaParser
 * should now report the same LCOM value for this class.
 */
public class LCOMMetricVerificationForMetricClassTest extends MetricVerificationTest {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        // Load the real source of org.b333vv.metric.model.metric.Metric from the project sources
        Path projectDir = Paths.get(System.getProperty("user.dir"));
        Path sourcePath = projectDir.resolve(Paths.get(
                "src", "main", "java", "org", "b333vv", "metric", "model", "metric", "Metric.java"));
        String realMetricClassContent = Files.readString(sourcePath, StandardCharsets.UTF_8);

        // Add the real class into the temp project so PSI/JavaParser can process it
        myFixture.addFileToProject("org/b333vv/metric/model/metric/Metric.java", realMetricClassContent);

        // Configure with an existing test file to trigger full PSI/JavaParser pipeline
        setupTest("com/verification/cohesion/LCOMTestCases.java");
    }

    public void testLCOM_For_Metric() {
        String className = "Metric"; // simple name: org.b333vv.metric.model.metric.Metric

        var psiValue = getPsiValue(className, MetricType.LCOM);
        System.out.println("[LCOM][PSI] class=" + className + ", actual=" + psiValue);
        if (psiValue != null && psiValue != Value.UNDEFINED) {
            // no hardcoded expected; we just assert parity with JavaParser below
        } else {
            fail("PSI LCOM value for " + className + " should not be null or undefined");
        }

        var javaParserValue = getJavaParserValue(className, MetricType.LCOM);
        System.out.println("[LCOM][JavaParser] class=" + className + ", actual=" + javaParserValue);
        if (javaParserValue != null && javaParserValue != Value.UNDEFINED) {
            org.junit.jupiter.api.Assertions.assertEquals(
                    psiValue.longValue(),
                    javaParserValue.longValue(),
                    "PSI and JavaParser LCOM should match for " + className
            );
        } else {
            fail("JavaParser LCOM value for " + className + " should not be null or undefined");
        }
    }
}
