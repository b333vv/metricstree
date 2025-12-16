/*
 * Copyright 2020 b333vv
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.b333vv.metric.model.visitor.kotlin.method;

import org.b333vv.metric.model.metric.Metric;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.psi.*;

import static org.b333vv.metric.model.metric.MetricType.MND;

/**
 * Visitor that computes the Maximum Nesting Depth (MND) metric for Kotlin functions.
 * <p>
 * The MND metric measures the deepest level of nesting of control structures within a function.
 * Higher values indicate more complex control flow that may be harder to understand, test, and maintain.
 * Deep nesting often suggests a need for refactoring, such as extracting nested logic into separate methods
 * or using early returns to reduce nesting levels.
 * </p>
 *
 * <h2>Metric Calculation</h2>
 * The metric considers all control flow and scoping constructs that create nesting levels in Kotlin:
 *
 * <h3>Conditional Constructs</h3>
 * <ul>
 *   <li><b>if expressions</b> - Each if/else-if/else block increases nesting depth when entered.
 *       Nested if statements within branches further increase depth.</li>
 *   <li><b>when expressions</b> - Kotlin's pattern matching construct. Each when expression increases
 *       nesting depth, and nested when expressions within branches increase it further.</li>
 * </ul>
 *
 * <h3>Loop Constructs</h3>
 * <ul>
 *   <li><b>for loops</b> - Each for loop increases nesting depth. Includes traditional loops and
 *       Kotlin's enhanced for-in loops over ranges, collections, and sequences.</li>
 *   <li><b>while loops</b> - Traditional while loops that increase nesting depth.</li>
 *   <li><b>do-while loops</b> - Post-condition loops that increase nesting depth.</li>
 * </ul>
 *
 * <h3>Exception Handling</h3>
 * <ul>
 *   <li><b>try-catch-finally blocks</b> - Exception handling constructs that increase nesting depth.
 *       The try block, each catch clause, and finally block are all considered at the same nesting level,
 *       but nested try-catch within them increases depth further.</li>
 * </ul>
 *
 * <h3>Kotlin-Specific Constructs</h3>
 * <ul>
 *   <li><b>Lambda expressions</b> - Anonymous function literals that create a new scope.
 *       Control structures within lambda bodies contribute to the overall nesting depth.
 *       This includes lambdas passed to scope functions and higher-order functions.</li>
 *   <li><b>Anonymous functions</b> - Explicit anonymous function declarations (fun() { ... })
 *       that create nested scopes. Control structures within them increase nesting depth.</li>
 *   <li><b>Object expressions</b> - Anonymous object creation with potential method implementations.
 *       Methods within object expressions can contain nested control structures.</li>
 *   <li><b>Property accessors</b> - Custom getter and setter implementations that may contain
 *       control flow logic. Complex accessors with nested control structures increase depth.</li>
 *   <li><b>Local/nested functions</b> - Functions defined within other functions. Control
 *       structures within local functions contribute to the enclosing function's complexity.</li>
 * </ul>
 *
 * <h3>Scope Functions</h3>
 * <p>
 * Kotlin's scope functions (let, run, with, apply, also) that accept lambdas are tracked as
 * they create new execution contexts. Control structures within these lambdas increase nesting depth:
 * </p>
 * <pre>
 * // MND = 2 (if + let's lambda scope)
 * fun processUser(user: User?) {
 *     user?.let {
 *         if (it.isActive) {  // nested within let
 *             process(it)
 *         }
 *     }
 * }
 * </pre>
 *
 * <h2>Depth Calculation Rules</h2>
 * <ul>
 *   <li>The depth starts at 0 for functions with no control structures</li>
 *   <li>Each control structure increases the current depth by 1 when entered</li>
 *   <li>The maximum depth encountered during traversal is recorded</li>
 *   <li>Sequential control structures at the same level do not increase depth</li>
 *   <li>Only nesting (structures inside structures) increases depth</li>
 *   <li>Exiting a control structure decreases the current depth by 1</li>
 * </ul>
 *
 * <h2>Examples</h2>
 * <pre>
 * // MND = 1 (single if)
 * fun simple(x: Int) {
 *     if (x > 0) println(x)
 * }
 *
 * // MND = 2 (if inside for)
 * fun nested(items: List<Int>) {
 *     for (item in items) {
 *         if (item > 0) println(item)
 *     }
 * }
 *
 * // MND = 4 (for -> if -> when -> try)
 * fun complex(items: List<String?>) {
 *     for (item in items) {              // depth 1
 *         if (item != null) {             // depth 2
 *             when (item.length) {        // depth 3
 *                 in 1..10 -> try {       // depth 4
 *                     process(item)
 *                 } catch (e: Exception) {
 *                     handle(e)
 *                 }
 *             }
 *         }
 *     }
 * }
 *
 * // MND = 3 (lambda + while + if)
 * fun withLambda(data: List<Int>) {
 *     data.forEach { value ->             // depth 1 (lambda)
 *         var i = 0
 *         while (i < value) {             // depth 2
 *             if (i % 2 == 0) {           // depth 3
 *                 println(i)
 *             }
 *             i++
 *         }
 *     }
 * }
 *
 * // MND = 2 (property accessor with if)
 * class Example {
 *     val computed: Int
 *         get() {
 *             if (condition) {            // depth 1
 *                 return 42
 *             }
 *             return 0
 *         }
 * }
 * </pre>
 *
 * <h2>Interpretation</h2>
 * <ul>
 *   <li><b>MND = 0-1:</b> Simple, flat control flow - easy to understand and test</li>
 *   <li><b>MND = 2-3:</b> Moderate nesting - generally acceptable complexity</li>
 *   <li><b>MND = 4-5:</b> High nesting - consider refactoring to reduce complexity</li>
 *   <li><b>MND > 5:</b> Very high nesting - strongly recommend refactoring using techniques like:
 *     <ul>
 *       <li>Extracting nested logic into separate functions</li>
 *       <li>Using early returns or guard clauses to reduce nesting</li>
 *       <li>Applying the strategy pattern for complex conditional logic</li>
 *       <li>Leveraging Kotlin's when expressions to replace nested if-else chains</li>
 *       <li>Using scope functions appropriately without over-nesting</li>
 *     </ul>
 *   </li>
 * </ul>
 *
 * <h2>Comparison with Related Metrics</h2>
 * <ul>
 *   <li><b>vs. Cyclomatic Complexity (CC):</b> MND measures depth of nesting, while CC measures
 *       the number of independent paths. High CC doesn't always mean high MND and vice versa.</li>
 *   <li><b>vs. Condition Nesting Depth (CND):</b> CND only counts conditional constructs (if, when),
 *       while MND includes all control structures (loops, try-catch, lambdas, etc.).</li>
 *   <li><b>vs. Loop Nesting Depth (LND):</b> LND only counts loop constructs,
 *       while MND is a comprehensive measure of all nesting.</li>
 * </ul>
 *
 * @see Metric
 * @see org.b333vv.metric.model.metric.MetricType#MND
 * @author b333vv
 */
