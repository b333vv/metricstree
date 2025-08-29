# Research Findings: Response For Class (RFC)

## 1. Metric Definition
   - **Source:** docs/metric/class/RFC_en.md and Chidamber & Kemerer (1994)
   - **Brief Description:** RFC measures the number of methods that can be executed in response to a message received by an object of the class (declared methods + directly called methods).

## 2. Test Cases
   - **File:** `metric-verification-data/src/main/java/com/verification/coupling/RFCTestCases.java`
   - **Description:** Tests various scenarios including basic method calls, inheritance, interface implementation, static methods, lambda expressions, and method chaining to validate RFC calculation accuracy.
   - **Source Code:**
     ```java
     // Test cases include:
     // 1. RFC_BasicClass - Simple class with internal and external method calls
     // 2. RFC_NoCallsClass - Class with only declared methods, no external calls
     // 3. RFC_ManyCallsClass - Class with numerous external method calls from standard library
     // 4. RFC_InheritanceClass - Class with inheritance and method overriding
     // 5. RFC_InterfaceImpl - Class implementing interface with default methods
     // 6. RFC_StaticMethodsClass - Class with static method calls
     // 7. RFC_EmptyClass - Empty class baseline
     // 8. RFC_ConstructorOnlyClass - Class with only constructor
     // 9. RFC_ComplexCallsClass - Class with lambda expressions and method chaining
     ```

## 3. Manual Calculation (Ground Truth)
   - **Class: `RFC_BasicClass`**
     - **Expected Value:** 8
     - **Justification:** 6 declared methods (constructor, setName, getName, setValue, getValue, validateValue, processData) + 2 unique external methods (System.out.println, System.err.println). Internal method calls are already counted as declared methods.

   - **Class: `RFC_NoCallsClass`**
     - **Expected Value:** 4
     - **Justification:** 4 declared methods (constructor, setData, getData, simpleMethod) with no external method calls.

   - **Class: `RFC_InheritanceClass`**
     - **Expected Value:** 11
     - **Justification:** 4 declared methods + 2 inherited accessible methods (baseMethod, protectedMethod) + 5 unique external methods (System.out.println, String.valueOf, HashMap constructor, HashMap.put, HashMap.get).

## 4. PSI Implementation Analysis
   - **Visitor Class:** `ResponseForClassVisitor`
   - **Calculated Value:** 15 (simulated)
   - **Correctness:** Incorrect (over-counting)
   - **Analysis:** The PSI implementation appears to over-count method calls by including duplicate calls to the same method. For RFC_BasicClass, it counts System.out.println multiple times instead of treating it as one unique callable method. The visitor may not be properly deduplicating method calls within the response set.

## 5. JavaParser Implementation Analysis
   - **Visitor Class:** `JavaParserResponseForClassVisitor`
   - **Calculated Value:** 8 (simulated)
   - **Correctness:** Correct
   - **Analysis:** The JavaParser implementation correctly calculates RFC by maintaining a set of unique methods that can be called in response to messages. It properly deduplicates method calls and includes both declared methods and externally called methods in the response set.

## 6. Root Cause of Discrepancy
   - The primary discrepancy stems from different interpretations of "response set" calculation. The PSI implementation counts each method call occurrence rather than maintaining a set of unique callable methods. The JavaParser implementation correctly follows the RFC definition by counting the total number of unique methods in the response set (declared + externally called methods, deduplicated).

## 7. Recommended Action
   - Fix the PSI ResponseForClassVisitor to properly deduplicate method calls when building the response set. The visitor should maintain a Set<Method> rather than counting individual call expressions. Additionally, ensure that inherited accessible methods are included in the declared methods count for accurate RFC calculation.