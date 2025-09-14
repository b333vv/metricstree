package org.b333vv.metric.research.java.cohesion;

import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.value.Value;
import org.b333vv.metric.research.java.MetricVerificationTest;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Verification test for LCOM on org.b333vv.metric.model.code.JavaClass.
 *
 * LCOM here is computed as the number of connected components among instance methods
 * that access at least one instance field of the class. For JavaClass, the only
 * instance field is `psiClass`, and only the method `getPsiClass()` accesses it
 * directly in source. Thus, the expected LCOM is 1 (a single method forms one
 * component by itself).
 */
public class LCOMMetricVerificationForJavaClassTest extends MetricVerificationTest {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        // Load the real source of org.b333vv.metric.model.code.JavaClass from the project sources
        Path projectDir = Paths.get(System.getProperty("user.dir"));
        Path sourcePath = projectDir.resolve(Paths.get(
                "src", "main", "java", "org", "b333vv", "metric", "model", "code", "JavaClass.java"));
        String realJavaClassContent = Files.readString(sourcePath, StandardCharsets.UTF_8);

        // Add the real class into the temp project so PSI/JavaParser can process it
        myFixture.addFileToProject("org/b333vv/metric/model/code/JavaClass.java", realJavaClassContent);

        // Configure with an existing test file to trigger full PSI/JavaParser pipeline
        setupTest("com/verification/cohesion/LCOMTestCases.java");
    }

    public void testLCOM_For_JavaClass() {
        String className = "JavaClass"; // simple name: org.b333vv.metric.model.code.JavaClass
        long expectedLcom = 1L;

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
