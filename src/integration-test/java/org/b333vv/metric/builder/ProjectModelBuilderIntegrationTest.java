package org.b333vv.metric.builder;

import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.b333vv.metric.model.code.*;
import org.b333vv.metric.model.code.ClassElement;
import org.b333vv.metric.model.code.ProjectElement;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.value.Value; // Added import
// import org.b333vv.metric.util.MetricsUtils; // Removed

import java.util.Set;
import java.util.stream.Collectors;

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

    private ProjectElement javaProject;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        // It's good practice to set current project for any utilities that might rely on it.
        // MetricsUtils.setCurrentProject(getProject()); // Removed

        javaProject = new ProjectElement("TestProject");
        // Assuming ProjectModelBuilder itself does not need MetricsUtils.setCurrentProject directly
        // but the underlying ClassModelBuilder might, which is usually called by ProjectModelBuilder.
        ProjectModelBuilder projectModelBuilder = new ProjectModelBuilder(javaProject);

        // Note: configureByText creates files in a light PSI file system.
        // The path provided to configureByText helps IntelliJ understand package structure.
        PsiFile psiFile1 = myFixture.addFileToProject("com/example/pkg1/File1.java", FILE1_STRING);
        PsiFile psiFile2 = myFixture.addFileToProject("com/example/pkg2/File2.java", FILE2_STRING);

        projectModelBuilder.addJavaFileToJavaProject((PsiJavaFile) psiFile1);
        projectModelBuilder.addJavaFileToJavaProject((PsiJavaFile) psiFile2);
    }

    public void testProjectStructure() {
        assertNotNull("JavaProject model should not be null.", javaProject);

        // 3. Assert the `JavaProject` structure
        // Package names are derived from PsiPackage.getName(), which is the last segment.
        // The ProjectModelBuilder stores them in allPackages map with FQN.
        assertNotNull("Package pkg1 not found by FQN.", javaProject.getFromAllPackages("com.example.pkg1"));
        assertNotNull("Package pkg2 not found by FQN.", javaProject.getFromAllPackages("com.example.pkg2"));

        // javaProject.packages() returns top-level packages added as direct children.
        // ProjectModelBuilder.findOrCreateJavaPackage likely creates a hierarchy and adds only the
        // top-most non-existent package part as a direct child of JavaProject (e.g. "com").
        // Let's check allPackages for the specific ones we expect.
        assertEquals("Should have two 'com.example.pkgX' packages in allPackages", 2L,
                javaProject.allPackages().filter(p -> p.getPsiPackage() != null && p.getPsiPackage().getQualifiedName().startsWith("com.example.pkg")).count());


        Set<String> expectedClassNames = Set.of("ClassA", "ClassB", "InnerB1");
        Set<String> actualClassNames = javaProject.allClasses()
                                             .map(ClassElement::getName) // Assuming JavaClass.getName() returns simple name
                                             .collect(Collectors.toSet());
        assertEquals("Mismatch in all class names globally.", expectedClassNames, actualClassNames);
    }

    public void testPackage1Contents() {
        PackageElement pkg1 = javaProject.getFromAllPackages("com.example.pkg1");
        assertNotNull("Package 'com.example.pkg1' should exist.", pkg1);
        assertEquals("Package name mismatch for pkg1 (short name).", "pkg1", pkg1.getName());
        assertNotNull("PsiPackage for pkg1 should not be null.", pkg1.getPsiPackage());
        assertEquals("Package FQN mismatch for pkg1.", "com.example.pkg1", pkg1.getPsiPackage().getQualifiedName());

        assertEquals("Package pkg1 should contain one file.", 1L, pkg1.files().count());
        FileElement file1 = pkg1.files().findFirst().orElse(null);
        assertNotNull("File1 should exist in pkg1.", file1);
        assertEquals("File1 name mismatch.", "File1.java", file1.getName());

        assertEquals("File1.java should contain one class.", 1L, file1.classes().count());
        ClassElement classA = file1.classes().findFirst().orElse(null);
        assertNotNull("ClassA should exist in File1.", classA);
        assertEquals("ClassA name mismatch.", "ClassA", classA.getName());

        // Assertions for ClassA
        // Constructor + methodA1 = 2 methods
        assertEquals("ClassA method count mismatch.", 2L, classA.methods().count());
        assertNotNull("NOM metric for ClassA should exist.", classA.metric(MetricType.NOM));
        assertEquals("ClassA NOM value mismatch.", Value.of(2), classA.metric(MetricType.NOM).getValue());
    }

    public void testPackage2ContentsAndInnerClasses() {
        PackageElement pkg2 = javaProject.getFromAllPackages("com.example.pkg2");
        assertNotNull("Package 'com.example.pkg2' should exist.", pkg2);
        assertEquals("Package name mismatch for pkg2 (short name).", "pkg2", pkg2.getName());
        assertNotNull("PsiPackage for pkg2 should not be null.", pkg2.getPsiPackage());
        assertEquals("Package FQN mismatch for pkg2.", "com.example.pkg2", pkg2.getPsiPackage().getQualifiedName());

        assertEquals("Package pkg2 should contain one file.", 1L, pkg2.files().count());
        FileElement file2 = pkg2.files().findFirst().orElse(null);
        assertNotNull("File2 should exist in pkg2.", file2);
        assertEquals("File2 name mismatch.", "File2.java", file2.getName());

        assertEquals("File2.java should contain one class (ClassB).", 1L, file2.classes().count());
        ClassElement classB = file2.classes().findFirst().orElse(null);
        assertNotNull("ClassB should exist in File2.", classB);
        assertEquals("ClassB name mismatch.", "ClassB", classB.getName());

        // Assertions for ClassB
        // Constructor + methodB1 = 2 methods
        assertEquals("ClassB method count mismatch.", 2L, classB.methods().count());
        assertNotNull("NOM metric for ClassB should exist.", classB.metric(MetricType.NOM));
        assertEquals("ClassB NOM value mismatch.", Value.of(2), classB.metric(MetricType.NOM).getValue());
        assertEquals("ClassB should have one inner class.", 1L, classB.innerClasses().count());

        // Assertions for InnerB1
        ClassElement innerB1 = classB.innerClasses().findFirst().orElse(null);
        assertNotNull("InnerB1 should exist in ClassB.", innerB1);
        // Inner class name might be "ClassB.InnerB1" or just "InnerB1" depending on JavaClass.getName()
        // It's typically just the class's own name: "InnerB1"
        assertEquals("InnerB1 name mismatch.", "InnerB1", innerB1.getName());
        // Constructor + methodIB1 = 2 methods
        assertEquals("InnerB1 method count mismatch.", 2L, innerB1.methods().count());
        assertNotNull("NOM metric for InnerB1 should exist.", innerB1.metric(MetricType.NOM));
        assertEquals("InnerB1 NOM value mismatch.", Value.of(2), innerB1.metric(MetricType.NOM).getValue());
    }
}
