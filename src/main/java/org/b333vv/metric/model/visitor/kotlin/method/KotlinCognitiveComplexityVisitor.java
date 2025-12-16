package org.b333vv.metric.model.visitor.kotlin.method;

import com.intellij.psi.tree.IElementType;
import org.b333vv.metric.model.metric.Metric;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.lexer.KtTokens;
import org.jetbrains.kotlin.psi.*;

import java.util.HashSet;
import java.util.Set;

import static org.b333vv.metric.model.metric.MetricType.CCM;

/**
 * Visitor for calculating Cognitive Complexity Metric (CCM) for Kotlin code.
 * 
 * <p>Cognitive Complexity is a measure of how difficult code is to understand, based on the
 * cognitive load required to comprehend the control flow. Unlike Cyclomatic Complexity,
 * which measures structural complexity, Cognitive Complexity focuses on human readability
 * and comprehension effort.
 * 
 * <h2>Metric Calculation Rules</h2>
 * 
 * <h3>Basic Increments (+1 for each):</h3>
 * <ul>
 *   <li><b>Decision structures</b>: if, when, while, do-while, for loops</li>
 *   <li><b>Exception handling</b>: each catch clause</li>
 *   <li><b>Jump expressions</b>: break, continue, and return with labels</li>
 *   <li><b>Recursion</b>: function calls to itself (detected by name and arity match)</li>
 *   <li><b>Boolean operators</b>: transitions between && and || operators in chains</li>
 *   <li><b>Elvis operator</b>: ?: conditional expressions</li>
 *   <li><b>When entries</b>: each branch after the first in when expressions</li>
 * </ul>
 * 
 * <h3>Nesting Increments (+nesting level):</h3>
 * <ul>
 *   <li>Additional penalty for each level of nesting inside decision structures</li>
 *   <li>Lambda expressions increase nesting level for enclosed structures</li>
 *   <li>Scope functions (let, also, apply, run, with) increase nesting when containing decisions</li>
 * </ul>
 * 
 * <h3>Zero Increment (no complexity added):</h3>
 * <ul>
 *   <li><b>Safe call operator</b>: ?. (designed for safety, reduces cognitive load)</li>
 *   <li><b>Non-null assertion</b>: !! (simple operation)</li>
 *   <li><b>Else clauses</b>: counted as part of the if structure</li>
 *   <li><b>Finally blocks</b>: always executed, no decision involved</li>
 * </ul>
 * 
 * <h2>Kotlin-Specific Constructs</h2>
 * <ul>
 *   <li><b>Lambda expressions</b>: increase nesting level for enclosed decision structures</li>
 *   <li><b>When expressions</b>: first branch free, each subsequent branch +1</li>
 *   <li><b>Scope functions</b>: let, also, apply, run, with increase nesting level</li>
 *   <li><b>Elvis operator</b>: ?: adds +1 as a conditional branch</li>
 *   <li><b>Labeled jumps</b>: break@label, continue@label, return@label add +1</li>
 * </ul>
 * 
 * <h2>Example</h2>
 * <pre>{@code
 * fun example(list: List<Int>?) {  // CCM = 0
 *     if (list != null) {          // +1 (decision)
 *         list.forEach { item ->   // +1 (nesting in lambda)
 *             if (item > 0) {      // +1 (decision) +1 (nesting level 1)
 *                 println(item)
 *             }
 *         }
 *     }
 *     // Total CCM = 4
 * }
 * }</pre>
 * 
 * @see <a href="https://www.sonarsource.com/docs/CognitiveComplexity.pdf">Cognitive Complexity White Paper</a>
 */
public class KotlinCognitiveComplexityVisitor extends KotlinMethodVisitor {

    private int complexity;
    private int nesting;

    @Override
    public void visitNamedFunction(@NotNull KtNamedFunction function) {
        complexity = 0;
        nesting = 0;
        KtExpression body = function.getBodyExpression();
        if (body != null) {
            body.accept(new BodyVisitor(function));
        }
        metric = Metric.of(CCM, complexity);
    }

    @Override
    public void visitSecondaryConstructor(@NotNull KtSecondaryConstructor constructor) {
        complexity = 0;
        nesting = 0;
        KtExpression body = constructor.getBodyExpression();
        if (body != null) {
            body.accept(new BodyVisitor(null));
        }
        metric = Metric.of(CCM, complexity);
    }

    /**
     * Internal visitor for traversing method/constructor body and calculating complexity.
     */
    private class BodyVisitor extends KtTreeVisitorVoid {
        private final KtNamedFunction owner;
        private IElementType lastBooleanOp = null;

        BodyVisitor(KtNamedFunction owner) {
            this.owner = owner;
        }

        @Override
        public void visitIfExpression(@NotNull KtIfExpression expression) {
            enterDecision();
            super.visitIfExpression(expression);
            exitDecision();
        }

        @Override
        public void visitWhenExpression(@NotNull KtWhenExpression expression) {
            enterDecision();
            // Count each when entry after the first one
            int entries = expression.getEntries().size();
            if (entries > 1) {
                // First entry is covered by enterDecision(), each subsequent adds +1
                complexity += (entries - 1);
            }
            super.visitWhenExpression(expression);
            exitDecision();
        }

