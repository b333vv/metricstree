# Research Findings: Lack of Cohesion of Methods (LCOM)

## 1. Metric Definition
   - **Source:** docs/metric/class/LCOM_en.md and Chidamber & Kemerer (1994)
   - **Brief Description:** LCOM measures how methods of a class are related to each other through shared field usage, counting the number of disconnected components of methods.

## 2. Test Cases
   - **File:** `metric-verification-data/src/main/java/com/verification/cohesion/LCOMTestCases.java`
   - **Description:** Tests various cohesion scenarios including perfect cohesion (all methods share fields), no cohesion (methods use different fields), partial cohesion (multiple disconnected components), inheritance patterns, and mixed read/write access patterns.
   - **Source Code:**
     ```java
     // Test cases include:
     // 1. LCOM_PerfectCohesion - All methods use the same field (LCOM = 1)
     // 2. LCOM_NoCohesion - Each method uses different fields (LCOM = 4)
     // 3. LCOM_PartialCohesion - Two disconnected components (LCOM = 2)
     // 4. LCOM_ComplexSharing - Complex field sharing patterns with chaining (LCOM = 2)
     // 5. LCOM_StaticOnly - Only static methods (LCOM = 0)
     // 6. LCOM_NoFieldAccess - Methods don't access fields (LCOM = 0)
     // 7. LCOM_Empty - Empty class baseline (LCOM = 0)
     // 8. LCOM_ChildClass - Inheritance with shared field access (LCOM = 1)
     // 9. LCOM_ReadOnlyAccess - Methods only read fields (LCOM = 1)
     // 10. LCOM_MixedAccess - Mixed read/write patterns (LCOM = 2)
     ```

## 3. Manual Calculation (Ground Truth)
   - **Class: `LCOM_PerfectCohesion`**
     - **Expected Value:** 1
     - **Justification:** All 5 methods (constructor, setSharedField, getSharedField, processSharedField, isSharedFieldEmpty) access the same field 'sharedField', forming one connected component.

   - **Class: `LCOM_NoCohesion`**
     - **Expected Value:** 4
     - **Justification:** Four methods each use different fields (field1, field2, field3, field4) with no shared access, creating 4 disconnected components. methodUsingNoFields is ignored as it accesses no fields.

   - **Class: `LCOM_PartialCohesion`**
     - **Expected Value:** 2
     - **Justification:** Component 1: {setName, getName, setDescription, getFullInfo} connected via name/description fields. Component 2: {incrementCount, getCount, isEnabled, resetCounters} connected via count/flag fields.

   - **Class: `LCOM_StaticOnly`**
     - **Expected Value:** 0
     - **Justification:** Class contains only static methods and static fields. Since LCOM only considers instance methods accessing instance fields, the result is 0.

## 4. PSI Implementation Analysis
   - **Visitor Class:** `LackOfCohesionOfMethodsVisitor`
   - **Calculated Value:** Correct (after fix)
   - **Correctness:** ✅ Correct
   - **Analysis:** **[FIXED]** The PSI implementation has been corrected to properly handle static members and edge cases. Key fixes implemented:
     - **Static Method Exclusion:** Modified `getPsiMethods()` to exclude static methods using `!method.hasModifierProperty(PsiModifier.STATIC)`
     - **Static Field Exclusion:** Updated `FieldsUsedVisitor` to exclude static fields using `!field.hasModifierProperty(PsiModifier.STATIC)`
     - **Edge Case Handling:** Added proper handling for classes with no applicable methods or methods that don't use fields
     - **Early Exit Logic:** Implemented checks to return LCOM = 0 when no instance methods use instance fields

## 5. JavaParser Implementation Analysis
   - **Visitor Class:** `JavaParserLackOfCohesionOfMethodsVisitor`
   - **Calculated Value:** Correct (after fix)
   - **Correctness:** ✅ Correct
   - **Analysis:** **[FIXED]** The JavaParser implementation has been enhanced for consistency with PSI:
     - **Static Method Exclusion:** Added filtering using `!method.isStatic()`
     - **Static Field Exclusion:** Added filtering using `!field.isStatic()`
     - **Consistent Edge Case Handling:** Aligned with PSI implementation for boundary conditions

## 6. Root Cause of Discrepancy (Resolved)
   - **Original Issue:** The discrepancy was caused by inclusion of static methods and static fields in LCOM calculation, which violates the metric definition that only considers instance methods accessing instance fields.
   - **Specific Problem:** The `LCOM_StaticOnly` test case was failing because static methods were being counted as components despite not accessing instance fields.
   - **Solution Applied:** Implemented comprehensive filtering of static members in both PSI and JavaParser implementations.

## 7. Verification Results
   - **Test Status:** ✅ All 11 test cases pass (100% success rate)
   - **PSI vs JavaParser Consistency:** ✅ Both implementations now produce identical results
   - **Edge Cases Handled:** ✅ Static-only classes, empty classes, inheritance scenarios
   - **Metric Accuracy:** ✅ All calculations match manually verified ground truth values

## 8. Implementation Files Modified
   - `src/main/java/org/b333vv/metric/model/visitor/type/LackOfCohesionOfMethodsVisitor.java`
   - `src/main/java/org/b333vv/metric/model/visitor/type/CohesionUtils.java`
   - `src/main/java/org/b333vv/metric/model/javaparser/visitor/type/JavaParserLackOfCohesionOfMethodsVisitor.java`
   - `src/integration-test/java/org/b333vv/metric/research/cohesion/LCOMMetricVerificationTest.java`

## 9. Status: ✅ RESOLVED
   - **Date:** 2025-08-29
   - **Resolution:** Successfully corrected both PSI and JavaParser implementations
   - **Validation:** All test cases pass with correct LCOM calculations
   - **Quality:** 100% accuracy achieved for all test scenarios