# NCSS (Non-Commenting Source Statements) Metric Calculation Logic

**NCSS (Non-Commenting Source Statements)** counts the number of executable statements in a class, excluding comments and empty statements.

### Main Calculation Logic in NonCommentingSourceStatementsVisitor

1. **Identify Statements:**
   - For each non-anonymous class, examine all statements within the class.
2. **Exclude Comments and Empty Statements:**
   - Ignore statements that are comments or empty.
3. **Count Executable Statements:**
   - Increment the counter for each valid (non-comment, non-empty) statement.
4. **Result:**
   - NCSS is the total count of such statements in the class.

### Example
If a class contains 10 statements, 2 of which are comments and 1 is empty, NCSS = 7.

### Purpose of the Metric
NCSS provides a measure of class size and complexity, helping to assess maintainability and code volume.
