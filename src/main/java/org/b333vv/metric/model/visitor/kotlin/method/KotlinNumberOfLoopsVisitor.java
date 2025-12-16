package org.b333vv.metric.model.visitor.kotlin.method;

import org.b333vv.metric.model.metric.Metric;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.psi.*;

import static org.b333vv.metric.model.metric.MetricType.NOL;

/**
 * Visitor for calculating Number Of Loops (NOL) metric for Kotlin code.
 * 
 * <p>The NOL metric counts all loop and iteration constructs within a method or constructor body.
 * This metric helps assess the iterative complexity of code and can indicate potential performance
 * considerations, testing complexity, and cognitive load for code comprehension.
 * 
 * <h2>Counted Loop Constructs</h2>
 * <p>This visitor counts the following loop constructs:
 * 
 * <h3>Traditional Loops</h3>
 * <ul>
 *   <li><b>for loops</b>: Kotlin for-in iteration loops over ranges, collections, and iterables
 *       <pre>{@code for (item in collection) { ... }}</pre></li>
 *   <li><b>while loops</b>: Conditional loops with entry condition
 *       <pre>{@code while (condition) { ... }}</pre></li>
 *   <li><b>do-while loops</b>: Conditional loops with exit condition
 *       <pre>{@code do { ... } while (condition)}</pre></li>
 * </ul>
 * 
 * <h3>Functional Iteration Constructs</h3>
 * <p>The following Kotlin standard library functions are counted as loops when they accept
 * lambda expressions, as they perform iteration over collections or ranges:
 * <ul>
 *   <li><b>forEach</b>: Iteration over collection elements
 *       <pre>{@code list.forEach { println(it) }}</pre></li>
 *   <li><b>forEachIndexed</b>: Iteration with index access
 *       <pre>{@code list.forEachIndexed { index, item -> ... }}</pre></li>
 *   <li><b>repeat</b>: Fixed number of iterations
 *       <pre>{@code repeat(5) { println(it) }}</pre></li>
 *   <li><b>map</b>: Transformation iterations
 *       <pre>{@code list.map { it * 2 }}</pre></li>
 *   <li><b>mapIndexed</b>: Indexed transformation iterations
 *       <pre>{@code list.mapIndexed { index, item -> ... }}</pre></li>
 *   <li><b>mapNotNull</b>: Transformation with null filtering
 *       <pre>{@code list.mapNotNull { it.toIntOrNull() }}</pre></li>
 *   <li><b>flatMap</b>: Flattening transformation iterations
 *       <pre>{@code list.flatMap { it.items }}</pre></li>
 *   <li><b>flatMapIndexed</b>: Indexed flattening iterations
 *       <pre>{@code list.flatMapIndexed { index, item -> ... }}</pre></li>
 *   <li><b>filter</b>: Filtering iterations
 *       <pre>{@code list.filter { it > 0 }}</pre></li>
 *   <li><b>filterIndexed</b>: Indexed filtering iterations
 *       <pre>{@code list.filterIndexed { index, item -> ... }}</pre></li>
 *   <li><b>fold</b>: Accumulation iterations with initial value
 *       <pre>{@code list.fold(0) { acc, item -> acc + item }}</pre></li>
 *   <li><b>reduce</b>: Accumulation iterations without initial value
 *       <pre>{@code list.reduce { acc, item -> acc + item }}</pre></li>
 *   <li><b>scan</b>: Intermediate accumulation iterations
 *       <pre>{@code list.scan(0) { acc, item -> acc + item }}</pre></li>
 *   <li><b>onEach</b>: Side-effect iterations
 *       <pre>{@code list.onEach { println(it) }}</pre></li>
 *   <li><b>takeWhile</b>: Conditional prefix iterations
 *       <pre>{@code list.takeWhile { it < 10 }}</pre></li>
 *   <li><b>dropWhile</b>: Conditional skip iterations
 *       <pre>{@code list.dropWhile { it < 10 }}</pre></li>
 *   <li><b>groupBy</b>: Grouping iterations by key
 *       <pre>{@code list.groupBy { it.category }}</pre></li>
 *   <li><b>partition</b>: Splitting iterations by predicate
 *       <pre>{@code list.partition { it > 0 }}</pre></li>
 *   <li><b>associateBy</b>: Map construction iterations
 *       <pre>{@code list.associateBy { it.id }}</pre></li>
 *   <li><b>associateWith</b>: Value association iterations
 *       <pre>{@code list.associateWith { it.value }}</pre></li>
 * </ul>
 * 
 * <h2>Not Counted</h2>
 * <p>The following are <b>NOT</b> counted as loops:
 * <ul>
 *   <li>Recursion (recursive function calls)</li>
 *   <li>Sequences (lazy evaluation constructs like {@code sequence { ... }})</li>
 *   <li>Non-iterating higher-order functions (e.g., {@code let}, {@code apply}, {@code also}, {@code run}, {@code with})</li>
 *   <li>Single-element access operations (e.g., {@code find}, {@code first}, {@code last}, {@code single})</li>
 *   <li>Aggregation operations without explicit iteration (e.g., {@code count()}, {@code sum()}, {@code average()})</li>
 * </ul>
 * 
 * <h2>Metric Calculation</h2>
 * <p>The metric is calculated as the total count of all loop constructs (both traditional and functional)
 * found in the method or constructor body. Nested loops are each counted individually.
 * 
 * <h2>Examples</h2>
 * 
 * <h3>Example 1: Traditional Loops (NOL = 3)</h3>
 * <pre>{@code
 * fun processData(items: List<Int>) {
 *     for (item in items) {              // NOL += 1
 *         var i = 0
 *         while (i < item) {             // NOL += 1
 *             i++
 *         }
 *     }
 *     do {                               // NOL += 1
 *         println("done")
 *     } while (false)
 * }
 * }</pre>
 * 
 * <h3>Example 2: Functional Style (NOL = 4)</h3>
 * <pre>{@code
 * fun transformData(data: List<String>) {
 *     data.filter { it.isNotEmpty() }   // NOL += 1
 *         .map { it.toUpperCase() }     // NOL += 1
 *         .forEach { println(it) }      // NOL += 1
 *     
 *     repeat(5) { println(it) }         // NOL += 1
 * }
 * }</pre>
 * 
 * <h3>Example 3: Mixed Constructs (NOL = 4)</h3>
 * <pre>{@code
 * fun complexProcessing(items: List<List<Int>>) {
 *     for (row in items) {              // NOL += 1
 *         row.forEach { item ->         // NOL += 1
 *             var sum = 0
 *             while (sum < item) {      // NOL += 1
 *                 sum++
 *             }
 *         }
 *     }
 *     items.flatMap { it }              // NOL += 1
 * }
 * }</pre>
 * 
 * <h2>Interpretation</h2>
 * <p>Higher NOL values indicate:
 * <ul>
 *   <li>Increased cyclomatic complexity</li>
 *   <li>More potential execution paths to test</li>
 *   <li>Greater cognitive load for understanding control flow</li>
 *   <li>Possible performance considerations for large datasets</li>
 * </ul>
 * 
 * <p>Consider refactoring methods with high NOL values by:
 * <ul>
 *   <li>Extracting nested loops into separate methods</li>
 *   <li>Using functional transformations to chain operations</li>
 *   <li>Simplifying complex iteration logic</li>
 * </ul>
 * 
 * @see KotlinLoopNestingDepthVisitor for measuring maximum nesting depth of loops
 * @see <a href="https://www.aivosto.com/project/help/pm-complexity.html">Code Complexity Metrics</a>
 */
