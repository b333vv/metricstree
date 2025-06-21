package org.b333vv.metric.builder;

import com.intellij.psi.PsiJavaFile;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.b333vv.metric.model.code.JavaClass;
import org.b333vv.metric.model.code.JavaFile;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.value.Value;
// import org.b333vv.metric.util.MetricsUtils; // Removed

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*; // Using JUnit 5 assertions

public class JavaFileModelBuilderIntegrationTest extends BasePlatformTestCase {

    private static final String TEST_FILE_STRING =
            "package com.example;\n" +
            "\n" +
            "public class PrimaryClass {\n" + // 2 methods (incl. constructor), 1 field, 1 inner class
            "    private int primaryField;\n" +
            "\n" +
            "    public PrimaryClass() { this.primaryField = 1; }\n" +
            "\n" +
            "    public void primaryMethod() { System.out.println(\"Primary method\"); }\n" +
            "\n" +
            "    static class InnerStaticClass {\n" + // 2 methods (incl. constructor), 1 field
            "        private static String innerField;\n" +
            "\n" +
            "        public InnerStaticClass() { innerField = \"test\"; }\n" +
            "\n" +
            "        public void innerMethod() { System.out.println(\"Inner method: \" + innerField); }\n" +
            "    }\n" +
            "}\n" +
            "\n" +
            "class PackagePrivateClass {\n" + // 2 methods (incl. constructor), 0 fields
            "    public PackagePrivateClass() {}\n" +
            "\n" +
            "    void packageMethod() { System.out.println(\"Package-private method\"); }\n" +
            "}\n";

    private JavaFile javaFileModel;
    private JavaClass primaryClassModel;
    private JavaClass innerStaticClassModel;
    private JavaClass packagePrivateClassModel;


    @Override
    protected void setUp() throws Exception {
        super.setUp();
        // MetricsUtils.setCurrentProject(getProject()); // Removed

        // Configure with a path that reflects the package structure
        myFixture.configureByText("com/example/TestFile.java", TEST_FILE_STRING);
        PsiJavaFile psiJavaFile = (PsiJavaFile) myFixture.getFile();

        ClassModelBuilder classModelBuilder = new ClassModelBuilder(getProject());
        javaFileModel = classModelBuilder.buildJavaFile(psiJavaFile);

        // Extract models for easier assertion
        List<JavaClass> topLevelClasses = javaFileModel.classes()
                .sorted(Comparator.comparing(jc -> jc.getPsiClass().getName())) // Ensure consistent order
                .collect(Collectors.toList());

        primaryClassModel = topLevelClasses.stream()
                .filter(c -> "PrimaryClass".equals(c.getPsiClass().getName()))
                .findFirst().orElse(null);

        packagePrivateClassModel = topLevelClasses.stream()
                .filter(c -> "PackagePrivateClass".equals(c.getPsiClass().getName()))
                .findFirst().orElse(null);

        if (primaryClassModel != null) {
            innerStaticClassModel = primaryClassModel.innerClasses().findFirst().orElse(null);
        }
    }

    public void testJavaFileModel() {
        assertNotNull("JavaFile model should not be null.", javaFileModel);
        // The name of the JavaFile model might be just "TestFile.java" or the fully qualified path
        // depending on how ClassModelBuilder.buildJavaFile sets it.
        // Assuming it's just the file name for now.
        assertEquals("JavaFile name mismatch.", "TestFile.java", javaFileModel.getName());
        // Expecting PrimaryClass and PackagePrivateClass as top-level classes
        assertEquals("Number of top-level classes mismatch.", 2L, javaFileModel.classes().count());
    }

    public void testPrimaryPublicClassModel() {
        assertNotNull("PrimaryClass model should not be null.", primaryClassModel);
        assertEquals("PrimaryClass name mismatch.", "PrimaryClass", primaryClassModel.getName());
        // Constructor + primaryMethod = 2 methods
        assertEquals("PrimaryClass method count mismatch.", 2L, primaryClassModel.methods().count());
        assertEquals("PrimaryClass inner class count mismatch.", 1L, primaryClassModel.innerClasses().count());

        Metric nomMetric = primaryClassModel.metric(MetricType.NOM);
        assertNotNull("NOM metric for PrimaryClass should not be null.", nomMetric);
        assertEquals("PrimaryClass NOM value mismatch.", Value.of(2), nomMetric.getValue());
    }

    public void testInnerStaticClassModel() {
        assertNotNull("InnerStaticClass model should not be null.", innerStaticClassModel);
        assertEquals("InnerStaticClass name mismatch.", "InnerStaticClass", innerStaticClassModel.getName());
        // Constructor + innerMethod = 2 methods
        assertEquals("InnerStaticClass method count mismatch.", 2L, innerStaticClassModel.methods().count());

        Metric nomMetric = innerStaticClassModel.metric(MetricType.NOM);
        assertNotNull("NOM metric for InnerStaticClass should not be null.", nomMetric);
        assertEquals("InnerStaticClass NOM value mismatch.", Value.of(2), nomMetric.getValue());
    }

    public void testPackagePrivateClassModel() {
        assertNotNull("PackagePrivateClass model should not be null.", packagePrivateClassModel);
        assertEquals("PackagePrivateClass name mismatch.", "PackagePrivateClass", packagePrivateClassModel.getName());
        // Constructor + packageMethod = 2 methods
        assertEquals("PackagePrivateClass method count mismatch.", 2L, packagePrivateClassModel.methods().count());

        Metric nomMetric = packagePrivateClassModel.metric(MetricType.NOM);
        assertNotNull("NOM metric for PackagePrivateClass should not be null.", nomMetric);
        assertEquals("PackagePrivateClass NOM value mismatch.", Value.of(2), nomMetric.getValue());
    }
}
