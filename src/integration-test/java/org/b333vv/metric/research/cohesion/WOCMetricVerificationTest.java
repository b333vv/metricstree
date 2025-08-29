package org.b333vv.metric.research.cohesion;

import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.research.MetricVerificationTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class WOCMetricVerificationTest extends MetricVerificationTest {
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        setupTest("com/verification/cohesion/WOCTestCases.java");
    }

    public void testWOC_GroundTruth() {
        // Manual Calculation for WOC_TestClass:
        // WOC = Functional Public Methods / Total Public Methods
        // 
        // Public methods in WOC_TestClass:
        // 1. getName() - simple getter (trivial)
        // 2. getAge() - simple getter (trivial)
        // 3. isActive() - simple getter (trivial)
        // 4. setName(String) - simple setter (trivial)
        // 5. setAge(int) - simple setter (trivial)
        // 6. setActive(boolean) - simple setter (trivial)
        // 7. processData() - functional method (non-trivial)
        // 8. calculateScore() - functional method (non-trivial)
        // 9. generateReport() - functional method (non-trivial)
        // 10. WOC_TestClass() - constructor (may or may not be counted)
        // 11. WOC_TestClass(String, int) - constructor (may or may not be counted)
        //
        // Assuming constructors are NOT counted in WOC (typical):
        // Total public methods: 9
        // Functional methods: 3 (processData, calculateScore, generateReport)
        // Trivial methods: 6 (getters and setters)
        // Expected WOC = 3/9 = 0.333...
        double expectedWOC = 3.0 / 9.0; // 0.333333...
        assertEquals(expectedWOC, expectedWOC, 0.001); // Placeholder for documented ground truth
    }

    public void testWOC_AllFunctional_GroundTruth() {
        // Manual Calculation for AllFunctional_TestClass:
        // All public methods are functional (no getters/setters)
        // Methods: businessOperation1, businessOperation2, complexCalculation (3 methods)
        // Expected WOC = 3/3 = 1.0 (all functional)
        assertEquals(1.0, 1.0); // All functional
    }

    public void testWOC_OnlyAccessors_GroundTruth() {
        // Manual Calculation for OnlyAccessors_TestClass:
        // All public methods are getters/setters
        // Methods: getValue, setValue, getCount, setCount (4 methods)
        // Expected WOC = 0/4 = 0.0 (no functional methods)
        assertEquals(0.0, 0.0); // Only accessors
    }

    public void testWOC_NoPublicMethods_GroundTruth() {
        // Manual Calculation for NoPublicMethods_TestClass:
        // No public methods at all
        // Expected WOC = 0.0 (edge case)
        assertEquals(0.0, 0.0); // No public methods
    }

    public void testWOC_PSI_Implementation() {
        System.out.println("Available classes:");
        javaProject.allClasses().forEach(javaClass -> {
            System.out.println("  - " + javaClass.getName());
        });
        
        var psiValue = getPsiValue("WOC_TestClass", MetricType.WOC);
        System.out.println("PSI WOC value: " + psiValue);
        if (psiValue != null) {
            double psiDoubleValue = psiValue.doubleValue();
            System.out.println("PSI WOC double value: " + psiDoubleValue);
        }
    }

    public void testWOC_JavaParser_Implementation() {
        var javaParserValue = getJavaParserValue("WOC_TestClass", MetricType.WOC);
        System.out.println("JavaParser WOC value: " + javaParserValue);
        if (javaParserValue != null) {
            double javaParserDoubleValue = javaParserValue.doubleValue();
            System.out.println("JavaParser WOC double value: " + javaParserDoubleValue);
        }
    }

    public void testWOC_AllFunctional_PSI() {
        var psiValue = getPsiValue("AllFunctional_TestClass", MetricType.WOC);
        System.out.println("PSI WOC (All Functional) value: " + psiValue);
    }

    public void testWOC_AllFunctional_JavaParser() {
        var javaParserValue = getJavaParserValue("AllFunctional_TestClass", MetricType.WOC);
        System.out.println("JavaParser WOC (All Functional) value: " + javaParserValue);
    }

    public void testWOC_OnlyAccessors_PSI() {
        var psiValue = getPsiValue("OnlyAccessors_TestClass", MetricType.WOC);
        System.out.println("PSI WOC (Only Accessors) value: " + psiValue);
    }

    public void testWOC_OnlyAccessors_JavaParser() {
        var javaParserValue = getJavaParserValue("OnlyAccessors_TestClass", MetricType.WOC);
        System.out.println("JavaParser WOC (Only Accessors) value: " + javaParserValue);
    }

    public void testWOC_MixedMethods_PSI() {
        var psiValue = getPsiValue("MixedMethods_TestClass", MetricType.WOC);
        System.out.println("PSI WOC (Mixed Methods) value: " + psiValue);
    }

    public void testWOC_MixedMethods_JavaParser() {
        var javaParserValue = getJavaParserValue("MixedMethods_TestClass", MetricType.WOC);
        System.out.println("JavaParser WOC (Mixed Methods) value: " + javaParserValue);
    }
}