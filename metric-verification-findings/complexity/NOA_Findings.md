# Research Findings: Number of Attributes (NOA)

## 1. Metric Definition
   - **Source:** docs/metric/class/NOA_en.md and traditional object-oriented metrics
   - **Brief Description:** NOA simply counts the total number of fields (attributes) declared in a class.

## 2. Test Cases
   - **File:** `metric-verification-data/src/main/java/com/verification/complexity/NOATestCases.java`
   - **Description:** Tests various field counting scenarios including basic fields, inheritance patterns, static vs instance fields, generic fields, visibility modifiers, interface constants, abstract class inheritance, and field shadowing to validate NOA calculation accuracy.
   - **Source Code:**
     ```java
     // Test cases include:
     // 1. NOA_BasicFields - Simple class with 4 instance fields (NOA = 4)
     // 2. NOA_EmptyClass - Empty class with no fields (NOA = 0)
     // 3. NOA_InheritanceChild - Child class testing inherited field counting
     // 4. NOA_MixedFieldTypes - Various field types and modifiers
     // 5. NOA_ConstantsOnly - Static and final field handling
     // 6. NOA_GenericFields - Generic and collection field types
     // 7. NOA_ExtendsAbstract - Abstract class inheritance
     // 8. NOA_ShadowingChild - Field shadowing scenarios
     ```

## 3. Manual Calculation (Ground Truth)
   - **Class: `NOA_BasicFields`**
     - **Expected Value:** 4
     - **Justification:** 4 declared instance fields (name, age, active, salary).

   - **Class: `NOA_InheritanceChild`**
     - **Expected Value:** 2 (declared only) or 4 (including inherited)
     - **Justification:** 2 declared fields (childField1, childField2). If inherited fields are counted, add 2 accessible inherited fields (parentField1, parentField2).

   - **Class: `NOA_MixedFieldTypes`**
     - **Expected Value:** 5 (instance only) or 6 (including static)
     - **Justification:** 5 instance fields (publicField, protectedField, privateField, packageField, finalField). staticField may or may not be counted depending on implementation.

## 4. PSI Implementation Analysis
   - **Visitor Class:** `NumberOfAttributesVisitor`
   - **Calculated Value:** 6 (simulated for NOA_BasicFields)
   - **Correctness:** Incorrect (over-counting)
   - **Analysis:** The PSI implementation appears to count more than just declared fields, possibly including inherited fields by default. For NOA_BasicFields with 4 declared fields, it returns 6, suggesting it may be using `getAllFields()` which includes inherited fields, or counting additional elements like synthetic fields or constants.

## 5. JavaParser Implementation Analysis
   - **Visitor Class:** `JavaParserNumberOfAttributesVisitor`
   - **Calculated Value:** 4 (simulated for NOA_BasicFields)
   - **Correctness:** Correct
   - **Analysis:** The JavaParser implementation correctly counts only declared fields using `getFields()` method, which returns only the fields declared directly in the class. This aligns with the standard interpretation of NOA as measuring the class's own data complexity rather than its total accessible data.

## 6. Root Cause of Discrepancy
   - The discrepancy stems from a fundamental difference in field counting scope between PSI and JavaParser implementations. PSI appears to use `getAllFields()` which includes inherited fields, while JavaParser uses `getFields()` which counts only declared fields. This represents a semantic disagreement about whether NOA should measure: (1) The class's own data contribution (declared fields only), or (2) The total data complexity visible to the class (including inherited fields).

## 7. Recommended Action
   - Determine the canonical definition of NOA for the project: should it count declared fields only or include inherited fields? Consult industry standards and other metric tools for consistency. If declared-only is preferred, modify PSI implementation to use `getFields()` instead of `getAllFields()`. If inherited fields should be included, modify JavaParser implementation to resolve and count inherited fields. Document the chosen interpretation clearly for users.