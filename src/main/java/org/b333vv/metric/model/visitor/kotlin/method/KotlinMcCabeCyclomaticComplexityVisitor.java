/*
 * Kotlin McCabe Cyclomatic Complexity - Phase 2.4.0
 */
package org.b333vv.metric.model.visitor.kotlin.method;

import org.b333vv.metric.model.metric.Metric;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.lexer.KtTokens;
import org.jetbrains.kotlin.psi.*;

import static org.b333vv.metric.model.metric.MetricType.CC;

/**
 * Computes McCabe Cyclomatic Complexity (CC) for Kotlin functions and constructors.
 * 
 * <p>Cyclomatic Complexity measures the number of linearly independent paths through
 * a program's source code, indicating the complexity of the decision structure.
 * The metric starts with a baseline value of 1 and increments for each decision point.</p>
 * 
 * <h2>Kotlin Language Constructs Counted:</h2>
 * 
 * <h3>Control Flow Statements:</h3>
 * <ul>
 *   <li><b>if expressions</b> - Each if adds 1 to complexity</li>
 *   <li><b>when expressions</b> - Each entry (branch) in when adds 1, including else</li>
 *   <li><b>for loops</b> - Each for adds 1</li>
 *   <li><b>while loops</b> - Each while adds 1</li>
 *   <li><b>do-while loops</b> - Each do-while adds 1</li>
 * </ul>
 * 
 * <h3>Exception Handling:</h3>
 * <ul>
 *   <li><b>catch clauses</b> - Each catch block adds 1</li>
 * </ul>
 * 
 * <h3>Boolean Operators:</h3>
 * <ul>
 *   <li><b>&amp;&amp; (logical AND)</b> - Each occurrence adds 1</li>
 *   <li><b>|| (logical OR)</b> - Each occurrence adds 1</li>
 * </ul>
 * 
 * <h3>Kotlin-Specific Null-Safety Operators:</h3>
 * <ul>
 *   <li><b>?: (Elvis operator)</b> - Each occurrence adds 1 (represents null check with fallback)</li>
 *   <li><b>?. (Safe call operator)</b> - Each occurrence adds 1 (represents null check before member access)</li>
 * </ul>
 * 
 * <h3>Jump Statements:</h3>
 * <ul>
 *   <li><b>break statements</b> - Each break (including labeled) adds 1</li>
 *   <li><b>continue statements</b> - Each continue (including labeled) adds 1</li>
 *   <li><b>return with labels</b> - Non-local returns in lambdas add 1</li>
 * </ul>
 * 
 * <h3>Lambda Expressions:</h3>
 * <ul>
 *   <li><b>Lambda expressions</b> - Lambdas containing control flow structures contribute
 *       to complexity through their internal decision points</li>
 * </ul>
 * 
 * <h2>Examples:</h2>
 * <pre>
 * // CC = 1 (baseline)
 * fun simple() { println("hello") }
 * 
 * // CC = 2 (baseline + 1 if)
 * fun withIf(x: Int) { if (x &gt; 0) println("positive") }
 * 
 * // CC = 3 (baseline + 1 if + 1 &amp;&amp;)
 * fun withAnd(x: Int, y: Int) { if (x &gt; 0 &amp;&amp; y &gt; 0) println("both positive") }
 * 
 * // CC = 4 (baseline + 3 when entries)
 * fun withWhen(x: Int) = when(x) { 
 *     1 -&gt; "one" 
 *     2 -&gt; "two" 
 *     else -&gt; "other" 
 * }
 * 
 * // CC = 3 (baseline + 1 elvis + 1 safe call)
 * fun withNullSafety(s: String?) = s?.length ?: 0
 * </pre>
 * 
 * <h2>Implementation Notes:</h2>
 * <ul>
 *   <li>Primary constructors have a fixed complexity of 1 (baseline)</li>
 *   <li>Secondary constructors are analyzed similarly to regular functions</li>
 *   <li>Init blocks are analyzed similarly to regular functions</li>
 *   <li>The visitor recursively analyzes nested structures and lambda expressions</li>
 *   <li>Each independent decision point increments the complexity counter</li>
 * </ul>
 * 
 * @see <a href="https://en.wikipedia.org/wiki/Cyclomatic_complexity">Cyclomatic Complexity on Wikipedia</a>
 * @author MetricsTree
 * @version 2.4.0
 */
public class KotlinMcCabeCyclomaticComplexityVisitor extends KotlinMethodVisitor {

