package org.b333vv.metric.model.visitor.kotlin.type;

import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.lexer.KtTokens;
import org.jetbrains.kotlin.psi.*;

import static org.b333vv.metric.model.metric.MetricType.WOC;

/**
 * Kotlin Weight Of A Class (WOC)
 * WOC = number of functional methods / total declared methods (excluding constructors).
 * Heuristics:
 * - Excludes property accessors (get/set) as non-functional.
 * - Excludes trivial delegations and empty bodies.
 */
public class KotlinWeightOfAClassVisitor extends KotlinClassVisitor {

    @Override
    public void visitClass(@NotNull KtClass klass) {
        long total = 0;
        long functional = 0;
        KtClassBody body = klass.getBody();
        if (body != null) {
            for (KtDeclaration d : body.getDeclarations()) {
                if (d instanceof KtNamedFunction) {
                    KtNamedFunction f = (KtNamedFunction) d;
                    total++;
                    if (isFunctional(f)) functional++;
                }
            }
        }
        if (total == 0) {
            metric = Metric.of(WOC, 0.00);
        } else {
            metric = Metric.of(WOC, Value.of((double) functional).divide(Value.of((double) total)));
        }
    }

    private boolean isFunctional(@NotNull KtNamedFunction f) {
        // Exclude simple property accessors by name pattern
        String name = f.getName();
        if (name != null) {
            if (name.startsWith("get") || name.startsWith("set") || name.startsWith("is")) {
                if (f.getValueParameters().isEmpty() || name.startsWith("set")) {
                    return false;
                }
            }
        }
        // Abstract or external have no body
        if (f.hasModifier(KtTokens.ABSTRACT_KEYWORD) || f.hasModifier(KtTokens.EXTERNAL_KEYWORD)) return false;
        KtBlockExpression body = f.getBodyBlockExpression();
        if (body == null) {
            // Expression body: treat as possibly trivial if it's just a reference or call
            KtExpression expr = f.getBodyExpression();
            if (expr == null) return false;
            if (expr instanceof KtNameReferenceExpression) return false; // return x
            if (expr instanceof KtCallExpression) return false; // return foo()
            return true;
        }
        KtExpression[] statements = body.getStatements().toArray(new KtExpression[0]);
        if (statements.length == 0) return false;
        if (statements.length > 1) return true;
        KtExpression s = statements[0];
        if (s instanceof KtReturnExpression) {
            KtExpression rv = ((KtReturnExpression) s).getReturnedExpression();
            if (rv instanceof KtNameReferenceExpression) return false;
            if (rv instanceof KtCallExpression) return false;
            return true;
        }
        if (s instanceof KtCallExpression) return false;
        if (s instanceof KtBinaryExpression) {
            KtBinaryExpression be = (KtBinaryExpression) s;
            if (be.getLeft() instanceof KtNameReferenceExpression && be.getRight() instanceof KtNameReferenceExpression) {
                return false; // x = y
            }
        }
        return true;
    }
}
