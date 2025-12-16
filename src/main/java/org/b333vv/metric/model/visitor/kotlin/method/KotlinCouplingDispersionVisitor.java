package org.b333vv.metric.model.visitor.kotlin.method;

import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.psi.*;

import java.util.HashSet;
import java.util.Set;

import static org.b333vv.metric.model.metric.MetricType.CDISP;

/**
 * Kotlin Coupling Dispersion (CDISP) metric calculator.
 * <p>
 * Measures the ratio of distinct external providers (classes/objects) to the total number
 * of distinct external method/property calls within a function or constructor.
 * </p>
 *
 * <h3>Metric Definition</h3>
 * <pre>
 * CDISP = |distinct providers| / |distinct external calls|
 * </pre>
 * where:
 * <ul>
 * <li>Value ranges from 0.0 to 1.0</li>
 * <li>Lower values indicate focused coupling (few providers, many calls)</li>
 * <li>Higher values indicate dispersed coupling (many providers, few calls each)</li>
 * <li>Returns 0.0 when there are no external calls</li>
 * </ul>
 *
 * <h3>Counted External Calls</h3>
 * The metric tracks the following types of external interactions:
 * <ul>
 * <li><b>Regular method calls:</b> {@code receiver.method()}</li>
 * <li><b>Safe calls:</b> {@code receiver?.method()}</li>
 * <li><b>Property access:</b> {@code receiver.property} (getters create coupling)</li>
 * <li><b>Infix function calls:</b> {@code receiver infix arg} (e.g., {@code list to 5})</li>
 * <li><b>Array access:</b> {@code receiver[index]} (invokes {@code get/set} operators)</li>
 * <li><b>Unary operators:</b> {@code !receiver}, {@code ++receiver} (operator overloading)</li>
 * <li><b>Binary operators:</b> {@code receiver + other} (operator overloading)</li>
 * <li><b>Invoke operator:</b> {@code receiver()} (function-like objects)</li>
 * </ul>
 *
 * <h3>Exclusions</h3>
 * The following are <b>not</b> counted as external coupling:
 * <ul>
 * <li>Calls on {@code this} or {@code super}</li>
 * <li>Calls within the same class (internal methods)</li>
 * <li>Local variable/parameter access</li>
 * <li>Literals and primitive operations</li>
 * </ul>
 *
 * <h3>Provider Identification</h3>
 * Providers are identified by normalizing the receiver expression:
 * <ul>
 * <li>Simple names: {@code "logger"}</li>
 * <li>Qualified names: {@code "service.helper"}</li>
 * <li>Complex expressions normalized to prevent false duplicates</li>
 * </ul>
 *
 * <h3>Interpretation</h3>
 * <ul>
 * <li><b>CDISP = 1.0:</b> Each provider is called exactly once (maximum dispersion)</li>
 * <li><b>CDISP ≈ 0.0:</b> Few providers are called many times (focused coupling)</li>
 * <li><b>Ideal range:</b> 0.2 - 0.5 indicates balanced coupling</li>
 * </ul>
 *
 * @see org.b333vv.metric.model.metric.MetricType#CDISP
 */
public class KotlinCouplingDispersionVisitor extends KotlinMethodVisitor {

    @Override
    public void visitNamedFunction(@NotNull KtNamedFunction function) {
        metric = Metric.of(CDISP, compute(function.getBodyExpression()));
    }

    @Override
    public void visitPrimaryConstructor(@NotNull KtPrimaryConstructor constructor) {
        // Primary constructors may have init blocks in the containing class
        KtClassOrObject containingClass = constructor.getContainingClassOrObject();
        if (containingClass instanceof KtClass) {
            metric = Metric.of(CDISP, computeForClass((KtClass) containingClass));
        } else {
            metric = Metric.of(CDISP, 0.0);
        }
    }

    @Override
    public void visitSecondaryConstructor(@NotNull KtSecondaryConstructor constructor) {
        metric = Metric.of(CDISP, compute(constructor.getBodyExpression()));
    }

    /**
     * Computes CDISP for a class (used for primary constructors with init blocks).
     */
    private double computeForClass(KtClass ktClass) {
        final Set<String> providers = new HashSet<>();
        final Set<String> usedCalls = new HashSet<>();

        // Visit all init blocks and property initializers
        for (KtAnonymousInitializer init : ktClass.getAnonymousInitializers()) {
            if (init.getBody() != null) {
                collectCalls(init.getBody(), providers, usedCalls);
            }
        }

        // Visit property initializers in primary constructor
        for (KtProperty property : ktClass.getProperties()) {
            if (property.getInitializer() != null) {
                collectCalls(property.getInitializer(), providers, usedCalls);
            }
        }

        return calculateDispersion(providers.size(), usedCalls.size());
    }

    /**
     * Computes CDISP for a function body expression.
     */
    private double compute(KtExpression body) {
        if (body == null) {
            return 0.0;
        }

        final Set<String> providers = new HashSet<>();
        final Set<String> usedCalls = new HashSet<>();
        collectCalls(body, providers, usedCalls);

        return calculateDispersion(providers.size(), usedCalls.size());
    }

