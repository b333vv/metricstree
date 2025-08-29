# Research Findings: [Metric Name] ([METRIC_CODE])

## 1. Metric Definition
   - **Source:** [Link to docs/metric/class/METRIC_CODE_en.md or academic paper]
   - **Brief Description:** [One-sentence summary of what the metric measures.]

## 2. Test Cases
   - **File:** `metric-verification-data/src/main/java/com/verification/[category]/[TestFileName].java`
   - **Description:** [Briefly describe the scenarios covered by the test file (e.g., "Tests inheritance, field access, and method calls for CBO").]
   - **Source Code:**
     ```java
     // Paste the full source code of the test file here
     ```

## 3. Manual Calculation (Ground Truth)
   - **Class: `[ClassName]`**
     - **Expected Value:** [Number]
     - **Justification:** [Step-by-step reasoning for the expected value based on the metric definition and the source code.]

## 4. PSI Implementation Analysis
   - **Visitor Class:** `[VisitorClassName]`
   - **Calculated Value:** [Value from test harness]
   - **Correctness:** [Correct/Incorrect]
   - **Analysis:** [If incorrect, describe why. e.g., "The visitor fails to count fields inherited from superclasses." If correct, state "Matches expected value."]

## 5. JavaParser Implementation Analysis
   - **Visitor Class:** `[JavaParserVisitorClassName]`
   - **Calculated Value:** [Value from test harness]
   - **Correctness:** [Correct/Incorrect]
   - **Analysis:** [If incorrect, describe why. e.g., "The TypeSolver is not configured correctly, leading to unresolved types and an inaccurate coupling count." If correct, state "Matches expected value."]

## 6. Root Cause of Discrepancy
   - [Summarize the primary reason for any difference between PSI, JavaParser, and the ground truth. e.g., "The PSI implementation has a bug in its counting logic, while the JavaParser implementation is correct." or "Both implementations differ from the ground truth due to a misinterpretation of the metric's formal definition regarding static methods."]

## 7. Recommended Action
   - [Propose a clear action. e.g., "Fix the bug in `CouplingBetweenObjectsVisitor` to correctly include interface implementations." or "Update both visitors to align with the formal definition from the Chidamber & Kemerer paper."]
