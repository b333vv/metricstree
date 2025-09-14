package org.b333vv.metric.research.java.complexity;

import com.intellij.psi.PsiClass;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.b333vv.metric.model.code.ClassElement;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.visitor.type.NumberOfAttributesVisitor;
import org.b333vv.metric.model.javaparser.visitor.type.JavaParserNumberOfAttributesVisitor;
import org.b333vv.metric.model.metric.value.Value;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ParseResult;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Verification test for NOA (Number of Attributes) metric
 * Compares PSI and JavaParser implementations across various test scenarios
 * 
 * KEY HYPOTHESIS: PSI uses getAllFields() (inherited + declared) 
 * vs JavaParser uses getFields() (declared only) - expecting discrepancies!
 */
public class NOAMetricVerificationTest extends BasePlatformTestCase {

    @Override
    protected String getTestDataPath() {
        return "metric-verification-data/src/main/java";
    }

    /**
     * Manual ground truth calculations for NOA test cases
     * NOA counts fields/attributes in a class
     */
    private void validateGroundTruth() {
        System.out.println("=== NOA Metric Ground Truth Analysis ===");
        
        // Test Case 1: NOA_BasicFields_TestClass
        System.out.println("NOA_BasicFields_TestClass expected:");
        System.out.println("  PSI (getAllFields): 4 fields [privateField + protectedField + publicField + staticField]");
        System.out.println("  JavaParser (getFields): 4 fields [declared only]");
        System.out.println("  Expected difference: 0 (no inheritance)");
        
        // Test Case 2: NOA_EmptyClass_TestClass  
        System.out.println("NOA_EmptyClass_TestClass expected:");
        System.out.println("  PSI (getAllFields): 0 fields");
        System.out.println("  JavaParser (getFields): 0 fields");
        System.out.println("  Expected difference: 0");
        
        // Test Case 3: NOA_InheritanceChild_TestClass
        System.out.println("NOA_InheritanceChild_TestClass expected:");
        System.out.println("  PSI (getAllFields): 4 fields [2 inherited + 2 declared]");
        System.out.println("  JavaParser (getFields): 2 fields [declared only]");
        System.out.println("  Expected difference: +2 (PSI includes inherited)");
        
        // Test Case 4: NOA_ConstantsOnly_TestClass
        System.out.println("NOA_ConstantsOnly_TestClass expected:");
        System.out.println("  PSI (getAllFields): 3 constants");
        System.out.println("  JavaParser (getFields): 3 constants");
        System.out.println("  Expected difference: 0");
        
        // Test Case 5: NOA_MixedFieldTypes_TestClass
        System.out.println("NOA_MixedFieldTypes_TestClass expected:");
        System.out.println("  PSI (getAllFields): 6 fields [various types and modifiers]");
        System.out.println("  JavaParser (getFields): 6 fields [various types and modifiers]");
        System.out.println("  Expected difference: 0");
    }

    public void testNOAVerification() {
        validateGroundTruth();
        
        System.out.println("\\n=== Starting NOA Metric Verification ===");
        
        // Test all NOA test cases - focusing on inheritance differences
        verifyNOAForClass("NOA_BasicFields_TestClass", 4, 4);
        verifyNOAForClass("NOA_EmptyClass_TestClass", 0, 0);
        verifyNOAForClass("NOA_InheritanceChild_TestClass", 4, 2); // Key test: PSI=4, JavaParser=2
        verifyNOAForClass("NOA_ConstantsOnly_TestClass", 3, 3);
        verifyNOAForClass("NOA_MixedFieldTypes_TestClass", 6, 6);
        
        System.out.println("=== NOA Metric Verification Complete ===\\n");
    }