    /**
     * Collects all external calls and their providers from the given expression.
     */
    private void collectCalls(KtExpression expression, Set<String> providers, Set<String> usedCalls) {
        expression.accept(new KtTreeVisitorVoid() {

            // Standard qualified calls: receiver.method()
            @Override
            public void visitQualifiedExpression(@NotNull KtQualifiedExpression qualifiedExpression) {
                KtExpression receiver = qualifiedExpression.getReceiverExpression();
                KtExpression selector = qualifiedExpression.getSelectorExpression();

                String receiverKey = normalizeReceiver(receiver);
                if (receiverKey != null && selector != null) {
                    // Method call
                    if (selector instanceof KtCallExpression) {
                        String callee = extractCalleeName((KtCallExpression) selector);
                        if (callee != null) {
                            addCall(receiverKey, callee, providers, usedCalls);
                        }
                    }
                    // Property access (getters)
                    else if (selector instanceof KtNameReferenceExpression) {
                        String propertyName = selector.getText();
                        if (propertyName != null && !propertyName.isEmpty()) {
                            addCall(receiverKey, propertyName, providers, usedCalls);
                        }
                    }
                }
                super.visitQualifiedExpression(qualifiedExpression);
            }

            // Safe calls: receiver?.method()
            @Override
            public void visitSafeQualifiedExpression(@NotNull KtSafeQualifiedExpression expression) {
                KtExpression receiver = expression.getReceiverExpression();
                KtExpression selector = expression.getSelectorExpression();

                String receiverKey = normalizeReceiver(receiver);
                if (receiverKey != null && selector != null) {
                    if (selector instanceof KtCallExpression) {
                        String callee = extractCalleeName((KtCallExpression) selector);
                        if (callee != null) {
                            addCall(receiverKey, callee, providers, usedCalls);
                        }
                    } else if (selector instanceof KtNameReferenceExpression) {
                        String propertyName = selector.getText();
                        if (propertyName != null && !propertyName.isEmpty()) {
                            addCall(receiverKey, propertyName, providers, usedCalls);
                        }
                    }
                }
                super.visitSafeQualifiedExpression(expression);
            }

            // Binary expressions: can be infix calls or operator overloading
            @Override
            public void visitBinaryExpression(@NotNull KtBinaryExpression expression) {
                KtExpression left = expression.getLeft();
                String receiverKey = normalizeReceiver(left);
                
                if (receiverKey != null) {
                    String operatorText = expression.getOperationReference().getText();
                    if (operatorText != null && !isSimpleOperator(operatorText)) {
                        // Treat as method call (e.g., "to" in "a to b")
                        addCall(receiverKey, operatorText, providers, usedCalls);
                    }
                }
                super.visitBinaryExpression(expression);
            }

            // Array access: receiver[index]
            @Override
            public void visitArrayAccessExpression(@NotNull KtArrayAccessExpression expression) {
                KtExpression receiver = expression.getArrayExpression();
                String receiverKey = normalizeReceiver(receiver);
                
                if (receiverKey != null) {
                    addCall(receiverKey, "get", providers, usedCalls);
                }
                super.visitArrayAccessExpression(expression);
            }

            // Unary expressions: operator overloading (e.g., ++, --, !)
            @Override
            public void visitUnaryExpression(@NotNull KtUnaryExpression expression) {
                KtExpression baseExpr = expression.getBaseExpression();
                String receiverKey = normalizeReceiver(baseExpr);
                
                if (receiverKey != null) {
                    String operatorText = expression.getOperationReference().getText();
                    if (operatorText != null) {
                        addCall(receiverKey, operatorText, providers, usedCalls);
                    }
                }
                super.visitUnaryExpression(expression);
            }
        });
    }

    /**
     * Extracts the callee name from a call expression.
     */
    private String extractCalleeName(KtCallExpression callExpression) {
        KtExpression calleeExpr = callExpression.getCalleeExpression();
        return calleeExpr != null ? calleeExpr.getText() : null;
    }

    /**
     * Adds a call to the tracking sets.
     */
    private void addCall(String receiverKey, String callee, Set<String> providers, Set<String> usedCalls) {
        providers.add(receiverKey);
        usedCalls.add(receiverKey + "#" + callee);
    }

    /**
     * Normalizes a receiver expression to a consistent key.
     * Returns null if the receiver should be excluded (this, super, or invalid).
     */
    private String normalizeReceiver(KtExpression receiver) {
        if (receiver == null) {
            return null;
        }

        // Exclude this/super references
        if (receiver instanceof KtThisExpression || receiver instanceof KtSuperExpression) {
            return null;
        }

        String text = receiver.getText();
        if (text == null) {
            return null;
        }

        text = text.trim();
        
        // Exclude explicit this/super and empty text
        if (text.isEmpty() || "this".equals(text) || "super".equals(text)) {
            return null;
        }

        // Normalize parenthesized expressions
        if (receiver instanceof KtParenthesizedExpression) {
            KtExpression innerExpr = ((KtParenthesizedExpression) receiver).getExpression();
            return normalizeReceiver(innerExpr);
        }

        // For qualified expressions, use the full text as-is
        // This handles cases like "service.dao" or "config.database"
        return text;
    }

    /**
     * Checks if an operator is a simple built-in operator that doesn't create external coupling.
     */
    private boolean isSimpleOperator(String operator) {
        // These are typically built-in operators for primitives
        return operator.matches("[+\\-*/=<>!&|]+") || 
               "&&".equals(operator) || 
               "||".equals(operator) || 
               "==".equals(operator) || 
               "!=".equals(operator) ||
               "===".equals(operator) ||
               "!==".equals(operator);
    }

    /**
     * Calculates the dispersion ratio.
     */
    private double calculateDispersion(int providerCount, int callCount) {
        if (callCount == 0) {
            return 0.0;
        }
        return Value.of((double) providerCount).divide(Value.of((double) callCount)).doubleValue();
    }
}