public class KotlinMaximumNestingDepthVisitor extends KotlinMethodVisitor {

    @Override
    public void visitNamedFunction(@NotNull KtNamedFunction function) {
        int depth = maxNestingDepth(function.getBodyExpression());
        metric = Metric.of(MND, depth);
    }

    @Override
    public void visitSecondaryConstructor(@NotNull KtSecondaryConstructor constructor) {
        int depth = maxNestingDepth(constructor.getBodyExpression());
        metric = Metric.of(MND, depth);
    }

    @Override
    public void visitPrimaryConstructor(@NotNull KtPrimaryConstructor constructor) {
        // Primary constructors cannot have a body with control structures
        metric = Metric.of(MND, 0);
    }

    /**
     * Calculates the maximum nesting depth within the given expression.
     *
     * @param body the body expression to analyze (may be null)
     * @return the maximum nesting depth, or 0 if body is null
     */
    private int maxNestingDepth(KtExpression body) {
        if (body == null) return 0;

        final int[] max = {0};
        body.accept(new NestingDepthVisitor(max));
        return max[0];
    }

    /**
     * Internal visitor that traverses the PSI tree and tracks nesting depth of all control structures.
     * This visitor maintains the current depth as it enters and exits nested constructs, recording
     * the maximum depth encountered.
     */
    private static class NestingDepthVisitor extends KtTreeVisitorVoid {
        private final int[] max;
        private int current = 0;

