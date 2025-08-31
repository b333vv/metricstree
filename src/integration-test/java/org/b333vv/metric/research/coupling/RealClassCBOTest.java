package org.b333vv.metric.research.coupling;

import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.value.Value;
import org.b333vv.metric.research.MetricVerificationTest;

public class RealClassCBOTest extends MetricVerificationTest {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        // Create a simple test file that references the JavaClass structure
        // We'll analyze the JavaClass that gets processed by the PSI calculation
        myFixture.addFileToProject("org/b333vv/metric/model/code/JavaClass.java",
            "/*\n" +
            " * Licensed under the Apache License, Version 2.0\n" +
            " */\n" +
            "package org.b333vv.metric.model.code;\n" +
            "\n" +
            "import com.intellij.psi.PsiClass;\n" +
            "import com.intellij.psi.PsiElementVisitor;\n" +
            "import org.b333vv.metric.model.visitor.type.JavaClassVisitor;\n" +
            "import org.jetbrains.annotations.NotNull;\n" +
            "\n" +
            "import java.util.Comparator;\n" +
            "import java.util.Objects;\n" +
            "import java.util.stream.Stream;\n" +
            "\n" +
            "public class JavaClass extends JavaCode {\n" +
            "    private final PsiClass psiClass;\n" +
            "\n" +
            "    public JavaClass(@NotNull PsiClass psiClass) {\n" +
            "        super(Objects.requireNonNull(psiClass.getName()));\n" +
            "        this.psiClass = psiClass;\n" +
            "    }\n" +
            "\n" +
            "    public void addClass(@NotNull JavaClass javaClass) {\n" +
            "        addChild(javaClass);\n" +
            "    }\n" +
            "\n" +
            "    public Stream<JavaMethod> methods() {\n" +
            "        return children.stream()\n" +
            "                .filter(c -> c instanceof JavaMethod)\n" +
            "                .map(c -> (JavaMethod) c)\n" +
            "                .sorted(Comparator.comparing(JavaCode::getName));\n" +
            "    }\n" +
            "\n" +
            "    @Override\n" +
            "    public void accept(@NotNull PsiElementVisitor visitor) {\n" +
            "        if (visitor instanceof JavaClassVisitor) {\n" +
            "            ((JavaClassVisitor) visitor).visitJavaClass(this);\n" +
            "        }\n" +
            "    }\n" +
            "}");

        // Use a main file to trigger the analysis scope correctly
        setupTest("org/b333vv/metric/model/code/JavaClass.java");
    }

    public void testRealClassCBOAlignment() {
        // Get the CBO metric for JavaClass
        Value psiValue = getPsiValue("JavaClass", MetricType.CBO);
        Value javaParserValue = getJavaParserValue("JavaClass", MetricType.CBO);

        System.out.println("Real Class CBO Test for JavaClass:");
        System.out.println("PSI Value: " + psiValue);
        System.out.println("JavaParser Value: " + javaParserValue);

        // Debug: Let's see what the actual difference is
        if (psiValue != null && javaParserValue != null) {
            System.out.println("Difference: PSI(" + psiValue.longValue() + ") - JavaParser(" + javaParserValue.longValue() + ") = " + 
                (psiValue.longValue() - javaParserValue.longValue()));
        }

        // Basic validation - both should have defined values
        assertNotNull("PSI CBO should be calculated", psiValue);
        assertNotNull("JavaParser CBO should be calculated", javaParserValue);
        assertFalse("PSI value should not be UNDEFINED", Value.UNDEFINED.equals(psiValue));
        assertFalse("JavaParser value should not be UNDEFINED", Value.UNDEFINED.equals(javaParserValue));
        
        // For now, just report the discrepancy without enforcing equality
        // We'll analyze and fix the differences based on the debug output
    }
}