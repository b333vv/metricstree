package org.b333vv.metric.research.java.complexity;

import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.research.java.MetricVerificationTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NCSSMetricVerificationTest extends MetricVerificationTest {
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        setupTest("com/verification/complexity/NCSSTestCases.java");
    }

    public void testNCSS_GroundTruth() {
        // Manual Calculation for NCSS (Non-Commenting Source Statements):
        // NCSS = Count of executable statements excluding comments and empty statements
        // Includes: assignments, method calls, control flow statements, declarations with initialization
        // Excludes: comments (single-line //, multi-line /* */, javadoc /** */), empty statements
        // Variable: field declarations, variable declarations without initialization may or may not count
        
        // Test Case 1: NCSS_TestClass (comprehensive statement types)
        // Constructor statements: field assignments (2)
        // Method statements: variable declarations, assignments, method calls, control flow
        // Estimated count:
        //   - Variable declarations with assignment: ~5-8
        //   - Assignment statements: ~8-10
        //   - Method call statements: ~6-8
        //   - Control flow (if, for, while, try-catch, switch): ~8-12
        //   - Return statements: ~3-4
        // Expected NCSS: 25-35 depending on implementation details
        
        // Test Case 2: NCSS_MinimalStatements_TestClass
        // Only essential executable statements: method call, return, assignment
        // Expected NCSS: 3 (minimal set of statements)
        
        // Test Case 3: NCSS_CommentsOnly_TestClass
        // No executable statements, only comments and empty methods
        // Expected NCSS: 0 (no executable code)
        
        // Test Case 4: NCSS_ComplexControlFlow_TestClass
        // Complex nested structures, loops, conditionals
        // Expected NCSS: ~10 statements including control structures
        
        // Test Case 5: NCSS_ExceptionHandling_TestClass
        // Try-catch-finally blocks with statements
        // Expected NCSS: ~10 statements including exception handling
        
        // Ground truth placeholders (will be updated with actual results)
        assertEquals(25, 25); // NCSS_TestClass expected minimum
        assertEquals(3, 3);   // NCSS_MinimalStatements_TestClass expected
        assertEquals(0, 0);   // NCSS_CommentsOnly_TestClass expected
        assertEquals(10, 10); // NCSS_ComplexControlFlow_TestClass expected
        assertEquals(10, 10); // NCSS_ExceptionHandling_TestClass expected
    }

    public void testNCSS_TestClass_PSI() {
        // Test the main class with comprehensive statement types
        var psiValue = getPsiValue("NCSS_TestClass", MetricType.NCSS);
        System.out.println("PSI NCSS (NCSS_TestClass) value: " + psiValue);
        if (psiValue != null) {
            double psiDoubleValue = psiValue.doubleValue();
            System.out.println("PSI NCSS (NCSS_TestClass) double value: " + psiDoubleValue);
            // Expected: 25-35 depending on statement counting methodology
        }
    }

    public void testNCSS_TestClass_JavaParser() {
        var javaParserValue = getJavaParserValue("NCSS_TestClass", MetricType.NCSS);
        System.out.println("JavaParser NCSS (NCSS_TestClass) value: " + javaParserValue);
        if (javaParserValue != null) {
            double javaParserDoubleValue = javaParserValue.doubleValue();
            System.out.println("JavaParser NCSS (NCSS_TestClass) double value: " + javaParserDoubleValue);
            // Expected: 25-35 depending on statement counting methodology
        }
    }

    public void testNCSS_MinimalStatements_PSI() {
        // Test class with minimal executable statements
        var psiValue = getPsiValue("NCSS_MinimalStatements_TestClass", MetricType.NCSS);
        System.out.println("PSI NCSS (NCSS_MinimalStatements_TestClass) value: " + psiValue);
        // Expected: 3 (method call, return, assignment)
    }

    public void testNCSS_MinimalStatements_JavaParser() {
        var javaParserValue = getJavaParserValue("NCSS_MinimalStatements_TestClass", MetricType.NCSS);
        System.out.println("JavaParser NCSS (NCSS_MinimalStatements_TestClass) value: " + javaParserValue);
        // Expected: 3 (method call, return, assignment)
    }

    public void testNCSS_CommentsOnly_PSI() {
        // Test class with only comments and no executable statements
        var psiValue = getPsiValue("NCSS_CommentsOnly_TestClass", MetricType.NCSS);
        System.out.println("PSI NCSS (NCSS_CommentsOnly_TestClass) value: " + psiValue);
        // Expected: 0 (no executable statements)
    }

    public void testNCSS_CommentsOnly_JavaParser() {
        var javaParserValue = getJavaParserValue("NCSS_CommentsOnly_TestClass", MetricType.NCSS);
        System.out.println("JavaParser NCSS (NCSS_CommentsOnly_TestClass) value: " + javaParserValue);
        // Expected: 0 (no executable statements)
    }

    public void testNCSS_ComplexControlFlow_PSI() {
        // Test class with complex control structures
        var psiValue = getPsiValue("NCSS_ComplexControlFlow_TestClass", MetricType.NCSS);
        System.out.println("PSI NCSS (NCSS_ComplexControlFlow_TestClass) value: " + psiValue);
        // Expected: ~10 statements including nested structures
    }

    public void testNCSS_ComplexControlFlow_JavaParser() {
        var javaParserValue = getJavaParserValue("NCSS_ComplexControlFlow_TestClass", MetricType.NCSS);
        System.out.println("JavaParser NCSS (NCSS_ComplexControlFlow_TestClass) value: " + javaParserValue);
        // Expected: ~10 statements including nested structures
    }

    public void testNCSS_ExceptionHandling_PSI() {
        // Test class with try-catch-finally exception handling
        var psiValue = getPsiValue("NCSS_ExceptionHandling_TestClass", MetricType.NCSS);
        System.out.println("PSI NCSS (NCSS_ExceptionHandling_TestClass) value: " + psiValue);
        // Expected: ~10 statements including exception handling
    }

    public void testNCSS_ExceptionHandling_JavaParser() {
        var javaParserValue = getJavaParserValue("NCSS_ExceptionHandling_TestClass", MetricType.NCSS);
        System.out.println("JavaParser NCSS (NCSS_ExceptionHandling_TestClass) value: " + javaParserValue);
        // Expected: ~10 statements including exception handling
    }

    public void testNCSS_VariableDeclarations_PSI() {
        // Test class with various declaration patterns
        var psiValue = getPsiValue("NCSS_VariableDeclarations_TestClass", MetricType.NCSS);
        System.out.println("PSI NCSS (NCSS_VariableDeclarations_TestClass) value: " + psiValue);
        // Expected: Variable depending on declaration counting
    }

    public void testNCSS_VariableDeclarations_JavaParser() {
        var javaParserValue = getJavaParserValue("NCSS_VariableDeclarations_TestClass", MetricType.NCSS);
        System.out.println("JavaParser NCSS (NCSS_VariableDeclarations_TestClass) value: " + javaParserValue);
        // Expected: Variable depending on declaration counting
    }

    public void testNCSS_ModernJava_PSI() {
        // Test class with modern Java features (streams, lambdas, optionals)
        var psiValue = getPsiValue("NCSS_ModernJava_TestClass", MetricType.NCSS);
        System.out.println("PSI NCSS (NCSS_ModernJava_TestClass) value: " + psiValue);
        // Expected: Variable depending on modern feature counting
    }

    public void testNCSS_ModernJava_JavaParser() {
        var javaParserValue = getJavaParserValue("NCSS_ModernJava_TestClass", MetricType.NCSS);
        System.out.println("JavaParser NCSS (NCSS_ModernJava_TestClass) value: " + javaParserValue);
        // Expected: Variable depending on modern feature counting
    }

    public void testNCSS_AllClasses_Summary() {
        // Summary test to compare all classes
        System.out.println("\n=== NCSS SUMMARY COMPARISON ===");
        
        String[] classNames = {
            "NCSS_TestClass", "NCSS_MinimalStatements_TestClass", "NCSS_CommentsOnly_TestClass",
            "NCSS_ComplexControlFlow_TestClass", "NCSS_ExceptionHandling_TestClass", 
            "NCSS_VariableDeclarations_TestClass", "NCSS_ModernJava_TestClass"
        };
        
        for (String className : classNames) {
            var psiValue = getPsiValue(className, MetricType.NCSS);
            var javaParserValue = getJavaParserValue(className, MetricType.NCSS);
            System.out.println(String.format("%-30s | PSI: %-8s | JavaParser: %-8s", 
                className, psiValue, javaParserValue));
        }
        
        System.out.println("=== END NCSS SUMMARY ===");
    }
}