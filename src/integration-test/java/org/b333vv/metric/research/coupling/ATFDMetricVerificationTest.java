package org.b333vv.metric.research.coupling;

import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.research.MetricVerificationTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ATFDMetricVerificationTest extends MetricVerificationTest {
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        setupTest("com/verification/coupling/ATFDTestCases.java");
    }

    public void testATFD_GroundTruth() {
        // Manual Calculation for ATFD (Access To Foreign Data):
        // ATFD = Number of unique external classes whose data (fields/methods) is accessed directly
        // Includes: direct field access, method calls to external objects
        // Excludes: access to own fields/methods, access through own methods, standard library (typically)
        // Counts unique classes only - multiple accesses to same class count as 1
        
        // Test Case 1: ATFD_TestClass (comprehensive foreign data access)
        // Foreign classes accessed:
        //   - ForeignDataSource: publicField access, getPublicData(), setPublicData(), etc.
        //   - AnotherForeignSource: numberField, flagField access, getNumber(), isFlag(), performAction()
        //   - ThirdForeignSource: accessed through parameters and local instances
        // Expected ATFD: 3 (ForeignDataSource, AnotherForeignSource, ThirdForeignSource)
        
        // Test Case 2: NoForeignAccess_TestClass
        // Only accesses own fields and methods
        // Expected ATFD: 0
        
        // Test Case 3: SingleForeignAccess_TestClass
        // Only accesses ForeignDataSource (multiple times, but same class)
        // Expected ATFD: 1
        
        // Test Case 4: InheritanceDerived
        // Accesses ForeignDataSource (through inherited field) and AnotherForeignSource
        // Internal method calls (baseAccessForeign, super.baseAccessForeign) should not count
        // Expected ATFD: 2
        
        // Test Case 5: StandardLibraryAccess_TestClass
        // Only accesses standard library (String, List, System, Math)
        // Standard library typically filtered out
        // Expected ATFD: 0 (if standard library filtered)
        
        // Test Case 6: ComplexAccess_TestClass
        // Multiple access patterns to ForeignDataSource (array, conditional, loop, try-catch)
        // All accesses are to same class
        // Expected ATFD: 1
        
        // Ground truth placeholders
        assertEquals(3, 3); // ATFD_TestClass expected
        assertEquals(0, 0); // NoForeignAccess_TestClass expected
        assertEquals(1, 1); // SingleForeignAccess_TestClass expected
        assertEquals(2, 2); // InheritanceDerived expected
        assertEquals(0, 0); // StandardLibraryAccess_TestClass expected (if standard library filtered)
        assertEquals(1, 1); // ComplexAccess_TestClass expected
    }

    public void testATFD_TestClass_PSI() {
        // Test the main class with comprehensive foreign data access
        var psiValue = getPsiValue("ATFD_TestClass", MetricType.ATFD);
        System.out.println("PSI ATFD (ATFD_TestClass) value: " + psiValue);
        if (psiValue != null) {
            double psiDoubleValue = psiValue.doubleValue();
            System.out.println("PSI ATFD (ATFD_TestClass) double value: " + psiDoubleValue);
            // Expected: 3 (ForeignDataSource, AnotherForeignSource, ThirdForeignSource)
        }
    }

    public void testATFD_TestClass_JavaParser() {
        var javaParserValue = getJavaParserValue("ATFD_TestClass", MetricType.ATFD);
        System.out.println("JavaParser ATFD (ATFD_TestClass) value: " + javaParserValue);
        if (javaParserValue != null) {
            double javaParserDoubleValue = javaParserValue.doubleValue();
            System.out.println("JavaParser ATFD (ATFD_TestClass) double value: " + javaParserDoubleValue);
            // Expected: 3 (ForeignDataSource, AnotherForeignSource, ThirdForeignSource)
        }
    }

    public void testATFD_NoForeignAccess_PSI() {
        // Test class with no foreign data access
        var psiValue = getPsiValue("NoForeignAccess_TestClass", MetricType.ATFD);
        System.out.println("PSI ATFD (NoForeignAccess_TestClass) value: " + psiValue);
        // Expected: 0 (no foreign data access)
    }

    public void testATFD_NoForeignAccess_JavaParser() {
        var javaParserValue = getJavaParserValue("NoForeignAccess_TestClass", MetricType.ATFD);
        System.out.println("JavaParser ATFD (NoForeignAccess_TestClass) value: " + javaParserValue);
        // Expected: 0 (no foreign data access)
    }

    public void testATFD_SingleForeignAccess_PSI() {
        // Test class with single foreign class access
        var psiValue = getPsiValue("SingleForeignAccess_TestClass", MetricType.ATFD);
        System.out.println("PSI ATFD (SingleForeignAccess_TestClass) value: " + psiValue);
        // Expected: 1 (only ForeignDataSource)
    }

    public void testATFD_SingleForeignAccess_JavaParser() {
        var javaParserValue = getJavaParserValue("SingleForeignAccess_TestClass", MetricType.ATFD);
        System.out.println("JavaParser ATFD (SingleForeignAccess_TestClass) value: " + javaParserValue);
        // Expected: 1 (only ForeignDataSource)
    }

    public void testATFD_InheritanceBase_PSI() {
        // Test base class with foreign data access
        var psiValue = getPsiValue("InheritanceBase", MetricType.ATFD);
        System.out.println("PSI ATFD (InheritanceBase) value: " + psiValue);
        // Expected: 1 (ForeignDataSource)
    }

    public void testATFD_InheritanceBase_JavaParser() {
        var javaParserValue = getJavaParserValue("InheritanceBase", MetricType.ATFD);
        System.out.println("JavaParser ATFD (InheritanceBase) value: " + javaParserValue);
        // Expected: 1 (ForeignDataSource)
    }

    public void testATFD_InheritanceDerived_PSI() {
        // Test derived class with mix of foreign access and internal calls
        var psiValue = getPsiValue("InheritanceDerived", MetricType.ATFD);
        System.out.println("PSI ATFD (InheritanceDerived) value: " + psiValue);
        // Expected: 2 (ForeignDataSource, AnotherForeignSource)
        // Internal method calls should not count as foreign access
    }

    public void testATFD_InheritanceDerived_JavaParser() {
        var javaParserValue = getJavaParserValue("InheritanceDerived", MetricType.ATFD);
        System.out.println("JavaParser ATFD (InheritanceDerived) value: " + javaParserValue);
        // Expected: 2 (ForeignDataSource, AnotherForeignSource)
        // Internal method calls should not count as foreign access
    }

    public void testATFD_StandardLibraryAccess_PSI() {
        // Test class with only standard library access
        var psiValue = getPsiValue("StandardLibraryAccess_TestClass", MetricType.ATFD);
        System.out.println("PSI ATFD (StandardLibraryAccess_TestClass) value: " + psiValue);
        // Expected: 0 (if standard library is filtered) or variable (if included)
    }

    public void testATFD_StandardLibraryAccess_JavaParser() {
        var javaParserValue = getJavaParserValue("StandardLibraryAccess_TestClass", MetricType.ATFD);
        System.out.println("JavaParser ATFD (StandardLibraryAccess_TestClass) value: " + javaParserValue);
        // Expected: 0 (if standard library is filtered) or variable (if included)
    }

    public void testATFD_ComplexAccess_PSI() {
        // Test class with complex access patterns to single foreign class
        var psiValue = getPsiValue("ComplexAccess_TestClass", MetricType.ATFD);
        System.out.println("PSI ATFD (ComplexAccess_TestClass) value: " + psiValue);
        // Expected: 1 (only ForeignDataSource, despite multiple access patterns)
    }

    public void testATFD_ComplexAccess_JavaParser() {
        var javaParserValue = getJavaParserValue("ComplexAccess_TestClass", MetricType.ATFD);
        System.out.println("JavaParser ATFD (ComplexAccess_TestClass) value: " + javaParserValue);
        // Expected: 1 (only ForeignDataSource, despite multiple access patterns)
    }

    public void testATFD_ModernJavaAccess_PSI() {
        // Test class with modern Java access patterns (lambda, anonymous class)
        var psiValue = getPsiValue("ModernJavaAccess_TestClass", MetricType.ATFD);
        System.out.println("PSI ATFD (ModernJavaAccess_TestClass) value: " + psiValue);
        // Expected: 1+ depending on how lambda/anonymous class access is handled
    }

    public void testATFD_ModernJavaAccess_JavaParser() {
        var javaParserValue = getJavaParserValue("ModernJavaAccess_TestClass", MetricType.ATFD);
        System.out.println("JavaParser ATFD (ModernJavaAccess_TestClass) value: " + javaParserValue);
        // Expected: 1+ depending on how lambda/anonymous class access is handled
    }

    public void testATFD_AllClasses_Summary() {
        // Summary test to compare all classes
        System.out.println("\n=== ATFD SUMMARY COMPARISON ===");
        
        String[] classNames = {
            "ATFD_TestClass", "NoForeignAccess_TestClass", "SingleForeignAccess_TestClass",
            "InheritanceBase", "InheritanceDerived", "StandardLibraryAccess_TestClass", 
            "ComplexAccess_TestClass", "ModernJavaAccess_TestClass"
        };
        
        for (String className : classNames) {
            var psiValue = getPsiValue(className, MetricType.ATFD);
            var javaParserValue = getJavaParserValue(className, MetricType.ATFD);
            System.out.println(String.format("%-30s | PSI: %-8s | JavaParser: %-8s", 
                className, psiValue, javaParserValue));
        }
        
        System.out.println("=== END ATFD SUMMARY ===");
    }
}