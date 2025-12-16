package org.b333vv.metric.model.visitor.kotlin.type;

import org.b333vv.metric.model.metric.Metric;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.lexer.KtTokens;
import org.jetbrains.kotlin.psi.*;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import static org.b333vv.metric.model.metric.MetricType.*;

/**
 * Visitor for calculating Halstead complexity metrics for Kotlin classes.
 * <p>
 * Halstead metrics are based on the count of operators and operands in source code.
 * This visitor analyzes Kotlin PSI (Program Structure Interface) tree to extract
 * these elements and compute the following metrics:
 * <ul>
 *   <li>CHEF (Effort) - Mental effort required to develop/understand the code</li>
 *   <li>CHER (Errors) - Estimated number of errors in implementation</li>
 *   <li>CHVL (Volume) - Size of the implementation</li>
 *   <li>CHD (Difficulty) - Difficulty level of the program</li>
 *   <li>CHVC (Vocabulary) - Number of unique operators and operands</li>
 *   <li>CHL (Length) - Total number of operators and operands</li>
 * </ul>
 * 
 * <h2>Operators counted:</h2>
 * <ul>
 *   <li><b>Arithmetic operators:</b> +, -, *, /, %, ++, --</li>
 *   <li><b>Comparison operators:</b> ==, !=, <, >, <=, >=, ===, !==</li>
 *   <li><b>Logical operators:</b> &&, ||, !</li>
 *   <li><b>Assignment operators:</b> =, +=, -=, *=, /=, %=</li>
 *   <li><b>Bitwise operators:</b> and, or, xor, inv, shl, shr, ushr</li>
 *   <li><b>Range operators:</b> .., ..<, downTo, until, step</li>
 *   <li><b>Null-safety operators:</b> ?., ?:, !!, as?, is, !is</li>
 *   <li><b>Member access:</b> . (property/method access)</li>
 *   <li><b>Indexing:</b> [] (array/collection access)</li>
 *   <li><b>Invocation:</b> () (function call)</li>
 *   <li><b>Type operations:</b> as, is, in, !in</li>
 *   <li><b>Lambda arrow:</b> -></li>
 *   <li><b>Destructuring:</b> component operators in destructuring declarations</li>
 *   <li><b>Keywords acting as operators:</b> if, when, for, while, do, try, catch, finally, throw, return, break, continue</li>
 *   <li><b>Declaration keywords:</b> fun, val, var, class, object, interface, enum, annotation</li>
 *   <li><b>Function/method calls:</b> Each distinct function name is counted as a unique operator</li>
 * </ul>
 * 
 * <h2>Operands counted:</h2>
 * <ul>
 *   <li><b>Identifiers:</b> Variable names, property names, parameter names</li>
 *   <li><b>Literals:</b> Numbers (integer, floating-point), booleans (true, false), null</li>
 *   <li><b>String literals:</b> String constants and string templates (counted as single operand)</li>
 *   <li><b>Class references:</b> Class names, type names</li>
 *   <li><b>Function parameters:</b> Arguments passed to functions</li>
 *   <li><b>Lambda parameters:</b> Parameters in lambda expressions</li>
 *   <li><b>Labels:</b> Named labels used with break/continue/return</li>
 * </ul>
 * 
 * <h2>Special handling:</h2>
 * <ul>
 *   <li>Lambda expressions: The lambda arrow (->) is counted as operator, parameters and body are analyzed</li>
 *   <li>String templates: Entire string (including interpolations) counted as single operand</li>
 *   <li>Function calls: Function name is operator, arguments are operands</li>
 *   <li>Property access: Dot operator (.) counted separately from property name</li>
 *   <li>Qualified expressions: Each level of qualification adds operators</li>
 *   <li>When expressions: 'when' keyword and arrow (->) for each branch counted as operators</li>
 *   <li>Destructuring: Each component access counted as operator</li>
 * </ul>
 * 
 * @see <a href="https://en.wikipedia.org/wiki/Halstead_complexity_measures">Halstead complexity measures</a>
 */
public class KotlinHalsteadClassVisitor extends KotlinClassVisitor {

    private int numberOfOperands;
    private int numberOfOperators;
    private final Set<String> operators = new HashSet<>();
    private final Set<String> operands = new HashSet<>();

    // Kotlin-specific operators that should be recognized
    private static final Set<String> KOTLIN_OPERATORS = Set.of(
            // Null-safety operators
            "?.", "?:", "!!", "as?",
            // Range operators  
            "..", "..<", "downTo", "until", "step",
            // Type check operators
            "is", "!is", "as",
            // Membership operators
            "in", "!in",
            // Lambda arrow
            "->",
            // Bitwise operators (infix functions)
            "and", "or", "xor", "inv", "shl", "shr", "ushr"
    );

    private int distinctOperands() { return operands.size(); }
    private int distinctOperators() { return operators.size(); }

    private long length() { return numberOfOperands + numberOfOperators; }
    private long vocabulary() { return (long) operands.size() + (long) operators.size(); }

