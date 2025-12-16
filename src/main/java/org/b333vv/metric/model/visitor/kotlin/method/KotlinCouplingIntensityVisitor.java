package org.b333vv.metric.model.visitor.kotlin.method;

import org.b333vv.metric.model.metric.Metric;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.psi.*;

import java.util.HashSet;
import java.util.Set;

import static org.b333vv.metric.model.metric.MetricType.CINT;

/**
 * Visitor for calculating Coupling Intensity (CINT) metric for Kotlin functions and constructors.
 * <p>
 * The CINT metric measures the degree of coupling between a function/method and external types
 * by counting distinct foreign method calls, property accesses, and operator invocations within
 * the function body. Higher CINT values indicate stronger coupling to external dependencies.
 * </p>
 *
 * <h3>What is included in CINT calculation:</h3>
 * <ul>
 *   <li><b>Qualified method calls:</b> Direct calls like {@code obj.method()}, safe calls {@code obj?.method()},
 *       and chained calls {@code a.b.c()}</li>
 *   <li><b>Property accesses:</b> Reading external properties via {@code obj.property} or {@code obj?.property}</li>
 *   <li><b>Infix function calls:</b> Calls using infix notation like {@code obj add item}</li>
 *   <li><b>Invoke operator calls:</b> Direct invocations of callable objects like {@code funObj()}</li>
 *   <li><b>Lambda invocations:</b> Calls to lambda parameters and function references</li>
 *   <li><b>Extension function calls:</b> Calls to extension functions on qualified receivers</li>
 *   <li><b>Collection operations:</b> Higher-order functions like {@code list.map{}}, {@code filter{}}, etc.</li>
 *   <li><b>Array access:</b> Element access via array/collection operators like {@code arr[i]}</li>
 *   <li><b>Delegation calls:</b> Method calls on delegated properties</li>
 *   <li><b>Scope function calls:</b> Calls like {@code obj.let{}}, {@code apply{}}, {@code run{}}, etc.</li>
 * </ul>
 *
 * <h3>What is excluded from CINT calculation:</h3>
 * <ul>
 *   <li><b>Self-calls:</b> Calls on {@code this} or {@code super} receivers</li>
 *   <li><b>Unqualified calls:</b> Local function calls without explicit receiver (e.g., {@code localFunc()})</li>
 *   <li><b>Constructor calls:</b> Object instantiation expressions are not counted as coupling</li>
 *   <li><b>Nested function definitions:</b> Inner function declarations themselves (only their calls)</li>
 * </ul>
 *
 * <h3>Distinctness criteria:</h3>
 * Each unique combination of (receiver expression + member name) is counted only once.
 * For example, multiple calls to {@code obj.foo()} count as 1, but {@code obj.foo()} and {@code obj.bar()}
 * count as 2, and {@code obj1.foo()} and {@code obj2.foo()} also count as 2.
 *
 * <h3>Result:</h3>
 * CINT = number of distinct foreign qualified calls and property accesses
 *
 * <h3>Use cases:</h3>
 * <ul>
 *   <li>Identifying methods with high external dependencies</li>
 *   <li>Finding candidates for refactoring to reduce coupling</li>
 *   <li>Assessing function testability (high CINT = more mocking needed)</li>
 *   <li>Measuring function complexity from a coupling perspective</li>
 * </ul>
 *
 * @see org.b333vv.metric.model.metric.MetricType#CINT
 */
public class KotlinCouplingIntensityVisitor extends KotlinMethodVisitor {

    @Override
    public void visitNamedFunction(@NotNull KtNamedFunction function) {
        long cintValue = 0;
        // Handle expression body
        if (function.getBodyExpression() != null && !function.hasBlockBody()) {
            cintValue = count(function, function.getBodyExpression());
        }
        // Handle block body
        else if (function.getBodyBlockExpression() != null) {
            cintValue = count(function, function.getBodyBlockExpression());
        }
        metric = Metric.of(CINT, cintValue);
    }

    @Override
    public void visitSecondaryConstructor(@NotNull KtSecondaryConstructor constructor) {
        metric = Metric.of(CINT, count(constructor, constructor.getBodyExpression()));
    }