        NestingDepthVisitor(int[] max) {
            this.max = max;
        }

        /**
         * Updates the maximum depth if the current depth exceeds it.
         */
        private void updateMax() {
            max[0] = Math.max(max[0], current);
        }

        /**
         * Enters a nesting level, updates max, traverses children, then exits the level.
         */
        private void withNesting(Runnable action) {
            current++;
            updateMax();
            try {
                action.run();
            } finally {
                current--;
            }
        }

        @Override
        public void visitIfExpression(@NotNull KtIfExpression expression) {
            withNesting(() -> NestingDepthVisitor.super.visitIfExpression(expression));
        }

        @Override
        public void visitWhenExpression(@NotNull KtWhenExpression expression) {
            withNesting(() -> NestingDepthVisitor.super.visitWhenExpression(expression));
        }

        @Override
        public void visitForExpression(@NotNull KtForExpression expression) {
            withNesting(() -> NestingDepthVisitor.super.visitForExpression(expression));
        }

        @Override
        public void visitWhileExpression(@NotNull KtWhileExpression expression) {
            withNesting(() -> NestingDepthVisitor.super.visitWhileExpression(expression));
        }

        @Override
        public void visitDoWhileExpression(@NotNull KtDoWhileExpression expression) {
            withNesting(() -> NestingDepthVisitor.super.visitDoWhileExpression(expression));
        }

        @Override
        public void visitTryExpression(@NotNull KtTryExpression expression) {
            withNesting(() -> NestingDepthVisitor.super.visitTryExpression(expression));
        }

        @Override
        public void visitLambdaExpression(@NotNull KtLambdaExpression lambdaExpression) {
            // Lambda expressions create a new scope and can contain nested control structures
            withNesting(() -> NestingDepthVisitor.super.visitLambdaExpression(lambdaExpression));
        }

        @Override
        public void visitNamedFunction(@NotNull KtNamedFunction function) {
            // Local/nested functions create new scopes with their own control structures
            // We include them in the parent function's complexity measurement
            if (function.getName() == null) {
                // Anonymous or local function inside another function
                withNesting(() -> NestingDepthVisitor.super.visitNamedFunction(function));
            } else {
                // Top-level or member function is handled by the outer visitor
                super.visitNamedFunction(function);
            }
        }

        @Override
        public void visitObjectLiteralExpression(@NotNull KtObjectLiteralExpression expression) {
            // Anonymous objects (object : Type { ... }) can contain methods with control structures
            withNesting(() -> NestingDepthVisitor.super.visitObjectLiteralExpression(expression));
        }

        @Override
        public void visitPropertyAccessor(@NotNull KtPropertyAccessor accessor) {
            // Property getters/setters can contain control flow logic
            withNesting(() -> NestingDepthVisitor.super.visitPropertyAccessor(accessor));
        }

        @Override
        public void visitProperty(@NotNull KtProperty property) {
            // Traverse property to reach its accessors
            super.visitProperty(property);
        }

        @Override
        public void visitCallExpression(@NotNull KtCallExpression expression) {
            // Continue traversing to find lambdas passed to scope functions
            super.visitCallExpression(expression);
        }

        @Override
        public void visitDotQualifiedExpression(@NotNull KtDotQualifiedExpression expression) {
            // Qualified expressions may contain scope function calls with lambdas
            super.visitDotQualifiedExpression(expression);
        }

        @Override
        public void visitSafeQualifiedExpression(@NotNull KtSafeQualifiedExpression expression) {
            // Safe call expressions (?.) may contain scope function calls with lambdas
            super.visitSafeQualifiedExpression(expression);
        }
    }
}
