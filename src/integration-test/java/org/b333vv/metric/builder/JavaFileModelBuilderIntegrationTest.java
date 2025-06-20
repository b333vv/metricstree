package org.b333vv.metric.builder;

import com.intellij.psi.PsiJavaFile;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.b333vv.metric.model.code.JavaClass;
import org.b333vv.metric.model.code.JavaFile;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.value.Value;
import org.b333vv.metric.util.MetricsUtils;

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
        MetricsUtils.setCurrentProject(getProject());

        myFixture.configureByText("TestFile.java", TEST_FILE_STRING);
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
        assertNotNull(javaFileModel, "JavaFile model should not be null.");
        assertEquals("TestFile.java", javaFileModel.getName(), "JavaFile name mismatch.");
        // Expecting PrimaryClass and PackagePrivateClass as top-level classes
        assertEquals(2, javaFileModel.classes().count(), "Number of top-level classes mismatch.");
    }

    public void testPrimaryPublicClassModel() {
        assertNotNull(primaryClassModel, "PrimaryClass model should not be null.");
        assertEquals("PrimaryClass", primaryClassModel.getName(), "PrimaryClass name mismatch.");
        // Constructor + primaryMethod = 2 methods
        assertEquals(2, primaryClassModel.methods().count(), "PrimaryClass method count mismatch.");
        assertEquals(1, primaryClassModel.innerClasses().count(), "PrimaryClass inner class count mismatch.");

        Metric nomMetric = primaryClassModel.metric(MetricType.NOM);
        assertNotNull(nomMetric, "NOM metric for PrimaryClass should not be null.");
        assertEquals(Value.of(2), nomMetric.getValue(), "PrimaryClass NOM value mismatch.");
    }

    public void testInnerStaticClassModel() {
        assertNotNull(innerStaticClassModel, "InnerStaticClass model should not be null.");
        assertEquals("InnerStaticClass", innerStaticClassModel.getName(), "InnerStaticClass name mismatch.");
        // Constructor + innerMethod = 2 methods
        assertEquals(2, innerStaticClassModel.methods().count(), "InnerStaticClass method count mismatch.");

        Metric nomMetric = innerStaticClassModel.metric(MetricType.NOM);
        assertNotNull(nomMetric, "NOM metric for InnerStaticClass should not be null.");
        assertEquals(Value.of(2), nomMetric.getValue(), "InnerStaticClass NOM value mismatch.");
    }

    public void testPackagePrivateClassModel() {
        assertNotNull(packagePrivateClassModel, "PackagePrivateClass model should not be null.");
        assertEquals("PackagePrivateClass", packagePrivateClassModel.getName(), "PackagePrivateClass name mismatch.");
        // Constructor + packageMethod = 2 methods
        assertEquals(2, packagePrivateClassModel.methods().count(), "PackagePrivateClass method count mismatch.");

        Metric nomMetric = packagePrivateClassModel.metric(MetricType.NOM);
        assertNotNull(nomMetric, "NOM metric for PackagePrivateClass should not be null.");
        assertEquals(Value.of(2), nomMetric.getValue(), "PackagePrivateClass NOM value mismatch.");
    }
}