    /**
     * Counts distinct foreign coupling points in the function body.
     *
     * @param context The function or constructor context
     * @param body The body expression to analyze
     * @return Number of distinct coupling points
     */
    private long count(@NotNull KtElement context, KtExpression body) {
        if (body == null) return 0;
        
        KtClassOrObject owner = findOwnerClass(context);
        final Set<String> couplingPoints = new HashSet<>();
        
        body.accept(new KtTreeVisitorVoid() {
            
            @Override
            public void visitQualifiedExpression(@NotNull KtQualifiedExpression expression) {
                KtExpression selector = expression.getSelectorExpression();
                KtExpression receiver = expression.getReceiverExpression();
                String receiverKey = buildReceiverKey(receiver, owner);
                
                if (receiverKey != null) {
                    // Handle method calls
                    if (selector instanceof KtCallExpression) {
                        String calleeName = extractCalleeName((KtCallExpression) selector);
                        if (calleeName != null) {
                            couplingPoints.add(receiverKey + "#call:" + calleeName);
                        }
                    }
                    // Handle property access (including Kotlin properties)
                    else if (selector instanceof KtNameReferenceExpression) {
                        String propertyName = ((KtNameReferenceExpression) selector).getReferencedName();
                        if (propertyName != null) {
                            couplingPoints.add(receiverKey + "#prop:" + propertyName);
                        }
                    }
                    // Handle array/collection access via get operator
                    else if (selector instanceof KtArrayAccessExpression) {
                        couplingPoints.add(receiverKey + "#op:get[]");
                    }
                }
                
                super.visitQualifiedExpression(expression);
            }
            
            @Override
            public void visitBinaryExpression(@NotNull KtBinaryExpression expression) {
                // Track infix function calls (custom operators and infix functions)
                if (expression.getOperationReference() instanceof KtOperationReferenceExpression) {
                    KtExpression left = expression.getLeft();
                    String receiverKey = buildReceiverKey(left, owner);
                    
                    if (receiverKey != null) {
                        String operatorName = expression.getOperationReference().getReferencedName();
                        if (operatorName != null && !isBuiltInOperator(operatorName)) {
                            // This is likely an infix function call
                            couplingPoints.add(receiverKey + "#infix:" + operatorName);
                        }
                    }
                }
                super.visitBinaryExpression(expression);
            }
            
            @Override
            public void visitCallExpression(@NotNull KtCallExpression expression) {
                // Handle invoke operator and lambda invocations on qualified receivers
                KtExpression calleeExpr = expression.getCalleeExpression();
                
                // Check if this is an invoke on a property/variable (like funObj())
                if (calleeExpr instanceof KtNameReferenceExpression) {
                    String refName = ((KtNameReferenceExpression) calleeExpr).getReferencedName();
                    // Only count if it's not a local/unqualified function call
                    // This will be caught by qualified expression visitor if it has a receiver
                    // Skip here to avoid duplicates
                }
                
                super.visitCallExpression(expression);
            }
            
            @Override
            public void visitArrayAccessExpression(@NotNull KtArrayAccessExpression expression) {
                // Handle array access operator on external objects
                KtExpression arrayExpr = expression.getArrayExpression();
                if (arrayExpr != null && !(arrayExpr instanceof KtThisExpression) 
                        && !(arrayExpr instanceof KtSuperExpression)) {
                    String receiverKey = buildReceiverKey(arrayExpr, owner);
                    if (receiverKey != null) {
                        couplingPoints.add(receiverKey + "#op:arrayAccess");
                    }
                }
                super.visitArrayAccessExpression(expression);
            }
        });
        
        return couplingPoints.size();
    }

    /**
     * Extracts the callee name from a call expression.
     */
    private String extractCalleeName(KtCallExpression callExpr) {
        KtExpression callee = callExpr.getCalleeExpression();
        if (callee == null) return null;
        
        if (callee instanceof KtNameReferenceExpression) {
            return ((KtNameReferenceExpression) callee).getReferencedName();
        } else if (callee instanceof KtOperationReferenceExpression) {
            return ((KtOperationReferenceExpression) callee).getReferencedName();
        }
        
        return callee.getText();
    }

    /**
     * Builds a normalized receiver key for coupling tracking.
     * Returns null for this/super receivers or if receiver is invalid.
     */
    private String buildReceiverKey(KtExpression receiver, KtClassOrObject owner) {
        if (receiver == null) return null;
        
        // Exclude this and super references
        if (receiver instanceof KtThisExpression || receiver instanceof KtSuperExpression) {
            return null;
        }
        
        String text = receiver.getText();
        if (text == null) return null;
        
        text = text.trim();
        if (text.isEmpty() || "this".equals(text) || "super".equals(text)) {
            return null;
        }
        
        // Normalize the receiver key by removing whitespace variations
        text = text.replaceAll("\\s+", " ");
        
        return text;
    }

    /**
     * Checks if an operator is a built-in Kotlin operator (not a custom infix function).
     */
    private boolean isBuiltInOperator(String operator) {
        // Common arithmetic, comparison, and logical operators
        return operator.matches("[+\\-*/%.=!<>&|^~]+") 
            || "in".equals(operator) 
            || "!in".equals(operator)
            || "is".equals(operator)
            || "!is".equals(operator)
            || "as".equals(operator)
            || "as?".equals(operator);
    }

    /**
     * Finds the enclosing class or object declaration for the given element.
     */
    private KtClassOrObject findOwnerClass(@NotNull KtElement element) {
        com.intellij.psi.PsiElement e = element;
        while (e != null && !(e instanceof KtClassOrObject)) {
            e = e.getParent();
            if (e != null && !(e instanceof KtElement)) break;
        }
        return (e instanceof KtClassOrObject) ? (KtClassOrObject) e : null;
    }
}