public class KotlinNumberOfLoopsVisitor extends KotlinMethodVisitor {

    @Override
    public void visitNamedFunction(@NotNull KtNamedFunction function) {
        metric = Metric.of(NOL, countLoops(function.getBodyExpression()));
    }

    @Override
    public void visitSecondaryConstructor(@NotNull KtSecondaryConstructor constructor) {
        metric = Metric.of(NOL, countLoops(constructor.getBodyExpression()));
    }

    @Override
    public void visitPrimaryConstructor(@NotNull KtPrimaryConstructor constructor) {
        metric = Metric.of(NOL, 0);
    }

    /**
     * Counts all loop constructs (traditional and functional) within the given expression.
     * 
     * @param body the expression to analyze (function or constructor body)
     * @return the total number of loop constructs found
     */
    private long countLoops(KtExpression body) {
        if (body == null) return 0;
        final long[] count = {0};
        body.accept(new KtTreeVisitorVoid() {
            @Override
            public void visitForExpression(@NotNull KtForExpression expression) {
                count[0]++;
                super.visitForExpression(expression);
            }

            @Override
            public void visitWhileExpression(@NotNull KtWhileExpression expression) {
                count[0]++;
                super.visitWhileExpression(expression);
            }

            @Override
            public void visitDoWhileExpression(@NotNull KtDoWhileExpression expression) {
                count[0]++;
                super.visitDoWhileExpression(expression);
            }

            @Override
            public void visitCallExpression(@NotNull KtCallExpression expression) {
                KtExpression calleeExpression = expression.getCalleeExpression();
                if (calleeExpression != null) {
                    String calleeName = calleeExpression.getText();
                    
                    // Count iterating functions with lambdas as loops
                    if (isIteratingFunction(calleeName) && hasLambdaArgument(expression)) {
                        count[0]++;
                    }
                }
                super.visitCallExpression(expression);
            }

            /**
             * Checks if the function name represents an iterating/looping operation.
             * 
             * @param name the function name to check
             * @return true if the function performs iteration over elements
             */
            private boolean isIteratingFunction(String name) {
                return name.equals("forEach") ||
                       name.equals("forEachIndexed") ||
                       name.equals("repeat") ||
                       name.equals("map") ||
                       name.equals("mapIndexed") ||
                       name.equals("mapNotNull") ||
                       name.equals("flatMap") ||
                       name.equals("flatMapIndexed") ||
                       name.equals("filter") ||
                       name.equals("filterIndexed") ||
                       name.equals("fold") ||
                       name.equals("reduce") ||
                       name.equals("scan") ||
                       name.equals("onEach") ||
                       name.equals("takeWhile") ||
                       name.equals("dropWhile") ||
                       name.equals("groupBy") ||
                       name.equals("partition") ||
                       name.equals("associateBy") ||
                       name.equals("associateWith");
            }

            /**
             * Checks if the call expression has lambda arguments, indicating an iteration operation.
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
        return count[0];
    }
}