    private double difficulty() {
        final int N2 = numberOfOperands;
        final int n1 = distinctOperators();
        final int n2 = distinctOperands();
        return n2 == 0 ? 0.0 : ((double) n1 / 2.0) * ((double) N2 / (double) n2);
    }

    private double volume() { 
        return (double) length() * Math.log(Math.max(1, vocabulary())) / Math.log(2.0); 
    }
    
    private double effort() { 
        return difficulty() * volume(); 
    }
    
    private double errors() { 
        return Math.pow(effort(), 2.0 / 3.0) / 3000.0; 
    }

    @Override
    public void visitClass(@NotNull KtClass klass) {
        numberOfOperands = 0;
        numberOfOperators = 0;
        operators.clear();
        operands.clear();

        // Visit class body expressions
        KtClassBody body = klass.getBody();
        if (body != null) {
            body.accept(new KtTreeVisitorVoid() {
                
                @Override
                public void visitBinaryExpression(@NotNull KtBinaryExpression expression) {
                    KtOperationReferenceExpression opRef = expression.getOperationReference();
                    String op = opRef.getText();
                    registerOperator(op);
                    super.visitBinaryExpression(expression);
                }

                @Override
                public void visitUnaryExpression(@NotNull KtUnaryExpression expression) {
                    String op = expression.getOperationReference().getText();
                    registerOperator(op);
                    super.visitUnaryExpression(expression);
                }

                @Override
                public void visitPrefixExpression(@NotNull KtPrefixExpression expression) {
                    String op = expression.getOperationReference().getText();
                    registerOperator(op);
                    super.visitPrefixExpression(expression);
                }

                @Override
                public void visitPostfixExpression(@NotNull KtPostfixExpression expression) {
                    String op = expression.getOperationReference().getText();
                    registerOperator(op);
                    super.visitPostfixExpression(expression);
                }

                @Override
                public void visitBinaryWithTypeRHSExpression(@NotNull KtBinaryExpressionWithTypeRHS expression) {
                    // Handle 'as', 'as?' type cast operators
                    String op = expression.getOperationReference().getText();
                    registerOperator(op);
                    super.visitBinaryWithTypeRHSExpression(expression);
                }

                @Override
                public void visitIsExpression(@NotNull KtIsExpression expression) {
                    // Handle 'is', '!is' type check operators
                    registerOperator(expression.isNegated() ? "!is" : "is");
                    super.visitIsExpression(expression);
                }

                @Override
                public void visitCallExpression(@NotNull KtCallExpression expression) {
                    // Function call: function name is operator, arguments are operands
                    KtExpression callee = expression.getCalleeExpression();
                    if (callee != null) {
                        String functionName = callee.getText();
                        registerOperator(functionName);
                    }
                    // Count parentheses as invocation operator
                    registerOperator("()");
                    super.visitCallExpression(expression);
                }

                @Override
                public void visitArrayAccessExpression(@NotNull KtArrayAccessExpression expression) {
                    // Array/collection indexing operator
                    registerOperator("[]");
                    super.visitArrayAccessExpression(expression);
                }

                @Override
                public void visitDotQualifiedExpression(@NotNull KtDotQualifiedExpression expression) {
                    // Member access operator
                    registerOperator(".");
                    super.visitDotQualifiedExpression(expression);
                }

                @Override
                public void visitSafeQualifiedExpression(@NotNull KtSafeQualifiedExpression expression) {
                    // Safe call operator
                    registerOperator("?.");
                    super.visitSafeQualifiedExpression(expression);
                }

                @Override
                public void visitIfExpression(@NotNull KtIfExpression expression) {
                    registerOperator("if");
                    if (expression.getElse() != null) {
                        registerOperator("else");
                    }
                    super.visitIfExpression(expression);
                }

                @Override
                public void visitWhenExpression(@NotNull KtWhenExpression expression) {
                    registerOperator("when");
                    // Each when entry has an arrow operator
                    int entryCount = expression.getEntries().size();
                    for (int i = 0; i < entryCount; i++) {
                        registerOperator("->");
                    }
                    super.visitWhenExpression(expression);
                }

                @Override
                public void visitForExpression(@NotNull KtForExpression expression) {
                    registerOperator("for");
                    registerOperator("in");
                    super.visitForExpression(expression);
                }

                @Override
                public void visitWhileExpression(@NotNull KtWhileExpression expression) {
                    registerOperator("while");
                    super.visitWhileExpression(expression);
                }

                @Override
                public void visitDoWhileExpression(@NotNull KtDoWhileExpression expression) {
                    registerOperator("do");
                    registerOperator("while");
                    super.visitDoWhileExpression(expression);
                }

                @Override
                public void visitTryExpression(@NotNull KtTryExpression expression) {
                    registerOperator("try");
                    for (KtCatchClause catchClause : expression.getCatchClauses()) {
                        registerOperator("catch");
                    }
                    if (expression.getFinallyBlock() != null) {
                        registerOperator("finally");
                    }
                    super.visitTryExpression(expression);
                }

                @Override
                public void visitThrowExpression(@NotNull KtThrowExpression expression) {
                    registerOperator("throw");
                    super.visitThrowExpression(expression);
                }

                @Override
                public void visitReturnExpression(@NotNull KtReturnExpression expression) {
                    registerOperator("return");
                    super.visitReturnExpression(expression);
                }

                @Override
                public void visitBreakExpression(@NotNull KtBreakExpression expression) {
                    registerOperator("break");
                    super.visitBreakExpression(expression);
                }

                @Override
                public void visitContinueExpression(@NotNull KtContinueExpression expression) {
                    registerOperator("continue");
                    super.visitContinueExpression(expression);
                }

                @Override
                public void visitLambdaExpression(@NotNull KtLambdaExpression expression) {
                    // Lambda arrow is an operator
                    registerOperator("->");
                    super.visitLambdaExpression(expression);
                }

                @Override
                public void visitNamedFunction(@NotNull KtNamedFunction function) {
                    registerOperator("fun");
                    super.visitNamedFunction(function);
                }

                @Override
                public void visitProperty(@NotNull KtProperty property) {
                    registerOperator(property.isVar() ? "var" : "val");
                    super.visitProperty(property);
                }

                @Override
                public void visitObjectDeclaration(@NotNull KtObjectDeclaration declaration) {
                    registerOperator("object");
                    super.visitObjectDeclaration(declaration);
                }

                @Override
                public void visitSimpleNameExpression(@NotNull KtSimpleNameExpression expression) {
                    // Identifiers are operands (variables, properties, etc.)
                    String text = expression.getText();
                    if (text != null && !text.isEmpty() && !isOperatorKeyword(text)) {
                        registerOperand(text);
                    }
                    super.visitSimpleNameExpression(expression);
                }

                @Override
                public void visitStringTemplateExpression(@NotNull KtStringTemplateExpression expression) {
                    // String literals (including templates) counted as single operand
                    registerOperand("\"string\"");
                    super.visitStringTemplateExpression(expression);
                }

                @Override
                public void visitConstantExpression(@NotNull KtConstantExpression expression) {
                    // Literals: numbers, booleans, null
                    String text = expression.getText();
                    if (text != null && !text.isEmpty()) {
                        registerOperand(text);
                    }
                    super.visitConstantExpression(expression);
                }

                @Override
                public void visitTypeReference(@NotNull KtTypeReference typeReference) {
                    // Type names in declarations are operands
                    String typeName = typeReference.getText();
                    if (typeName != null && !typeName.isEmpty()) {
                        registerOperand(typeName);
                    }
                    super.visitTypeReference(typeReference);
                }

                @Override
                public void visitDestructuringDeclaration(@NotNull KtDestructuringDeclaration declaration) {
                    // Each component in destructuring is an operator
                    int componentCount = declaration.getEntries().size();
                    for (int i = 0; i < componentCount; i++) {
                        registerOperator("component" + (i + 1));
                    }
                    super.visitDestructuringDeclaration(declaration);
                }
            });
        }

        metric = Metric.of(CHVL, volume());
    }

