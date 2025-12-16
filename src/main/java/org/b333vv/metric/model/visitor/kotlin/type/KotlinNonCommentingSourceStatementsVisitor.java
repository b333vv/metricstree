/*
 * Kotlin Non-Commenting Source Statements (NCSS) - Enhanced implementation
 */
package org.b333vv.metric.model.visitor.kotlin.type;

import org.b333vv.metric.model.metric.Metric;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.lexer.KtTokens;
import org.jetbrains.kotlin.psi.*;

import static org.b333vv.metric.model.metric.MetricType.NCSS;

/**
 * Visitor that counts Non-Commenting Source Statements (NCSS) for Kotlin classes.
 * 
 * <p>The NCSS metric measures the size of source code by counting executable statements,
 * declarations, and control flow constructs while excluding comments and whitespace.
 * This implementation is adapted for Kotlin's language features and PSI structure.
 * 
 * <h2>Counted Statement Types</h2>
 * 
 * <h3>Type Declarations</h3>
 * <ul>
 *   <li><b>Class declarations</b> - regular classes, data classes, sealed classes, enum classes</li>
 *   <li><b>Object declarations</b> - singleton objects, companion objects</li>
 *   <li><b>Object expressions</b> - anonymous object instances</li>
 *   <li><b>Type alias declarations</b> - typealias statements</li>
 * </ul>
 * 
 * <h3>Member Declarations</h3>
 * <ul>
 *   <li><b>Properties</b> - val/var declarations (including delegated properties)</li>
 *   <li><b>Destructuring declarations</b> - val (x, y) = pair</li>
 *   <li><b>Named functions</b> - regular functions, extension functions, operator functions</li>
 *   <li><b>Constructors</b> - primary and secondary constructors</li>
 *   <li><b>Init blocks</b> - class initialization blocks</li>
 *   <li><b>Anonymous initializers</b> - initialization code blocks</li>
 * </ul>
 * 
 * <h3>Control Flow Statements</h3>
 * <ul>
 *   <li><b>Loops</b> - for, while, do-while expressions</li>
 *   <li><b>Conditional statements</b> - if expressions (else branches counted separately)</li>
 *   <li><b>When expressions</b> - each when entry (case) is counted</li>
 *   <li><b>Jump statements</b> - return, throw, break, continue (including labeled variants)</li>
 *   <li><b>Try-catch-finally</b> - each catch clause and finally block counted separately</li>
 * </ul>
 * 
 * <h3>Expressions as Statements</h3>
 * <ul>
 *   <li><b>Assignment expressions</b> - all assignment operators (=, +=, -=, etc.)</li>
 *   <li><b>Unary operations</b> - prefix/postfix increment/decrement (++, --) as statements</li>
 *   <li><b>Call expressions</b> - function/method calls at statement level</li>
 *   <li><b>Qualified expressions</b> - property access and method calls with receivers</li>
 *   <li><b>Lambda expressions</b> - lambda literals used as statements</li>
 * </ul>
 * 
 * <h3>Not Counted</h3>
 * <ul>
 *   <li>Comments (single-line, multi-line, KDoc)</li>
 *   <li>Package statements</li>
 *   <li>Import statements</li>
 *   <li>Whitespace and formatting</li>
 *   <li>Annotations (considered metadata, not executable statements)</li>
 *   <li>Type parameter declarations</li>
 *   <li>Pure expressions used as values (not statement context)</li>
 * </ul>
 * 
 * <h2>Implementation Notes</h2>
 * <p>This visitor traverses the Kotlin PSI tree and accumulates statement counts.
 * The implementation handles Kotlin-specific constructs including:
 * <ul>
 *   <li>Expression-oriented syntax (if, when, try as expressions)</li>
 *   <li>Property delegation (by lazy, observable, etc.)</li>
 *   <li>Extension functions and receivers</li>
 *   <li>Object-oriented features (objects, companions, data classes)</li>
 *   <li>Destructuring and multi-variable declarations</li>
 * </ul>
 * 
 * @author b333vv
 * @version 2.0
 * @see org.b333vv.metric.model.metric.MetricType#NCSS
 */
public class KotlinNonCommentingSourceStatementsVisitor extends KotlinClassVisitor {
    private int statements = 0;

    @Override
    public void visitClass(@NotNull KtClass klass) {
        int prev = statements;
        // Count the class declaration itself
        statements += 1;

        super.visitClass(klass);

        metric = Metric.of(NCSS, statements);
        statements = prev;
    }

    @Override
    public void visitObjectDeclaration(@NotNull KtObjectDeclaration declaration) {
        // Count object declarations (singleton objects, companion objects)
        statements += 1;
        super.visitObjectDeclaration(declaration);
    }

    @Override
    public void visitObjectLiteralExpression(@NotNull KtObjectLiteralExpression expression) {
        // Count anonymous object expressions
        statements += 1;
        super.visitObjectLiteralExpression(expression);
    }

    @Override
    public void visitTypeAlias(@NotNull KtTypeAlias typeAlias) {
        // Count type alias declarations
        statements += 1;
        super.visitTypeAlias(typeAlias);
    }

