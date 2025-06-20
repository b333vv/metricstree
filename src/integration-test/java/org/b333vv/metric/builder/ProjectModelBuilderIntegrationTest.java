package org.b333vv.metric.builder;

import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.b333vv.metric.model.code.JavaClass;
import org.b333vv.metric.model.code.JavaFile;
import org.b333vv.metric.model.code.JavaPackage;
import org.b333vv.metric.model.code.JavaProject;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.util.MetricsUtils; // For MetricsUtils.setCurrentProject if needed by builder internals

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*; // Using JUnit 5 assertions

public class ProjectModelBuilderIntegrationTest extends BasePlatformTestCase {

    private static final String FILE1_STRING =
            "package com.example.pkg1;\n" +
            "public class ClassA {\n" +
            "    public int fieldA = 10;\n" +
            "    public ClassA() {}\n" + // Constructor
            "    public void methodA1() {}\n" +
            "}";

    private static final String FILE2_STRING =
            "package com.example.pkg2;\n" +
            "public class ClassB {\n" +
            "    public ClassB() {}\n" + // Constructor
            "    public void methodB1() {}\n" +
            "    public class InnerB1 {\n" + // Non-static inner class
            "        public InnerB1() {}\n" + // Constructor
            "        public void methodIB1() {}\n" +
            "    }\n" +
            "}";

    private JavaProject javaProject;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        // It's good practice to set current project for any utilities that might rely on it.
        MetricsUtils.setCurrentProject(getProject());

        javaProject = new JavaProject("TestProject");
        // Assuming ProjectModelBuilder itself does not need MetricsUtils.setCurrentProject directly
        // but the underlying ClassModelBuilder might, which is usually called by ProjectModelBuilder.
        ProjectModelBuilder projectModelBuilder = new ProjectModelBuilder(javaProject);

        // Note: configureByText creates files in a light PSI file system.
        // The path provided to configureByText helps IntelliJ understand package structure.
        PsiFile psiFile1 = myFixture.configureByText("src/com/example/pkg1/File1.java", FILE1_STRING);
        PsiFile psiFile2 = myFixture.configureByText("src/com/example/pkg2/File2.java", FILE2_STRING);

        projectModelBuilder.addJavaFileToJavaProject((PsiJavaFile) psiFile1);
        projectModelBuilder.addJavaFileToJavaProject((PsiJavaFile) psiFile2);
    }

    public void testProjectStructure() {
        assertNotNull(javaProject);

        // 3. Assert the `JavaProject` structure
        // Package names are derived from PsiPackage.getName(), which is the last segment.
        // The ProjectModelBuilder stores them in allPackages map with FQN.
        assertNotNull(javaProject.getFromAllPackages("com.example.pkg1"), "Package pkg1 not found by FQN.");
        assertNotNull(javaProject.getFromAllPackages("com.example.pkg2"), "Package pkg2 not found by FQN.");

        // javaProject.packages() returns top-level packages added as direct children.
        // ProjectModelBuilder.findOrCreateJavaPackage likely creates a hierarchy and adds only the
        // top-most non-existent package part as a direct child of JavaProject (e.g. "com").
        // Let's check allPackages for the specific ones we expect.
        assertEquals(2, javaProject.allPackages().filter(p -> p.getName().startsWith("com.example.pkg")).count(),
                "Should have two 'com.example.pkgX' packages in allPackages");


        Set<String> expectedClassNames = Set.of("ClassA", "ClassB", "InnerB1");
        Set<String> actualClassNames = javaProject.allClasses()
                                             .map(JavaClass::getName) // Assuming JavaClass.getName() returns simple name
                                             .collect(Collectors.toSet());
        assertEquals(expectedClassNames, actualClassNames, "Mismatch in all class names globally.");
    }

    public void testPackage1Contents() {
        JavaPackage pkg1 = javaProject.getFromAllPackages("com.example.pkg1");
        assertNotNull(pkg1, "Package 'com.example.pkg1' should exist.");
        assertEquals("com.example.pkg1", pkg1.getName()); // Or just "pkg1" depending on getFromAllPackages key vs JavaPackage.getName()

        assertEquals(1, pkg1.files().count(), "Package pkg1 should contain one file.");
        JavaFile file1 = pkg1.files().findFirst().orElse(null);
        assertNotNull(file1);
        assertEquals("File1.java", file1.getName()); // Name derived from PsiJavaFile

        assertEquals(1, file1.classes().count(), "File1.java should contain one class.");
        JavaClass classA = file1.classes().findFirst().orElse(null);
        assertNotNull(classA);
        assertEquals("ClassA", classA.getName()); // Name from PsiClass

        // Assertions for ClassA
        // Constructor + methodA1 = 2 methods
        assertEquals(2, classA.methods().count(), "ClassA method count mismatch.");
        assertNotNull(classA.metric(MetricType.NOM), "NOM metric for ClassA should exist.");
        assertEquals(Value.of(2), classA.metric(MetricType.NOM).getValue(), "ClassA NOM value mismatch.");
    }

    public void testPackage2ContentsAndInnerClasses() {
        JavaPackage pkg2 = javaProject.getFromAllPackages("com.example.pkg2");
        assertNotNull(pkg2, "Package 'com.example.pkg2' should exist.");
        assertEquals("com.example.pkg2", pkg2.getName());

        assertEquals(1, pkg2.files().count(), "Package pkg2 should contain one file.");
        JavaFile file2 = pkg2.files().findFirst().orElse(null);
        assertNotNull(file2);
        assertEquals("File2.java", file2.getName());

        assertEquals(1, file2.classes().count(), "File2.java should contain one class (ClassB).");
        JavaClass classB = file2.classes().findFirst().orElse(null);
        assertNotNull(classB);
        assertEquals("ClassB", classB.getName());

        // Assertions for ClassB
        // Constructor + methodB1 = 2 methods
        assertEquals(2, classB.methods().count(), "ClassB method count mismatch.");
        assertNotNull(classB.metric(MetricType.NOM), "NOM metric for ClassB should exist.");
        assertEquals(Value.of(2), classB.metric(MetricType.NOM).getValue(), "ClassB NOM value mismatch.");
        assertEquals(1, classB.innerClasses().count(), "ClassB should have one inner class.");

        // Assertions for InnerB1
        JavaClass innerB1 = classB.innerClasses().findFirst().orElse(null);
        assertNotNull(innerB1);
        // Inner class name might be "ClassB.InnerB1" or just "InnerB1" depending on JavaClass.getName()
        // It's typically just the class's own name: "InnerB1"
        assertEquals("InnerB1", innerB1.getName());
        // Constructor + methodIB1 = 2 methods
        assertEquals(2, innerB1.methods().count(), "InnerB1 method count mismatch.");
        assertNotNull(innerB1.metric(MetricType.NOM), "NOM metric for InnerB1 should exist.");
        assertEquals(Value.of(2), innerB1.metric(MetricType.NOM).getValue(), "InnerB1 NOM value mismatch.");
    }
}