    private void verifyNOAForClass(String className, int expectedPSI, int expectedJavaParser) {
        try {
            System.out.println("\\n--- Verifying " + className + " ---");
            System.out.println("Expected PSI NOA: " + expectedPSI + " (getAllFields)");
            System.out.println("Expected JavaParser NOA: " + expectedJavaParser + " (getFields)");
            
            // Get PSI value
            Value psiValue = getPSINOAValue(className);
            System.out.println("PSI NOA (" + className + ") value: " + psiValue.intValue());
            
            // Get JavaParser value  
            Value javaParserValue = getJavaParserNOAValue(className);
            System.out.println("JavaParser NOA (" + className + ") value: " + javaParserValue.intValue());
            
            // Calculate discrepancy
            int discrepancy = javaParserValue.intValue() - psiValue.intValue();
            System.out.println("Discrepancy: " + discrepancy + " (JavaParser - PSI)");
            
            if (discrepancy != 0) {
                System.out.println("⚠️  EXPECTED DISCREPANCY CONFIRMED");
            } else {
                System.out.println("✅ Values match (unexpected for inheritance cases)");
            }
            
            // Compare with expected
            if (psiValue.intValue() != expectedPSI) {
                System.out.println("❌ PSI differs from expected by: " + (psiValue.intValue() - expectedPSI));
            } else {
                System.out.println("✅ PSI matches expected");
            }
            
            if (javaParserValue.intValue() != expectedJavaParser) {
                System.out.println("❌ JavaParser differs from expected by: " + (javaParserValue.intValue() - expectedJavaParser));
            } else {
                System.out.println("✅ JavaParser matches expected");
            }
            
        } catch (Exception e) {
            System.err.println("Error verifying " + className + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Value getPSINOAValue(String className) {
        try {
            // Load the source code and configure it as individual class file
            String sourceCode = getJavaSourceForClass(className);
            com.intellij.psi.PsiJavaFile psiJavaFile = (com.intellij.psi.PsiJavaFile) myFixture.configureByText(className + ".java", sourceCode);
            
            // Find the target class (should be the main class)
            PsiClass psiClass = null;
            for (PsiClass cls : psiJavaFile.getClasses()) {
                if (cls.getName().equals(className)) {
                    psiClass = cls;
                    break;
                }
            }
            
            if (psiClass == null) {
                fail("Could not find PsiClass for " + className);
                return Value.UNDEFINED;
            }
            
            ClassElement javaClass = new ClassElement(psiClass);
            NumberOfAttributesVisitor visitor = new NumberOfAttributesVisitor();
            javaClass.accept(visitor);
            
            Metric metric = javaClass.metrics().findFirst().orElse(null);
            assertNotNull("NOA metric should be calculated", metric);
            assertEquals("Metric type should be NOA", MetricType.NOA, metric.getType());
            
            return metric.getValue();
        } catch (Exception e) {
            fail("Failed to get PSI NOA value for " + className + ": " + e.getMessage());
            return Value.UNDEFINED;
        }
    }

    private Value getJavaParserNOAValue(String className) {
        try {
            // Load the source file and parse with JavaParser
            String sourceCode = getJavaSourceForClass(className);
            
            JavaParser javaParser = new JavaParser();
            ParseResult<CompilationUnit> parseResult = javaParser.parse(sourceCode);
            
            if (!parseResult.isSuccessful()) {
                fail("Failed to parse source code for " + className);
                return Value.UNDEFINED;
            }
            
            CompilationUnit cu = parseResult.getResult().get();
            
            ClassOrInterfaceDeclaration classDecl = cu.findFirst(ClassOrInterfaceDeclaration.class, 
                cls -> cls.getNameAsString().equals(className)).orElse(null);
            
            if (classDecl == null) {
                fail("Could not find class declaration for " + className);
                return Value.UNDEFINED;
            }
            
            JavaParserNumberOfAttributesVisitor visitor = new JavaParserNumberOfAttributesVisitor();
            AtomicReference<Value> result = new AtomicReference<>(Value.UNDEFINED);
            
            visitor.visit(classDecl, metric -> {
                if (metric.getType() == MetricType.NOA) {
                    result.set(metric.getValue());
                }
            });
            
            return result.get();
        } catch (Exception e) {
            fail("Failed to get JavaParser NOA value for " + className + ": " + e.getMessage());
            return Value.UNDEFINED;
        }
    }

    private String getJavaSourceForClass(String className) {
        switch (className) {
            case "NOA_BasicFields_TestClass":
                return "package com.verification.complexity;\n\n" +
                       "public class NOA_BasicFields_TestClass {\n" +
                       "    private String privateField = \"value\";\n" +
                       "    protected int protectedField;\n" +
                       "    public boolean publicField = true;\n" +
                       "    private static final String staticField = \"constant\";\n" +
                       "}\n";
                       
            case "NOA_EmptyClass_TestClass":
                return "package com.verification.complexity;\n\n" +
                       "public class NOA_EmptyClass_TestClass {\n" +
                       "    // No fields\n" +
                       "}\n";
                       
            case "NOA_InheritanceChild_TestClass":
                return "package com.verification.complexity;\n\n" +
                       "class NOA_BaseClass {\n" +
                       "    protected String baseField1;\n" +
                       "    private int baseField2;\n" +
                       "}\n\n" +
                       "public class NOA_InheritanceChild_TestClass extends NOA_BaseClass {\n" +
                       "    private String childField1 = \"child\";\n" +
                       "    public double childField2;\n" +
                       "}\n";
                       
            case "NOA_ConstantsOnly_TestClass":
                return "package com.verification.complexity;\n\n" +
                       "public class NOA_ConstantsOnly_TestClass {\n" +
                       "    public static final int CONSTANT_1 = 42;\n" +
                       "    private static final String CONSTANT_2 = \"text\";\n" +
                       "    protected static final boolean CONSTANT_3 = false;\n" +
                       "}\n";
                       
            case "NOA_MixedFieldTypes_TestClass":
                return "package com.verification.complexity;\n\n" +
                       "import java.util.List;\n" +
                       "import java.util.Map;\n\n" +
                       "public class NOA_MixedFieldTypes_TestClass {\n" +
                       "    private String stringField;\n" +
                       "    protected int intField = 100;\n" +
                       "    public List<String> listField;\n" +
                       "    private Map<String, Object> mapField;\n" +
                       "    static double staticField = 3.14;\n" +
                       "    final boolean finalField = true;\n" +
                       "}\n";
                       
            default:
                fail("Unknown test class: " + className);
                return "";
        }
    }
}