    @Override
    public void visitProperty(@NotNull KtProperty property) {
        // Count property declaration (val/var, including delegated properties)
        statements += 1;
        super.visitProperty(property);
    }

    @Override
    public void visitDestructuringDeclaration(@NotNull KtDestructuringDeclaration declaration) {
        // Count destructuring declarations like: val (x, y) = pair
        statements += 1;
        super.visitDestructuringDeclaration(declaration);
    }

    @Override
    public void visitNamedFunction(@NotNull KtNamedFunction function) {
        // Count function declaration (including extension functions, operators)
        statements += 1;
        super.visitNamedFunction(function);
    }

    @Override
    public void visitPrimaryConstructor(@NotNull KtPrimaryConstructor constructor) {
        // Count primary constructor declaration
        statements += 1;
        super.visitPrimaryConstructor(constructor);
    }

    @Override
    public void visitSecondaryConstructor(@NotNull KtSecondaryConstructor constructor) {
        // Count secondary constructor declaration
        statements += 1;
        super.visitSecondaryConstructor(constructor);
    }

    @Override
    public void visitClassInitializer(@NotNull KtClassInitializer initializer) {
        // Count init blocks in classes
        statements += 1;
        super.visitClassInitializer(initializer);
    }

    @Override
    public void visitAnonymousInitializer(@NotNull KtAnonymousInitializer initializer) {
        // Count anonymous initialization blocks
        statements += 1;
        super.visitAnonymousInitializer(initializer);
    }

    @Override
    public void visitIfExpression(@NotNull KtIfExpression expression) {
        super.visitIfExpression(expression);
        if (expression.getElse() != null) {
            // Count 'else' branch as separate statement
            statements += 1;
        }
    }

    @Override
    public void visitTryExpression(@NotNull KtTryExpression expression) {
        super.visitTryExpression(expression);
        // Count each catch clause
        statements += Math.max(0, expression.getCatchClauses().size());
        // Count finally block if present
        if (expression.getFinallyBlock() != null) {
            statements += 1;
        }
    }

    @Override
    public void visitWhenExpression(@NotNull KtWhenExpression expression) {
        super.visitWhenExpression(expression);
        // Each when entry (case/default) counts as a statement
        statements += expression.getEntries().size();
    }

    @Override
    public void visitForExpression(@NotNull KtForExpression expression) {
        statements += 1;
        super.visitForExpression(expression);
    }

    @Override
    public void visitWhileExpression(@NotNull KtWhileExpression expression) {
        statements += 1;
        super.visitWhileExpression(expression);
    }

    @Override
    public void visitDoWhileExpression(@NotNull KtDoWhileExpression expression) {
        statements += 1;
        super.visitDoWhileExpression(expression);
    }

    @Override
    public void visitReturnExpression(@NotNull KtReturnExpression expression) {
        statements += 1;
        super.visitReturnExpression(expression);
    }

    @Override
    public void visitThrowExpression(@NotNull KtThrowExpression expression) {
        statements += 1;
        super.visitThrowExpression(expression);
    }

    @Override
    public void visitBreakExpression(@NotNull KtBreakExpression expression) {
        statements += 1;
        super.visitBreakExpression(expression);
    }

    @Override
    public void visitContinueExpression(@NotNull KtContinueExpression expression) {
        statements += 1;
        super.visitContinueExpression(expression);
    }

    @Override
    public void visitBinaryExpression(@NotNull KtBinaryExpression expression) {
        // Count assignment expressions as statements
        if (KtTokens.ALL_ASSIGNMENTS.contains(expression.getOperationToken())) {
            statements += 1;
        }
        super.visitBinaryExpression(expression);
    }

    @Override
    public void visitUnaryExpression(@NotNull KtUnaryExpression expression) {
        // Count postfix/prefix increment/decrement as statements when used standalone
        if (expression.getParent() instanceof KtBlockExpression) {
            if (KtTokens.INCREMENT.equals(expression.getOperationToken()) ||
                KtTokens.DECREMENT.equals(expression.getOperationToken())) {
                statements += 1;
            }
        }
        super.visitUnaryExpression(expression);
    }

    @Override
    public void visitCallExpression(@NotNull KtCallExpression expression) {
        // Count top-level call expressions in a block as statements
        if (expression.getParent() instanceof KtBlockExpression ||
            expression.getParent() instanceof KtDotQualifiedExpression && 
            expression.getParent().getParent() instanceof KtBlockExpression) {
            statements += 1;
        }
        super.visitCallExpression(expression);
    }

    @Override
    public void visitDotQualifiedExpression(@NotNull KtDotQualifiedExpression expression) {
        // Count qualified expressions (method calls, property access) as statements
        // when they appear at statement level in blocks
        if (expression.getParent() instanceof KtBlockExpression) {
            // Avoid double-counting if selector is already a call expression
            if (!(expression.getSelectorExpression() instanceof KtCallExpression)) {
                statements += 1;
            }
        }
        super.visitDotQualifiedExpression(expression);
    }

    @Override
    public void visitLambdaExpression(@NotNull KtLambdaExpression expression) {
        // Count lambda expressions when used as standalone statements
        if (expression.getParent() instanceof KtBlockExpression) {
            statements += 1;
        }
        super.visitLambdaExpression(expression);
    }
}
