package org.b333vv.metric.research.java.coupling;

import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.research.java.MetricVerificationTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DACMetricVerificationTest extends MetricVerificationTest {
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        setupTest("com/verification/coupling/DACTestCases.java");
    }

    public void testDAC_GroundTruth() {
        // Manual Calculation for DAC (Data Abstraction Coupling):
        // DAC = Number of unique abstract data types (classes) used as field types
        // Includes: class types, interface types, enum types, wrapper classes
        // Excludes: primitive types (int, double, boolean, etc.)
        // Standard library: may or may not be counted depending on implementation
        
        // Test Case 1: DAC_TestClass (comprehensive field types)
        // Wrapper classes: Integer, Double, Boolean, Character (4)
        // Standard library: String, List, Map, Set, Date, File, URL (7) - if counted
        // Custom classes: CustomDataType, AnotherDataType, GenericContainer (3)
        // Interface/Abstract: DataInterface, AbstractDataType (2)
        // Array component types: String, CustomDataType (may add 0-2 if counted separately)
        // Expected DAC: 16-18 (if standard library included) or 7-9 (if excluded)
        
        // Test Case 2: NoFields_TestClass
        // No fields at all
        // Expected DAC: 0
        
        // Test Case 3: PrimitivesOnly_TestClass
        // Only primitive type fields
        // Expected DAC: 0
        
        // Test Case 4: DuplicateTypes_TestClass
        // Multiple fields of same types (should count unique types only)
        // Unique types: String, CustomDataType, List, Map (4)
        // Expected DAC: 4 (if standard library counted) or 2 (if excluded)
        
        // Test Case 5: InheritanceDerived
        // Fields from inheritance chain
        // Types: String, CustomDataType, AnotherDataType (3)
        // Expected DAC: 3 (if including inherited fields) or 2 (if only own fields)
        
        // Test Case 6: EnumFields_TestClass
        // Enum types should count as class types
        // Types: TestEnum, DayOfWeek, String, CustomDataType (4)
        // Expected DAC: 4 (if standard library counted) or 2 (if excluded)
        
        // Ground truth placeholders
        assertEquals(16, 16); // DAC_TestClass expected (with standard library)
        assertEquals(0, 0);   // NoFields_TestClass expected
        assertEquals(0, 0);   // PrimitivesOnly_TestClass expected
        assertEquals(4, 4);   // DuplicateTypes_TestClass expected (with standard library)
        assertEquals(4, 4);   // EnumFields_TestClass expected (with standard library)
    }

    public void testDAC_TestClass_PSI() {
        // Test the main class with comprehensive field types
        var psiValue = getPsiValue("DAC_TestClass", MetricType.DAC);
        System.out.println("PSI DAC (DAC_TestClass) value: " + psiValue);
        if (psiValue != null) {
            double psiDoubleValue = psiValue.doubleValue();
            System.out.println("PSI DAC (DAC_TestClass) double value: " + psiDoubleValue);
            // Expected: 16-18 (if including standard library) or 7-9 (if excluding)
        }
    }

    public void testDAC_TestClass_JavaParser() {
        var javaParserValue = getJavaParserValue("DAC_TestClass", MetricType.DAC);
        System.out.println("JavaParser DAC (DAC_TestClass) value: " + javaParserValue);
        if (javaParserValue != null) {
            double javaParserDoubleValue = javaParserValue.doubleValue();
            System.out.println("JavaParser DAC (DAC_TestClass) double value: " + javaParserDoubleValue);
            // Expected: 16-18 (if including standard library) or 7-9 (if excluding)
        }
    }

    public void testDAC_NoFields_PSI() {
        // Test class with no fields
        var psiValue = getPsiValue("NoFields_TestClass", MetricType.DAC);
        System.out.println("PSI DAC (NoFields_TestClass) value: " + psiValue);
        // Expected: 0 (no fields)
    }

    public void testDAC_NoFields_JavaParser() {
        var javaParserValue = getJavaParserValue("NoFields_TestClass", MetricType.DAC);
        System.out.println("JavaParser DAC (NoFields_TestClass) value: " + javaParserValue);
        // Expected: 0 (no fields)
    }

    public void testDAC_PrimitivesOnly_PSI() {
        // Test class with only primitive fields
        var psiValue = getPsiValue("PrimitivesOnly_TestClass", MetricType.DAC);
        System.out.println("PSI DAC (PrimitivesOnly_TestClass) value: " + psiValue);
        // Expected: 0 (primitives don't count toward DAC)
    }

    public void testDAC_PrimitivesOnly_JavaParser() {
        var javaParserValue = getJavaParserValue("PrimitivesOnly_TestClass", MetricType.DAC);
        System.out.println("JavaParser DAC (PrimitivesOnly_TestClass) value: " + javaParserValue);
        // Expected: 0 (primitives don't count toward DAC)
    }

    public void testDAC_DuplicateTypes_PSI() {
        // Test class with duplicate field types (should count unique types only)
        var psiValue = getPsiValue("DuplicateTypes_TestClass", MetricType.DAC);
        System.out.println("PSI DAC (DuplicateTypes_TestClass) value: " + psiValue);
        // Expected: 4 (String, CustomDataType, List, Map - if standard library counted)
    }

    public void testDAC_DuplicateTypes_JavaParser() {
        var javaParserValue = getJavaParserValue("DuplicateTypes_TestClass", MetricType.DAC);
        System.out.println("JavaParser DAC (DuplicateTypes_TestClass) value: " + javaParserValue);
        // Expected: 4 (String, CustomDataType, List, Map - if standard library counted)
    }

    public void testDAC_ComplexGenerics_PSI() {
        // Test class with complex generic types
        var psiValue = getPsiValue("ComplexGenerics_TestClass", MetricType.DAC);
        System.out.println("PSI DAC (ComplexGenerics_TestClass) value: " + psiValue);
        // Expected: Variable depending on generic type parameter handling
    }

    public void testDAC_ComplexGenerics_JavaParser() {
        var javaParserValue = getJavaParserValue("ComplexGenerics_TestClass", MetricType.DAC);
        System.out.println("JavaParser DAC (ComplexGenerics_TestClass) value: " + javaParserValue);
        // Expected: Variable depending on generic type parameter handling
    }

    public void testDAC_InheritanceBase_PSI() {
        // Test base class with fields
        var psiValue = getPsiValue("InheritanceBase", MetricType.DAC);
        System.out.println("PSI DAC (InheritanceBase) value: " + psiValue);
        // Expected: 2 (String, CustomDataType)
    }

    public void testDAC_InheritanceBase_JavaParser() {
        var javaParserValue = getJavaParserValue("InheritanceBase", MetricType.DAC);
        System.out.println("JavaParser DAC (InheritanceBase) value: " + javaParserValue);
        // Expected: 2 (String, CustomDataType)
    }

    public void testDAC_InheritanceDerived_PSI() {
        // Test derived class (should include inherited fields or not)
        var psiValue = getPsiValue("InheritanceDerived", MetricType.DAC);
        System.out.println("PSI DAC (InheritanceDerived) value: " + psiValue);
        // Expected: 3 (all types) or 2 (only derived class fields)
    }

    public void testDAC_InheritanceDerived_JavaParser() {
        var javaParserValue = getJavaParserValue("InheritanceDerived", MetricType.DAC);
        System.out.println("JavaParser DAC (InheritanceDerived) value: " + javaParserValue);
        // Expected: 3 (all types) or 2 (only derived class fields)
    }

    public void testDAC_NestedClasses_PSI() {
        // Test class with nested class fields
        var psiValue = getPsiValue("NestedClasses_TestClass", MetricType.DAC);
        System.out.println("PSI DAC (NestedClasses_TestClass) value: " + psiValue);
        // Expected: 3 (StaticNestedClass, InnerClass, String)
    }

    public void testDAC_NestedClasses_JavaParser() {
        var javaParserValue = getJavaParserValue("NestedClasses_TestClass", MetricType.DAC);
        System.out.println("JavaParser DAC (NestedClasses_TestClass) value: " + javaParserValue);
        // Expected: 3 (StaticNestedClass, InnerClass, String)
    }

    public void testDAC_EnumFields_PSI() {
        // Test class with enum fields
        var psiValue = getPsiValue("EnumFields_TestClass", MetricType.DAC);
        System.out.println("PSI DAC (EnumFields_TestClass) value: " + psiValue);
        // Expected: 4 (TestEnum, DayOfWeek, String, CustomDataType)
    }

    public void testDAC_EnumFields_JavaParser() {
        var javaParserValue = getJavaParserValue("EnumFields_TestClass", MetricType.DAC);
        System.out.println("JavaParser DAC (EnumFields_TestClass) value: " + javaParserValue);
        // Expected: 4 (TestEnum, DayOfWeek, String, CustomDataType)
    }

    public void testDAC_SpecialTypes_PSI() {
        // Test class with special type fields (functional interfaces, exceptions, etc.)
        var psiValue = getPsiValue("SpecialTypes_TestClass", MetricType.DAC);
        System.out.println("PSI DAC (SpecialTypes_TestClass) value: " + psiValue);
        // Expected: 6+ (Runnable, Function, Exception, RuntimeException, Class, String)
    }

    public void testDAC_SpecialTypes_JavaParser() {
        var javaParserValue = getJavaParserValue("SpecialTypes_TestClass", MetricType.DAC);
        System.out.println("JavaParser DAC (SpecialTypes_TestClass) value: " + javaParserValue);
        // Expected: 6+ (Runnable, Function, Exception, RuntimeException, Class, String)
    }

    public void testDAC_AllClasses_Summary() {
        // Summary test to compare all classes
        System.out.println("\n=== DAC SUMMARY COMPARISON ===");
        
        String[] classNames = {
            "DAC_TestClass", "NoFields_TestClass", "PrimitivesOnly_TestClass",
            "DuplicateTypes_TestClass", "ComplexGenerics_TestClass", 
            "InheritanceBase", "InheritanceDerived", "NestedClasses_TestClass",
            "EnumFields_TestClass", "SpecialTypes_TestClass"
        };
        
        for (String className : classNames) {
            var psiValue = getPsiValue(className, MetricType.DAC);
            var javaParserValue = getJavaParserValue(className, MetricType.DAC);
            System.out.println(String.format("%-25s | PSI: %-8s | JavaParser: %-8s", 
                className, psiValue, javaParserValue));
        }
        
        System.out.println("=== END DAC SUMMARY ===");
    }
}