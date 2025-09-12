/*
 * Kotlin Lines Of Code - Phase 2.3.2
 */
package org.b333vv.metric.model.visitor.kotlin.method;

import org.b333vv.metric.model.metric.Metric;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.psi.*;

import static org.b333vv.metric.model.metric.MetricType.LOC;

/**
 * Computes lines of code for Kotlin functions.
 * Handling policy:
 *  - Block-body: count newline characters within the body braces
 *  - Expression-body: count as 1 line by default, plus nested newlines if any
 */
public class KotlinLinesOfCodeVisitor extends KotlinMethodVisitor {

    @Override
    public void visitNamedFunction(@NotNull KtNamedFunction function) {
        KtExpression body = function.getBodyExpression();
        int lines = countLines(body);
        metric = Metric.of(LOC, lines);
    }

    @Override
    public void visitSecondaryConstructor(@NotNull KtSecondaryConstructor constructor) {
        metric = Metric.of(LOC, countLines(constructor.getBodyExpression()));
    }

    private int countLines(KtExpression expr) {
        if (expr == null) return 0;
        String text = expr.getText();
        if (text == null || text.isEmpty()) return 0;
        int lines = 1;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '\n') lines++;
        }
        return lines;
    }
}
