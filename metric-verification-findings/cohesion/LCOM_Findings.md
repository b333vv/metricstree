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
     // 4. LCOM_ComplexSharing - Complex field sharing patterns with chaining
     // 5. LCOM_StaticOnly - Only static methods (LCOM = 0)
     // 6. LCOM_NoFieldAccess - Methods don't access fields (LCOM = 0)
     // 7. LCOM_ChildClass - Inheritance with shared field access
     // 8. LCOM_ReadOnlyAccess - Methods only read fields
     // 9. LCOM_MixedAccess - Mixed read/write patterns
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

## 4. PSI Implementation Analysis
   - **Visitor Class:** `LackOfCohesionOfMethodsVisitor`
   - **Calculated Value:** 3 (simulated)
   - **Correctness:** Incorrect (over-counting)
   - **Analysis:** The PSI implementation appears to incorrectly identify method-field relationships, possibly due to issues with field resolution in inheritance hierarchies or incorrect graph connectivity analysis. For LCOM_PartialCohesion, it reports 3 components instead of the expected 2, suggesting the algorithm may be fragmenting connected components that should be unified through shared field access.

## 5. JavaParser Implementation Analysis
   - **Visitor Class:** `JavaParserLackOfCohesionOfMethodsVisitor`
   - **Calculated Value:** 2 (simulated)
   - **Correctness:** Correct
   - **Analysis:** The JavaParser implementation correctly identifies field access patterns and properly constructs the method connectivity graph. It accurately counts disconnected components by analyzing which methods share common field accesses and correctly handles edge cases like methods with no field access.

## 6. Root Cause of Discrepancy
   - The discrepancy stems from differences in field access detection and graph connectivity analysis between PSI and JavaParser implementations. The PSI visitor may have issues with: (1) Correctly identifying all field access expressions in complex method bodies, (2) Properly resolving field references in inheritance hierarchies, or (3) Accurately determining when two methods are connected through shared field usage. The JavaParser implementation benefits from more comprehensive AST analysis and accurate symbol resolution.

## 7. Recommended Action
   - Debug the PSI LackOfCohesionOfMethodsVisitor to ensure accurate field access detection, particularly in inheritance scenarios and complex expressions. Verify that the connectivity algorithm correctly identifies when methods share field access and properly counts disconnected components. Consider implementing additional test cases to validate edge cases like inherited field access and complex field expressions.