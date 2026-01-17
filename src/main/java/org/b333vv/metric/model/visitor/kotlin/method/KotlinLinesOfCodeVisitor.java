/*
 * Kotlin Lines Of Code - Phase 2.3.2
 */
package org.b333vv.metric.model.visitor.kotlin.method;

import org.b333vv.metric.model.metric.Metric;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.psi.*;

import static org.b333vv.metric.model.metric.MetricType.LOC;

/**
 * Computes Lines of Code (LOC) metric for Kotlin functions and constructors.
 * 
 * <p>
 * The LOC metric measures the number of actual code lines in a function,
 * excluding:
 * <ul>
 * <li>Single-line comments starting with //</li>
 * <li>Multi-line comments</li>
 * <li>KDoc comments</li>
 * <li>Blank lines containing only whitespace</li>
 * <li>Lines with only opening or closing braces</li>
 * </ul>
 * 
 * <p>
 * The metric counts:
 * <ul>
 * <li>Executable statements</li>
 * <li>Variable declarations and initializations</li>
 * <li>Control flow statements (if, when, for, while, etc.)</li>
 * <li>Expression statements</li>
 * <li>Function calls and property accesses</li>
 * <li>Lines containing code before single-line comments (e.g., "val x = 5 //
 * comment" counts as 1)</li>
 * <li>For expression-body functions: the expression itself (minimum 1
 * line)</li>
 * <li>For block-body functions: all code lines within the block</li>
 * </ul>
 * 
 * <p>
 * String literals spanning multiple lines are counted as multiple lines.
 * 
 * <p>
 * Examples:
 * 
 * <pre>
 * // Function with 3 LOC
 * fun calculate(x: Int): Int {
 *     val result = x * 2  // LOC = 1
 *     println(result)      // LOC = 2
 *     return result        // LOC = 3
 * }
 * 
 * // Expression-body function with 1 LOC
 * fun double(x: Int) = x * 2
 * 
 * // Function with comments and blank lines - 2 LOC
 * fun process(value: String) {
 *     // This is a comment
 *     val trimmed = value.trim()  // LOC = 1
 *     println(trimmed)             // LOC = 2
 * }
 * </pre>
 * 
 * @see org.b333vv.metric.model.visitor.kotlin.method.KotlinMethodVisitor
 * @see org.b333vv.metric.model.metric.MetricType#LOC
 */
public class KotlinLinesOfCodeVisitor extends KotlinMethodVisitor {

    @Override
    public void visitNamedFunction(@NotNull KtNamedFunction function) {
        KtExpression body = function.getBodyExpression();
        int lines = countLinesOfCode(body);
        metric = Metric.of(LOC, lines);
    }

    @Override
    public void visitSecondaryConstructor(@NotNull KtSecondaryConstructor constructor) {
        int lines = countLinesOfCode(constructor.getBodyExpression());
        metric = Metric.of(LOC, lines);
    }

    @Override
    public void visitPrimaryConstructor(@NotNull KtPrimaryConstructor constructor) {
        metric = Metric.of(LOC, 1);
    }

    @Override
    public void visitAnonymousInitializer(@NotNull KtAnonymousInitializer initializer) {
        int lines = countLinesOfCode(initializer.getBody());
        metric = Metric.of(LOC, lines);
    }

    /**
     * Counts lines of code in a Kotlin expression, excluding comments and blank
     * lines.
     * 
     * @param expr the expression to analyze (can be null)
     * @return the number of code lines
     */
    private int countLinesOfCode(KtExpression expr) {
        if (expr == null) {
            return 0;
        }

        String text = expr.getText();
        if (text == null || text.isEmpty()) {
            return 0;
        }

        String[] lines = text.split("\\n", -1);
        int codeLines = 0;
        boolean inMultiLineComment = false;
        boolean inStringLiteral = false;

        for (String line : lines) {
            String trimmed = line.trim();

            // Process character by character to handle comments and strings properly
            StringBuilder processedLine = new StringBuilder();
            boolean lineHasCode = false;

            for (int i = 0; i < trimmed.length(); i++) {
                char c = trimmed.charAt(i);
                char next = (i + 1 < trimmed.length()) ? trimmed.charAt(i + 1) : '\0';

                // Handle multi-line comment start
                if (!inStringLiteral && !inMultiLineComment && c == '/' && next == '*') {
                    inMultiLineComment = true;
                    i++; // skip next char
                    continue;
                }

                // Handle multi-line comment end
                if (inMultiLineComment && c == '*' && next == '/') {
                    inMultiLineComment = false;
                    i++; // skip next char
                    continue;
                }

                // Skip if inside multi-line comment
                if (inMultiLineComment) {
                    continue;
                }

                // Handle single-line comment
                if (!inStringLiteral && c == '/' && next == '/') {
                    // Rest of line is comment
                    break;
                }

                // Handle string literals (simplified - doesn't handle all edge cases)
                if (c == '"' && (i == 0 || trimmed.charAt(i - 1) != '\\')) {
                    inStringLiteral = !inStringLiteral;
                    processedLine.append(c);
                    lineHasCode = true;
                    continue;
                }

                // Add character to processed line
                processedLine.append(c);
                if (!Character.isWhitespace(c)) {
                    lineHasCode = true;
                }
            }

            // Check if line has actual code (not just braces or whitespace)
            String finalProcessed = processedLine.toString().trim();
            if (lineHasCode && !finalProcessed.isEmpty()) {
                // Exclude lines with only opening/closing braces
                if (!finalProcessed.equals("{") && !finalProcessed.equals("}")) {
                    codeLines++;
                }
            }
        }

        // Ensure at least 1 line for expression-body functions
        if (codeLines == 0 && !(expr instanceof KtBlockExpression)) {
            // Expression-body function
            String trimmedText = text.trim();
            if (!trimmedText.isEmpty()) {
                return 1;
            }
        }

        return codeLines;
    }
}