    /**
     * Builds the complete list of Halstead metrics for the analyzed class.
     * 
     * @return List containing all six Halstead metrics: effort, errors, volume, 
     *         difficulty, vocabulary, and length
     */
    public java.util.List<Metric> buildMetrics() {
        java.util.List<Metric> list = new LinkedList<>();
        list.add(Metric.of(CHEF, effort()));
        list.add(Metric.of(CHER, errors()));
        list.add(Metric.of(CHVL, volume()));
        list.add(Metric.of(CHD, difficulty()));
        list.add(Metric.of(CHVC, vocabulary()));
        list.add(Metric.of(CHL, length()));
        return list;
    }

    private void registerOperator(String operator) {
        if (operator == null || operator.trim().isEmpty()) return;
        numberOfOperators++;
        operators.add(operator);
    }

    private void registerOperand(String operand) {
        if (operand == null || operand.trim().isEmpty()) return;
        numberOfOperands++;
        operands.add(operand);
    }

    /**
     * Checks if a text represents a Kotlin operator keyword to avoid 
     * counting it as both operator and operand.
     */
    private boolean isOperatorKeyword(String text) {
        return KOTLIN_OPERATORS.contains(text) ||
               "if".equals(text) || "else".equals(text) ||
               "when".equals(text) || "for".equals(text) ||
               "while".equals(text) || "do".equals(text) ||
               "try".equals(text) || "catch".equals(text) || "finally".equals(text) ||
               "throw".equals(text) || "return".equals(text) ||
               "break".equals(text) || "continue".equals(text) ||
               "fun".equals(text) || "val".equals(text) || "var".equals(text) ||
               "object".equals(text) || "class".equals(text) || "interface".equals(text);
    }
}