        @Override
        public void visitForExpression(@NotNull KtForExpression expression) {
            enterDecision();
            super.visitForExpression(expression);
            exitDecision();
        }

        @Override
        public void visitWhileExpression(@NotNull KtWhileExpression expression) {
            enterDecision();
            super.visitWhileExpression(expression);
            exitDecision();
        }

        @Override
        public void visitDoWhileExpression(@NotNull KtDoWhileExpression expression) {
            enterDecision();
            super.visitDoWhileExpression(expression);
            exitDecision();
        }

        @Override
        public void visitTryExpression(@NotNull KtTryExpression expression) {
            // Each catch clause is a decision point
            for (KtCatchClause catchClause : expression.getCatchClauses()) {
                enterDecision();
                if (catchClause.getCatchBody() != null) {
                    catchClause.getCatchBody().accept(this);
                }
                exitDecision();
            }
            // Finally block doesn't add complexity (always executed)
            if (expression.getFinallyBlock() != null) {
                expression.getFinallyBlock().accept(this);
            }
            // Visit try block without additional decision increment
            if (expression.getTryBlock() != null) {
                expression.getTryBlock().accept(this);
            }
        }

        @Override
        public void visitBinaryExpression(@NotNull KtBinaryExpression expression) {
            // Count transitions between boolean operators (&&, ||)
            if (expression.getOperationToken() == KtTokens.ANDAND || 
                expression.getOperationToken() == KtTokens.OROR) {
                
                // Only count if there's a transition (different from previous operator)
                if (lastBooleanOp != null && lastBooleanOp != expression.getOperationToken()) {
                    complexity += 1;
                }
                lastBooleanOp = expression.getOperationToken();
            }
            
            // Elvis operator ?: is a conditional branch
            if (expression.getOperationToken() == KtTokens.ELVIS) {
                complexity += 1;
            }
            
            super.visitBinaryExpression(expression);
            
            // Reset after visiting the expression
            if (expression.getOperationToken() != KtTokens.ANDAND && 
                expression.getOperationToken() != KtTokens.OROR) {
                lastBooleanOp = null;
            }
        }

        @Override
        public void visitLambdaExpression(@NotNull KtLambdaExpression lambdaExpression) {
            // Lambda increases nesting for any decisions inside it
            nesting++;
            super.visitLambdaExpression(lambdaExpression);
            nesting = Math.max(0, nesting - 1);
        }

        @Override
        public void visitCallExpression(@NotNull KtCallExpression expression) {
            // Check for scope functions that increase cognitive complexity
            KtExpression calleeExpression = expression.getCalleeExpression();
            if (calleeExpression != null) {
                String calleeName = calleeExpression.getText();
                
                // Scope functions: let, also, apply, run, with
                Set<String> scopeFunctions = new HashSet<>();
                scopeFunctions.add("let");
                scopeFunctions.add("also");
                scopeFunctions.add("apply");
                scopeFunctions.add("run");
                scopeFunctions.add("with");
                
                if (scopeFunctions.contains(calleeName)) {
                    // Scope functions with lambda arguments increase nesting
                    if (!expression.getLambdaArguments().isEmpty()) {
                        // Nesting is handled by visitLambdaExpression
                    }
                }
                
                // Recursion detection: same name and arity
                if (owner != null && owner.getName() != null && owner.getName().equals(calleeName)) {
                    int args = expression.getValueArguments().size();
                    int params = owner.getValueParameters().size();
                    if (args == params) {
                        complexity += 1;
                    }
                }
            }
            super.visitCallExpression(expression);
        }

        @Override
        public void visitBreakExpression(@NotNull KtBreakExpression expression) {
            // Break with label adds complexity (non-linear flow)
            if (expression.getLabelName() != null) {
                complexity += 1;
            }
            super.visitBreakExpression(expression);
        }

        @Override
        public void visitContinueExpression(@NotNull KtContinueExpression expression) {
            // Continue with label adds complexity (non-linear flow)
            if (expression.getLabelName() != null) {
                complexity += 1;
            }
            super.visitContinueExpression(expression);
        }

        @Override
        public void visitReturnExpression(@NotNull KtReturnExpression expression) {
            // Return with label adds complexity (non-local return)
            if (expression.getLabelName() != null) {
                complexity += 1;
            }
            super.visitReturnExpression(expression);
        }

        @Override
        public void visitSafeQualifiedExpression(@NotNull KtSafeQualifiedExpression expression) {
            // Safe call operator ?. adds no complexity
            // It's designed for safety and readability, doesn't add cognitive load
            super.visitSafeQualifiedExpression(expression);
        }

        /**
         * Enters a decision structure, incrementing complexity by 1 + current nesting level.
         */
        private void enterDecision() {
            complexity += 1 + nesting;
            nesting++;
        }

        /**
         * Exits a decision structure, decrementing the nesting level.
         */
        private void exitDecision() {
            nesting = Math.max(0, nesting - 1);
        }
    }
}