    @Override
    public void visitNamedFunction(@NotNull KtNamedFunction function) {
        int complexity = 1; // baseline
        complexity += computeForBody(function.getBodyExpression());
        metric = Metric.of(CC, complexity);
    }

    @Override
    public void visitPrimaryConstructor(@NotNull KtPrimaryConstructor constructor) {
        // Primary constructors have minimal logic, treat as baseline
        metric = Metric.of(CC, 1);
    }

    @Override
    public void visitSecondaryConstructor(@NotNull KtSecondaryConstructor constructor) {
        int complexity = 1; // baseline
        KtBlockExpression body = constructor.getBodyExpression();
        complexity += computeForBody(body);
        metric = Metric.of(CC, complexity);
    }

    @Override
    public void visitAnonymousInitializer(@NotNull KtAnonymousInitializer initializer) {
        int complexity = 1; // baseline
        KtExpression body = initializer.getBody();
        complexity += computeForBody(body);
        metric = Metric.of(CC, complexity);
    }

    /**
     * Recursively computes cyclomatic complexity for a given expression body.
     * 
     * @param body the expression to analyze (can be null)
     * @return the computed complexity value (excluding baseline)
     */
    private int computeForBody(KtExpression body) {
        if (body == null)
            return 0;
        
        final int[] c = { 0 };
        
        body.accept(new KtTreeVisitorVoid() {
            @Override
            public void visitIfExpression(@NotNull KtIfExpression expression) {
                c[0] += 1;
                super.visitIfExpression(expression);
            }

            @Override
            public void visitWhenExpression(@NotNull KtWhenExpression expression) {
                // Count each entry (branch) in the when expression
                // Each entry represents a distinct decision path
                int entries = expression.getEntries().size();
                c[0] += Math.max(1, entries);
                super.visitWhenExpression(expression);
            }

            @Override
            public void visitForExpression(@NotNull KtForExpression expression) {
                c[0] += 1;
                super.visitForExpression(expression);
            }

            @Override
            public void visitWhileExpression(@NotNull KtWhileExpression expression) {
                c[0] += 1;
                super.visitWhileExpression(expression);
            }

            @Override
            public void visitDoWhileExpression(@NotNull KtDoWhileExpression expression) {
                c[0] += 1;
                super.visitDoWhileExpression(expression);
            }

            @Override
            public void visitTryExpression(@NotNull KtTryExpression expression) {
                // Each catch clause represents an additional exception handling path
                c[0] += expression.getCatchClauses().size();
                super.visitTryExpression(expression);
            }

            @Override
            public void visitBinaryExpression(@NotNull KtBinaryExpression expression) {
                // Boolean operators create additional decision branches
                if (expression.getOperationToken() == KtTokens.ANDAND
                        || expression.getOperationToken() == KtTokens.OROR) {
                    c[0] += 1;
                }
                
                // Elvis operator ?: represents a null check with fallback value
                // This is always a decision point regardless of context
                if (expression.getOperationToken() == KtTokens.ELVIS) {
                    c[0] += 1;
                }
                super.visitBinaryExpression(expression);
            }

            @Override
            public void visitSafeQualifiedExpression(@NotNull KtSafeQualifiedExpression expression) {
                // Safe call operator ?. represents a null check before member access
                // Every safe call is a potential branch point
                c[0] += 1;
                super.visitSafeQualifiedExpression(expression);
            }

            @Override
            public void visitBreakExpression(@NotNull KtBreakExpression expression) {
                // Break statements (including labeled breaks) create alternate control flow paths
                c[0] += 1;
                super.visitBreakExpression(expression);
            }

            @Override
            public void visitContinueExpression(@NotNull KtContinueExpression expression) {
                // Continue statements (including labeled continues) create alternate control flow paths
                c[0] += 1;
                super.visitContinueExpression(expression);
            }

            @Override
            public void visitReturnExpression(@NotNull KtReturnExpression expression) {
                // Count labeled returns (non-local returns from lambdas)
                // These represent explicit control flow changes in lambda contexts
                if (expression.getLabeledExpression() != null) {
                    c[0] += 1;
                }
                super.visitReturnExpression(expression);
            }

            @Override
            public void visitLambdaExpression(@NotNull KtLambdaExpression expression) {
                // Lambda expressions can contain their own control flow structures
                // Analyze the lambda body to capture internal complexity
                super.visitLambdaExpression(expression);
            }
        });
        
        return c[0];
    }
}
