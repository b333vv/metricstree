# NCSS (Non-Commenting Source Statements) for Kotlin

Kotlin visitor: `org.b333vv.metric.model.visitor.kotlin.type.KotlinNonCommentingSourceStatementsVisitor`

Counts non-commenting source statements for a Kotlin class.

What is counted:
- Class declaration.
- Each property declaration.
- Each function and constructor declaration.
- Inside bodies: for/while/do-while, when entries, return/throw/break/continue, assignment expressions, top-level call expressions in blocks.
- Else branch as a separate statement.
- Each catch and finally in a try-expression.

Result:
- `NCSS = accumulated count` by walking the class PSI.
