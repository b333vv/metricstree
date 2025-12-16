/*
 * Kotlin Loop Nesting Depth - Phase 2.3.3
 */
package org.b333vv.metric.model.visitor.kotlin.method;

import org.b333vv.metric.model.metric.Metric;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.psi.*;

import static org.b333vv.metric.model.metric.MetricType.LND;

/**
 * Visitor for calculating Loop Nesting Depth (LND) metric for Kotlin code.
 * 
 * <p>Loop Nesting Depth measures the maximum depth of nested loop constructs within a function.
 * This metric helps identify complex iterative logic that may be difficult to understand,
 * maintain, and test. Higher nesting depths generally indicate increased cognitive complexity
 * and potential performance concerns.
 * 
 * <h2>Counted Loop Constructs</h2>
 * <ul>
 *   <li><b>for loops</b>: Traditional Kotlin for-in iteration loops</li>
 *   <li><b>while loops</b>: Conditional loops with entry condition</li>
 *   <li><b>do-while loops</b>: Conditional loops with exit condition</li>
 *   <li><b>forEach</b>: Higher-order function for collection iteration</li>
 *   <li><b>forEachIndexed</b>: Iteration with index access</li>
 *   <li><b>repeat</b>: Kotlin's repeat construct for fixed iterations</li>
 *   <li><b>map</b>: Transformation iterations over collections</li>
 *   <li><b>filter</b>: Filtering iterations over collections</li>
 *   <li><b>flatMap</b>: Flattening transformation iterations</li>
 *   <li><b>fold/reduce</b>: Accumulation iterations</li>
 *   <li><b>scan</b>: Intermediate accumulation iterations</li>
 *   <li><b>onEach</b>: Side-effect iterations</li>
 *   <li><b>also/let/apply</b>: When used with iteration inside lambda bodies</li>
 * </ul>
 * 
 * <h2>Metric Calculation</h2>
 * <p>The metric is calculated as the maximum nesting depth encountered across all
 * execution paths in the function. Each nested loop increments the depth counter.
 * Lambda expressions containing loops contribute to the nesting depth when they
 * are nested within other loops.
 * 
 * <h2>Examples</h2>
 * 
 * <h3>Example 1: Basic Nesting (LND = 2)</h3>
 * <pre>{@code
 * fun processMatrix(matrix: Array<IntArray>) {
 *     for (row in matrix) {              // depth = 1
 *         for (element in row) {         // depth = 2
 *             println(element)
 *         }
 *     }
 * }
 * }</pre>
 * 
 * <h3>Example 2: Functional Style (LND = 2)</h3>
 * <pre>{@code
 * fun processData(data: List<List<Int>>) {
 *     data.forEach { row ->              // depth = 1
 *         row.filter { it > 0 }          // depth = 2
 *            .forEach { println(it) }    // depth = 2
 *     }
 * }
 * }</pre>
 * 
 * <h3>Example 3: Mixed Constructs (LND = 3)</h3>
 * <pre>{@code
 * fun complexProcessing(items: List<List<Int>>) {
 *     while (hasMore()) {                // depth = 1
 *         items.forEach { row ->         // depth = 2
 *             for (item in row) {        // depth = 3
 *                 process(item)
 *             }
 *         }
 *     }
 * }
 * }</pre>
 * 
 * <h2>Best Practices</h2>
 * <p>Generally, LND values above 3 indicate code that may benefit from refactoring.
 * Consider extracting nested loops into separate functions or using functional
 * transformations to flatten the logic.
 * 
 * @see <a href="https://www.aivosto.com/project/help/pm-complexity.html">Loop Nesting Depth Metrics</a>
 */
public class KotlinLoopNestingDepthVisitor extends KotlinMethodVisitor {

    @Override
    public void visitNamedFunction(@NotNull KtNamedFunction function) {
        int depth = maxLoopDepth(function.getBodyExpression());
        metric = Metric.of(LND, depth);
    }

    @Override
    public void visitSecondaryConstructor(@NotNull KtSecondaryConstructor constructor) {
        int depth = maxLoopDepth(constructor.getBodyExpression());
        metric = Metric.of(LND, depth);
    }

    /**
     * Calculates the maximum loop nesting depth within the given expression.
     * 
     * @param body the expression to analyze (function or constructor body)
     * @return the maximum nesting depth of loop constructs
     */
    private int maxLoopDepth(KtExpression body) {
        if (body == null) return 0;
        final int[] max = {0};
        body.accept(new KtTreeVisitorVoid() {
            private int current = 0;

            @Override
            public void visitForExpression(@NotNull KtForExpression expression) {
                current++;
                max[0] = Math.max(max[0], current);
                super.visitForExpression(expression);
                current--;
            }

            @Override
            public void visitWhileExpression(@NotNull KtWhileExpression expression) {
                current++;
                max[0] = Math.max(max[0], current);
                super.visitWhileExpression(expression);
                current--;
            }

            @Override
            public void visitDoWhileExpression(@NotNull KtDoWhileExpression expression) {
                current++;
                max[0] = Math.max(max[0], current);
                super.visitDoWhileExpression(expression);
                current--;
            }

            @Override
            public void visitCallExpression(@NotNull KtCallExpression expression) {
                KtExpression calleeExpression = expression.getCalleeExpression();
                if (calleeExpression != null) {
                    String calleeName = calleeExpression.getText();
                    
                    // Check for iterating functions with lambdas
                    if (isIteratingFunction(calleeName) && hasLambdaArgument(expression)) {
                        current++;
                        max[0] = Math.max(max[0], current);
                        super.visitCallExpression(expression);
                        current--;
                        return;
                    }
                }
                super.visitCallExpression(expression);
            }

            /**
             * Checks if the function name represents an iterating/looping operation.
             * 
             * @param name the function name to check
             * @return true if the function performs iteration
             */
            private boolean isIteratingFunction(String name) {
                return name.equals("forEach") ||
                       name.equals("forEachIndexed") ||
                       name.equals("repeat") ||
                       name.equals("map") ||
                       name.equals("flatMap") ||
                       name.equals("filter") ||
                       name.equals("fold") ||
                       name.equals("reduce") ||
                       name.equals("scan") ||
                       name.equals("onEach") ||
                       name.equals("takeWhile") ||
                       name.equals("dropWhile") ||
                       name.equals("filterIndexed") ||
                       name.equals("mapIndexed") ||
                       name.equals("mapNotNull") ||
                       name.equals("flatMapIndexed") ||
                       name.equals("groupBy") ||
                       name.equals("partition") ||
                       name.equals("associateBy") ||
                       name.equals("associateWith");
            }

            /**
             * Checks if the call expression has lambda arguments.
             * 
             * @param expression the call expression to check
             * @return true if lambda arguments are present
             */
            private boolean hasLambdaArgument(KtCallExpression expression) {
                // Check for trailing lambda or lambda in value arguments
                return !expression.getLambdaArguments().isEmpty() ||
                       expression.getValueArguments().stream()
                           .anyMatch(arg -> arg.getArgumentExpression() instanceof KtLambdaExpression);
            }
        });
        return max[0];
    }
}
