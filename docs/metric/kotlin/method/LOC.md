# LOC (Lines Of Code) for Kotlin

Kotlin visitor: `org.b333vv.metric.model.visitor.kotlin.method.KotlinLinesOfCodeVisitor`

Handling policy:
- Block-body: count newline characters within the body braces.
- Expression-body: count as 1 line by default, plus nested newlines if any.

Result:
- `LOC = number of lines` within the function/constructor body text; 0 if body is absent.
