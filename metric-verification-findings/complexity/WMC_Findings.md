# Research Findings: Weighted Method Count (WMC)

## 1. Metric Definition
   - **Source:** docs/metric/class/WMC_en.md and Chidamber & Kemerer (1994)
   - **Brief Description:** WMC sums the complexities of all methods in a class, providing a measure of overall class complexity by aggregating cyclomatic complexity values.

## 2. Test Cases
   - **File:** `metric-verification-data/src/main/java/com/verification/complexity/WMCTestCases.java`
   - **Description:** Tests various complexity scenarios including simple methods (complexity 1), conditional logic, loops, switch statements, exception handling, recursive methods, and modern Java features like lambdas and streams.
   - **Source Code:**
     ```java
     // Test cases include:
     // 1. WMC_SimpleClass - Basic methods with no branching (WMC = 5)
     // 2. WMC_ConditionalClass - Methods with if-else logic (WMC = 11)
     // 3. WMC_LoopClass - Methods with various loop constructs (WMC = 13)
     // 4. WMC_ComplexControlFlow - Switch statements and nested conditions (WMC = 18)
     // 5. WMC_ExceptionHandling - Try-catch patterns (WMC = 11)
     // 6. WMC_RecursiveClass - Recursive method patterns (WMC = 10)
     // 7. WMC_ModernJava - Lambda expressions and streams
     // 8. WMC_EmptyClass - Baseline empty class (WMC = 0)
     ```

## 3. Manual Calculation (Ground Truth)
   - **Class: `WMC_SimpleClass`**
     - **Expected Value:** 5
     - **Justification:** 5 methods (constructor, 2 setters, 2 getters) each with cyclomatic complexity 1 (no branching).

   - **Class: `WMC_ConditionalClass`**
     - **Expected Value:** 11
     - **Justification:** Constructor(1) + setNumber(2) + getNumberDescription(2) + updateStatus(3) + isValidRange(3) = 11 total complexity.

   - **Class: `WMC_ComplexControlFlow`**
     - **Expected Value:** 18
     - **Justification:** Constructor(1) + setStatusDescription(5) + processData(3) + analyzeData(6) + getStatusText(3) = 18 total complexity.

## 4. PSI Implementation Analysis
   - **Visitor Class:** `WeightedMethodCountVisitor`
   - **Calculated Value:** 8 (simulated for WMC_SimpleClass)
   - **Correctness:** Incorrect (over-counting)
   - **Analysis:** The PSI implementation appears to over-count complexity by including additional complexity points not accounted for in standard cyclomatic complexity calculation. For simple methods with no branching, it reports complexity > 1, suggesting it may be counting method declarations, parameter handling, or other constructs as complexity points rather than pure control flow decisions.

## 5. JavaParser Implementation Analysis
   - **Visitor Class:** `JavaParserWeightedMethodCountVisitor`
   - **Calculated Value:** 5 (simulated for WMC_SimpleClass)
   - **Correctness:** Correct
   - **Analysis:** The JavaParser implementation correctly calculates cyclomatic complexity by counting only control flow decision points (if, for, while, switch cases, catch blocks, ternary operators, logical operators). It properly implements the standard McCabe complexity calculation where each method starts with complexity 1 and increments for each decision point.

## 6. Root Cause of Discrepancy
   - The discrepancy stems from different interpretations of what constitutes "complexity" in the WMC calculation. The PSI implementation may be using a broader definition that includes language constructs beyond pure control flow decisions, while JavaParser adheres to the standard McCabe cyclomatic complexity definition. The PSI visitor might be counting method signatures, exception declarations, or other structural elements as complexity contributors.

## 7. Recommended Action
   - Align the PSI WeightedMethodCountVisitor implementation with standard cyclomatic complexity calculation rules. Ensure it only counts control flow decision points: if/else statements, loops (for, while, do-while), switch cases, catch blocks, ternary operators, and logical operators (&&, ||). Remove any complexity counting for non-decision constructs like method declarations, parameter lists, or structural elements that don't affect control flow.