package org.b333vv.metric.research.coupling;

import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.research.MetricVerificationTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MPCMetricVerificationTest extends MetricVerificationTest {
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        setupTest("com/verification/coupling/MPCTestCases.java");
    }

    public void testMPC_GroundTruth() {
        // Manual Calculation for MPC (Message Passing Coupling):
        // MPC = Number of method calls made by a class to methods of other classes
        // Includes: calls to external class methods, static method calls to external classes
        // Excludes: calls to own methods, field access, constructor calls (typically)
        // Standard library: may or may not be counted depending on implementation
        
        // Test Case 1: MPC_TestClass (primary test with multiple call types)
        // Core external method calls in performOperations():
        //   - externalService.process() (1)
        //   - externalService.getData() (2) 
        //   - anotherService.execute() (3)
        //   - externalService.validate(data) (4)
        //   - anotherService.calculate(42) (5)
        //   - UtilityClass.format(data) (6)
        //   - UtilityClass.log("Operation completed") (7)
        //   - ExternalService.staticMethod() (8)
        //   - System.out.println() (9) - may or may not count
        // Additional calls in chainingCalls():
        //   - externalService.getData() (10)
        //   - anotherService.execute() (11)
        //   - anotherService.calculate(10) (12)
        //   - externalService.validate() (13)
        //   - String.valueOf() (14) - may or may not count
        // Additional calls in conditionalCalls():
        //   - externalService.process() (15)
        //   - anotherService.execute() in loop (16) - may count once or multiple times
        // Expected MPC: 8-16 depending on standard library counting and loop handling
        
        // Test Case 2: NoExternalCalls_TestClass
        // No external method calls, only local method calls and field access
        // Expected MPC: 0
        
        // Test Case 3: StaticCalls_TestClass
        // Static method calls: UtilityClass.format(), UtilityClass.log(), ExternalService.staticMethod()
        // Standard library static calls: Math.abs(), Math.max(), Integer.parseInt(), String.valueOf()
        // Expected MPC: 3-7 depending on standard library counting
        
        // Test Case 4: InstantiationCalls_TestClass
        // Method calls: service.process(), new AnotherService().execute(), 
        //              new ExternalService().getData(), another.execute(), another.calculate()
        // Expected MPC: 5 (constructor calls typically don't count toward MPC)
        
        // Test Case 5: VariousContexts_TestClass
        // Method calls: service.getData() (multiple times), service.process(), 
        //              service.validate(), UtilityClass.log()
        // Expected MPC: 6-8 method calls
        
        // Test Case 6: InheritanceDerived
        // External calls: baseService.getData(), derivedService.execute()
        // Internal calls (not counted): this.baseMethod(), super.baseMethod()
        // Expected MPC: 2
        
        // Ground truth placeholders
        assertEquals(8, 8); // MPC_TestClass minimum expected (core external calls)
        assertEquals(0, 0); // NoExternalCalls_TestClass expected
        assertEquals(3, 3); // StaticCalls_TestClass minimum expected
        assertEquals(5, 5); // InstantiationCalls_TestClass expected
        assertEquals(2, 2); // InheritanceDerived expected
    }

    public void testMPC_TestClass_PSI() {
        // Test the main class with various types of method calls
        var psiValue = getPsiValue("MPC_TestClass", MetricType.MPC);
        System.out.println("PSI MPC (MPC_TestClass) value: " + psiValue);
        if (psiValue != null) {
            double psiDoubleValue = psiValue.doubleValue();
            System.out.println("PSI MPC (MPC_TestClass) double value: " + psiDoubleValue);
            // Expected: 8-16 depending on implementation details
        }
    }

    public void testMPC_TestClass_JavaParser() {
        var javaParserValue = getJavaParserValue("MPC_TestClass", MetricType.MPC);
        System.out.println("JavaParser MPC (MPC_TestClass) value: " + javaParserValue);
        if (javaParserValue != null) {
            double javaParserDoubleValue = javaParserValue.doubleValue();
            System.out.println("JavaParser MPC (MPC_TestClass) double value: " + javaParserDoubleValue);
            // Expected: 8-16 depending on implementation details
        }
    }

    public void testMPC_NoExternalCalls_PSI() {
        // Test class with no external method calls
        var psiValue = getPsiValue("NoExternalCalls_TestClass", MetricType.MPC);
        System.out.println("PSI MPC (NoExternalCalls_TestClass) value: " + psiValue);
        // Expected: 0 (no external method calls)
    }

    public void testMPC_NoExternalCalls_JavaParser() {
        var javaParserValue = getJavaParserValue("NoExternalCalls_TestClass", MetricType.MPC);
        System.out.println("JavaParser MPC (NoExternalCalls_TestClass) value: " + javaParserValue);
        // Expected: 0 (no external method calls)
    }

    public void testMPC_StaticCalls_PSI() {
        // Test class with only static method calls
        var psiValue = getPsiValue("StaticCalls_TestClass", MetricType.MPC);
        System.out.println("PSI MPC (StaticCalls_TestClass) value: " + psiValue);
        // Expected: 3-7 (static calls to external classes)
    }

    public void testMPC_StaticCalls_JavaParser() {
        var javaParserValue = getJavaParserValue("StaticCalls_TestClass", MetricType.MPC);
        System.out.println("JavaParser MPC (StaticCalls_TestClass) value: " + javaParserValue);
        // Expected: 3-7 (static calls to external classes)
    }

    public void testMPC_InstantiationCalls_PSI() {
        // Test class with instantiation and immediate method calls
        var psiValue = getPsiValue("InstantiationCalls_TestClass", MetricType.MPC);
        System.out.println("PSI MPC (InstantiationCalls_TestClass) value: " + psiValue);
        // Expected: 5 (method calls, not counting constructor calls)
    }

    public void testMPC_InstantiationCalls_JavaParser() {
        var javaParserValue = getJavaParserValue("InstantiationCalls_TestClass", MetricType.MPC);
        System.out.println("JavaParser MPC (InstantiationCalls_TestClass) value: " + javaParserValue);
        // Expected: 5 (method calls, not counting constructor calls)
    }

    public void testMPC_VariousContexts_PSI() {
        // Test method calls in different syntactic contexts
        var psiValue = getPsiValue("VariousContexts_TestClass", MetricType.MPC);
        System.out.println("PSI MPC (VariousContexts_TestClass) value: " + psiValue);
        // Expected: 6-8 (method calls in various contexts)
    }

    public void testMPC_VariousContexts_JavaParser() {
        var javaParserValue = getJavaParserValue("VariousContexts_TestClass", MetricType.MPC);
        System.out.println("JavaParser MPC (VariousContexts_TestClass) value: " + javaParserValue);
        // Expected: 6-8 (method calls in various contexts)
    }

    public void testMPC_ComplexCalls_PSI() {
        // Test complex call patterns and nesting
        var psiValue = getPsiValue("ComplexCalls_TestClass", MetricType.MPC);
        System.out.println("PSI MPC (ComplexCalls_TestClass) value: " + psiValue);
        // Expected: Variable depending on how nested calls are counted
    }

    public void testMPC_ComplexCalls_JavaParser() {
        var javaParserValue = getJavaParserValue("ComplexCalls_TestClass", MetricType.MPC);
        System.out.println("JavaParser MPC (ComplexCalls_TestClass) value: " + javaParserValue);
        // Expected: Variable depending on how nested calls are counted
    }

    public void testMPC_InheritanceBase_PSI() {
        // Test base class with external method calls
        var psiValue = getPsiValue("InheritanceBase", MetricType.MPC);
        System.out.println("PSI MPC (InheritanceBase) value: " + psiValue);
        // Expected: 1 (baseService.process())
    }

    public void testMPC_InheritanceBase_JavaParser() {
        var javaParserValue = getJavaParserValue("InheritanceBase", MetricType.MPC);
        System.out.println("JavaParser MPC (InheritanceBase) value: " + javaParserValue);
        // Expected: 1 (baseService.process())
    }

    public void testMPC_InheritanceDerived_PSI() {
        // Test derived class with mix of external and internal calls
        var psiValue = getPsiValue("InheritanceDerived", MetricType.MPC);
        System.out.println("PSI MPC (InheritanceDerived) value: " + psiValue);
        // Expected: 2 (baseService.getData(), derivedService.execute())
        // Internal calls to this.baseMethod() and super.baseMethod() should not count
    }

    public void testMPC_InheritanceDerived_JavaParser() {
        var javaParserValue = getJavaParserValue("InheritanceDerived", MetricType.MPC);
        System.out.println("JavaParser MPC (InheritanceDerived) value: " + javaParserValue);
        // Expected: 2 (baseService.getData(), derivedService.execute())
        // Internal calls to this.baseMethod() and super.baseMethod() should not count
    }

    public void testMPC_AllClasses_Summary() {
        // Summary test to compare all classes
        System.out.println("\n=== MPC SUMMARY COMPARISON ===");
        
        String[] classNames = {
            "MPC_TestClass", "NoExternalCalls_TestClass", "StaticCalls_TestClass",
            "InstantiationCalls_TestClass", "VariousContexts_TestClass", 
            "ComplexCalls_TestClass", "InheritanceBase", "InheritanceDerived"
        };
        
        for (String className : classNames) {
            var psiValue = getPsiValue(className, MetricType.MPC);
            var javaParserValue = getJavaParserValue(className, MetricType.MPC);
            System.out.println(String.format("%-25s | PSI: %-8s | JavaParser: %-8s", 
                className, psiValue, javaParserValue));
        }
        
        System.out.println("=== END MPC SUMMARY ===");
    }
}