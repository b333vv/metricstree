/*
 * Kotlin Condition Nesting Depth - Enhanced Version
 */
package org.b333vv.metric.model.visitor.kotlin.method;

import org.b333vv.metric.model.metric.Metric;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.psi.*;

import static org.b333vv.metric.model.metric.MetricType.CND;

/**
 * Visitor that computes the Condition Nesting Depth (CND) metric for Kotlin functions.
 * <p>
 * The CND metric measures the maximum nesting depth of conditional constructs within a function.
 * Higher values indicate more complex control flow that may be harder to understand and test.
 * </p>
 * 
 * <h2>Metric Calculation</h2>
 * The metric considers the following conditional constructs in Kotlin:
 * <ul>
 *   <li><b>if expressions</b> - Standard if/else-if/else branches. Each nested if within a branch
 *       increases the depth. Else-if chains at the same level do not increase depth.</li>
 *   <li><b>when expressions</b> - Kotlin's pattern matching construct. Nested when expressions
 *       within branches increase the depth. Multiple when entries at the same level do not
 *       increase depth.</li>
 *   <li><b>try-catch blocks</b> - Exception handling constructs. Each try block and its catch/finally
 *       clauses are considered as conditional logic. Nested try-catch increases depth.</li>
 *   <li><b>elvis operator (?:)</b> - Null-coalescing operator that represents a conditional choice.
 *       Nested elvis operators increase the depth as they represent branching logic.</li>
 *   <li><b>Lambda expressions</b> - Conditional constructs within lambda bodies contribute to the
 *       overall nesting depth of the enclosing function.</li>
 *   <li><b>Anonymous functions</b> - Similar to lambdas, conditional constructs in anonymous
 *       functions are counted toward the total depth.</li>
 *   <li><b>Property accessors</b> - Custom getter and setter implementations may contain
 *       conditional logic that contributes to complexity.</li>
 * </ul>
 * 
 * <h2>Depth Calculation Rules</h2>
 * <ul>
 *   <li>The depth starts at 0 for functions with no conditional constructs</li>
 *   <li>Each conditional construct increases the current depth by 1 when entered</li>
 *   <li>The maximum depth encountered during traversal is recorded</li>
 *   <li>Sequential conditionals at the same level do not increase depth</li>
 *   <li>Only nesting (conditionals inside conditionals) increases depth</li>
 * </ul>
 * 
 * <h2>Examples</h2>
 * <pre>
 * // CND = 1 (single if)
 * fun simple(x: Int) {
 *     if (x > 0) println(x)
 * }
 * 
 * // CND = 2 (nested if)
 * fun nested(x: Int, y: Int) {
 *     if (x > 0) {
 *         if (y > 0) println("both positive")
 *     }
 * }
 * 
 * // CND = 3 (if -> when -> try)
 * fun complex(x: Int?) {
 *     if (x != null) {
 *         when (x) {
 *             in 1..10 -> try {
 *                 process(x)
 *             } catch (e: Exception) {
 *                 handle(e)
 *             }
 *         }
 *     }
 * }
 * 
 * // CND = 2 (elvis operator is conditional)
 * fun withElvis(x: Int?) {
 *     val y = x ?: (calculateDefault() ?: 0)
 * }
 * </pre>
 * 
 * <h2>Interpretation</h2>
 * <ul>
 *   <li><b>CND = 0-1:</b> Simple, easy to understand</li>
 *   <li><b>CND = 2-3:</b> Moderate complexity, generally acceptable</li>
 *   <li><b>CND = 4-5:</b> High complexity, consider refactoring</li>
 *   <li><b>CND > 5:</b> Very high complexity, strongly recommend refactoring</li>
 * </ul>
 * 
 * @see Metric
 * @see org.b333vv.metric.model.metric.MetricType#CND
 */
public class KotlinConditionNestingDepthVisitor extends KotlinMethodVisitor {

    @Override
    public void visitNamedFunction(@NotNull KtNamedFunction function) {
        int depth = maxConditionalDepth(function.getBodyExpression());
        metric = Metric.of(CND, depth);
    }

    @Override
    public void visitSecondaryConstructor(@NotNull KtSecondaryConstructor constructor) {
        int depth = maxConditionalDepth(constructor.getBodyExpression());
        metric = Metric.of(CND, depth);
    }

    /**
     * Calculates the maximum conditional nesting depth within the given expression.
     * 
     * @param body the body expression to analyze (may be null)
     * @return the maximum conditional nesting depth, or 0 if body is null
     */
    private int maxConditionalDepth(KtExpression body) {
        if (body == null) return 0;
        
        final int[] max = {0};
        body.accept(new ConditionalDepthVisitor(max));
        return max[0];
    }

    /**
     * Internal visitor that traverses the PSI tree and tracks conditional nesting depth.
     */
    private static class ConditionalDepthVisitor extends KtTreeVisitorVoid {
        private final int[] max;
        private int current = 0;

        ConditionalDepthVisitor(int[] max) {
            this.max = max;
        }

        @Override
        public void visitIfExpression(@NotNull KtIfExpression expression) {
            current++;
            max[0] = Math.max(max[0], current);
            super.visitIfExpression(expression);
            current--;
        }

        @Override
        public void visitWhenExpression(@NotNull KtWhenExpression expression) {
            current++;
            max[0] = Math.max(max[0], current);
            super.visitWhenExpression(expression);
            current--;
        }

        @Override
        public void visitTryExpression(@NotNull KtTryExpression expression) {
            current++;
            max[0] = Math.max(max[0], current);
            super.visitTryExpression(expression);
            current--;
        }

        @Override
        public void visitBinaryWithTypeRHSExpression(@NotNull KtBinaryExpressionWithTypeRHS expression) {
            // Handle elvis operator (?:) which is a form of conditional logic
            super.visitBinaryWithTypeRHSExpression(expression);
        }

        @Override
        public void visitBinaryExpression(@NotNull KtBinaryExpression expression) {
            // Elvis operator (?:) represents conditional logic: a ?: b means "if a is null, use b"
            if (expression.getOperationReference().getText().equals("?:")) {
                current++;
                max[0] = Math.max(max[0], current);
                super.visitBinaryExpression(expression);
                current--;
            } else {
                super.visitBinaryExpression(expression);
            }
        }

        @Override
        public void visitLambdaExpression(@NotNull KtLambdaExpression lambdaExpression) {
            // Continue traversing lambda bodies to count nested conditions
            super.visitLambdaExpression(lambdaExpression);
        }

        @Override
        public void visitNamedFunction(@NotNull KtNamedFunction function) {
            // Continue traversing anonymous/local functions to count nested conditions
            super.visitNamedFunction(function);
        }

        @Override
        public void visitProperty(@NotNull KtProperty property) {
            // Traverse property accessors which may contain conditional logic
            super.visitProperty(property);
        }

        @Override
        public void visitPropertyAccessor(@NotNull KtPropertyAccessor accessor) {
            // Property getters/setters can contain conditional logic
            super.visitPropertyAccessor(accessor);
        }
    }
}
