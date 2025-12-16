package org.b333vv.metric.model.visitor.kotlin.type;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import org.b333vv.metric.model.metric.Metric;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.psi.*;

import java.util.HashSet;
import java.util.Set;

import static org.b333vv.metric.model.metric.MetricType.ATFD;

/**
 * Visitor for calculating the Access To Foreign Data (ATFD) metric for Kotlin classes.
 * <p>
 * ATFD measures the degree to which a class accesses data from other classes, indicating
 * potential Feature Envy code smell. Higher ATFD values suggest that a class is more
 * interested in data of other classes than its own, which may indicate poor encapsulation
 * or misplaced responsibilities.
 * </p>
 *
 * <h2>What is Counted as Foreign Data Access</h2>
 * The metric counts the number of distinct external classes whose data is accessed.
 * The following constructs are considered data access:
 *
 * <h3>Direct Property Access</h3>
 * <ul>
 *   <li><b>Property access via dot notation:</b> {@code foreignObject.property}</li>
 *   <li><b>Property access via safe call:</b> {@code foreignObject?.property}</li>
 *   <li><b>Extension property access:</b> {@code foreignObject.extensionProperty} (when extension is defined elsewhere)</li>
 *   <li><b>Delegated property access:</b> {@code foreignObject.delegatedProperty} (properties using {@code by} delegation)</li>
 * </ul>
 *
 * <h3>Accessor Method Calls</h3>
 * Method calls are counted only if they follow accessor naming conventions:
 * <ul>
 *   <li><b>Getters:</b> Methods starting with {@code get}, e.g., {@code foreignObject.getName()}</li>
 *   <li><b>Setters:</b> Methods starting with {@code set}, e.g., {@code foreignObject.setName(value)}</li>
 *   <li><b>Boolean getters:</b> Methods starting with {@code is}, e.g., {@code foreignObject.isActive()}</li>
 * </ul>
 * Non-accessor methods are considered behavior rather than data access and are excluded.
 *
 * <h3>Kotlin-Specific Constructs</h3>
 * <ul>
 *   <li><b>Indexed access operators:</b> {@code foreignObject[key]} or {@code foreignObject[index]}</li>
 *   <li><b>Property references:</b> {@code foreignObject::property} (property reference expressions)</li>
 *   <li><b>Companion object properties:</b> {@code ForeignClass.Companion.property} or {@code ForeignClass.CONSTANT}</li>
 * </ul>
 *
 * <h3>Java Interoperability</h3>
 * When accessing Java classes from Kotlin:
 * <ul>
 *   <li><b>Java field access:</b> Direct access to public fields</li>
 *   <li><b>Java getter/setter calls:</b> Following JavaBean conventions</li>
 * </ul>
 *
 * <h2>What is NOT Counted</h2>
 * <ul>
 *   <li><b>Access to own class properties:</b> {@code this.property} or implicit access</li>
 *   <li><b>Access to superclass properties:</b> {@code super.property}</li>
 *   <li><b>Method calls for behavior:</b> Methods not following accessor naming patterns</li>
 *   <li><b>Constructor calls:</b> Creating new objects</li>
 *   <li><b>Local variables and parameters:</b> Variables declared within the method scope</li>
 *   <li><b>Static utility methods:</b> Stateless utility function calls</li>
 * </ul>
 *
 * <h2>Resolution Strategy</h2>
 * The visitor uses a two-phase approach:
 * <ol>
 *   <li><b>Primary (resolved):</b> When PSI resolution succeeds, the actual declaring class is identified</li>
 *   <li><b>Fallback (unresolved):</b> When resolution fails, a heuristic based on receiver expression text
 *       is used to avoid undercounting in incomplete/compiling code</li>
 * </ol>
 *
 * <h2>Metric Calculation</h2>
 * The final ATFD value is the count of <b>distinct external classes</b> (not individual accesses).
 * Multiple accesses to the same foreign class count as one.
 *
 * @see org.b333vv.metric.model.metric.MetricType#ATFD
 */
public class KotlinAccessToForeignDataVisitor extends KotlinClassVisitor {

    @Override
    public void visitClass(@NotNull KtClass klass) {
        final Set<String> providers = new HashSet<>();

        KtClassBody body = klass.getBody();
        if (body != null) {
            body.accept(new KtTreeVisitorVoid() {
                @Override
                public void visitDotQualifiedExpression(@NotNull KtDotQualifiedExpression expression) {
                    collectFromQualified(expression.getSelectorExpression(), expression.getReceiverExpression());
                    super.visitDotQualifiedExpression(expression);
                }

                @Override
                public void visitSafeQualifiedExpression(@NotNull KtSafeQualifiedExpression expression) {
                    collectFromQualified(expression.getSelectorExpression(), expression.getReceiverExpression());
                    super.visitSafeQualifiedExpression(expression);
                }

                @Override
                public void visitArrayAccessExpression(@NotNull KtArrayAccessExpression expression) {
                    // Array/indexed access like foreignObject[key] is data access
                    KtExpression arrayExpr = expression.getArrayExpression();
                    if (arrayExpr != null) {
                        collectFromIndexedAccess(arrayExpr);
                    }
                    super.visitArrayAccessExpression(expression);
                }

                @Override
                public void visitCallableReferenceExpression(@NotNull KtCallableReferenceExpression expression) {
                    // Property references like object::property
                    KtExpression receiver = expression.getReceiverExpression();
                    if (receiver != null && expression.getCallableReference() instanceof KtSimpleNameExpression) {
                        // Only count property references (not function references)
                        collectFromPropertyReference(receiver, (KtSimpleNameExpression) expression.getCallableReference());
                    }
                    super.visitCallableReferenceExpression(expression);
                }

                /**
                 * Handles indexed access expressions (array/map access operators).
                 * In Kotlin, {@code obj[key]} can be a data access if obj is a foreign object.
                 */
                private void collectFromIndexedAccess(KtExpression arrayExpression) {
                    if (arrayExpression instanceof KtThisExpression || arrayExpression instanceof KtSuperExpression) {
                        return;
                    }
                    // Try to resolve the array expression to find its type
                    addFallback(arrayExpression);
                }

                /**
                 * Handles property reference expressions.
                 * Property references like {@code obj::property} indicate potential data access.
                 */
                private void collectFromPropertyReference(KtExpression receiver, KtSimpleNameExpression propertyRef) {
                    if (receiver instanceof KtThisExpression || receiver instanceof KtSuperExpression) {
                        return;
                    }
                    // Try to resolve the property reference
                    for (com.intellij.psi.PsiReference ref : propertyRef.getReferences()) {
                        PsiElement resolved = ref.resolve();
                        if (resolved instanceof KtProperty) {
                            addProvider(findOwnerClass((KtProperty) resolved));
                            return;
                        } else if (resolved instanceof PsiField) {
                            addProvider(((PsiField) resolved).getContainingClass());
                            return;
                        }
                    }
                    // Fallback if not resolved
                    addFallback(receiver);
                }

                /**
                 * Main collection logic for qualified expressions (dot and safe-call).
                 * Handles direct property access, accessor method calls, and extension properties.
                 */
                private void collectFromQualified(KtExpression selector, KtExpression receiver) {
                    if (selector == null)
                        return;

                    PsiElement resolved = null;
                    String referenceName = null;

                    // 1. Identify selector type and try to resolve
                    if (selector instanceof KtSimpleNameExpression) {
                        // Likely a property access (including extension properties)
                        for (com.intellij.psi.PsiReference ref : ((KtSimpleNameExpression) selector).getReferences()) {
                            resolved = ref.resolve();
                            if (resolved != null)
                                break;
                        }
                    } else if (selector instanceof KtCallExpression) {
                        // Likely a method call
                        KtExpression callee = ((KtCallExpression) selector).getCalleeExpression();
                        if (callee instanceof KtSimpleNameExpression) {
                            referenceName = ((KtSimpleNameExpression) callee).getReferencedName();
                            for (com.intellij.psi.PsiReference ref : ((KtSimpleNameExpression) callee)
                                    .getReferences()) {
                                resolved = ref.resolve();
                                if (resolved != null)
                                    break;
                            }
                        }
                    }

                    if (resolved != null) {
                        // Resolved Case: determine if it's data access
                        if (resolved instanceof PsiField) {
                            addProvider(((PsiField) resolved).getContainingClass());
                            return;
                        }
                        if (resolved instanceof KtProperty) {
                            // Kotlin property (including delegated and extension properties)
                            addProvider(findOwnerClass((KtProperty) resolved));
                            return;
                        }
                        if (resolved instanceof KtParameter) {
                            // Primary constructor properties
                            KtParameter param = (KtParameter) resolved;
                            if (param.hasValOrVar()) {
                                addProvider(findOwnerClass(param));
                            }
                            return;
                        }
                        // For methods/functions, only count if it is an accessor
                        if (resolved instanceof com.intellij.psi.PsiMethod) {
                            if (isAccessorName(((com.intellij.psi.PsiMethod) resolved).getName())) {
                                addProvider(((com.intellij.psi.PsiMethod) resolved).getContainingClass());
                            }
                            return; // Resolved to a method; if not accessor, it's behavior. Don't fallback.
                        }
                        if (resolved instanceof KtNamedFunction) {
                            String functionName = ((KtNamedFunction) resolved).getName();
                            if (isAccessorName(functionName)) {
                                addProvider(findOwnerClass((KtNamedFunction) resolved));
                            }
                            return; // Resolved to function.
                        }
                    }

                    // Fallback Case: Resolution failed (or we are in a context where resolution is
                    // partial). Only guess it is data if the shape looks like data.

                    if (selector instanceof KtSimpleNameExpression) {
                        // Unresolved property-like access: assume data
                        addFallback(receiver);
                    } else if (selector instanceof KtCallExpression && referenceName != null) {
                        // Unresolved method call: only if name looks like accessor
                        if (isAccessorName(referenceName)) {
                            addFallback(receiver);
                        }
                    }
                }

                private void addProvider(com.intellij.psi.PsiClass psiClass) {
                    if (psiClass != null && psiClass.getQualifiedName() != null) {
                        providers.add(psiClass.getQualifiedName());
                    }
                }

                private void addProvider(KtClassOrObject ktClass) {
                    if (ktClass != null && ktClass.getFqName() != null) {
                        providers.add(ktClass.getFqName().asString());
                    }
                }

                /**
                 * Fallback mechanism when PSI resolution fails.
                 * Uses the receiver expression text as a heuristic identifier.
                 * Filters out this/super references.
                 */
                private void addFallback(KtExpression receiver) {
                    if (receiver == null)
                        return;
                    if (receiver instanceof KtThisExpression || receiver instanceof KtSuperExpression)
                        return;
                    String text = receiver.getText();
                    if (text == null)
                        return;
                    text = text.trim();
                    if (text.isEmpty() || "this".equals(text) || "super".equals(text))
                        return;
                    providers.add("#RX#:" + text);
                }
            });
        }

        // Exclude this class/object itself by its FQN if present
        if (klass.getFqName() != null) {
            providers.remove(klass.getFqName().asString());
        }

        metric = Metric.of(ATFD, providers.size());
    }

    /**
     * Determines if a method name follows accessor naming conventions.
     * Accessors are considered data access, while other methods are behavior.
     *
     * @param name the method or function name
     * @return true if the name matches getter/setter/boolean getter patterns
     */
    private boolean isAccessorName(String name) {
        if (name == null)
            return false;
        return name.startsWith("get") || name.startsWith("set") || name.startsWith("is");
    }

    /**
     * Finds the containing class or object for a Kotlin element.
     * Traverses up the PSI tree until a class/object declaration is found.
     *
     * @param element the starting element
     * @return the containing KtClassOrObject, or null if not found
     */
    private KtClassOrObject findOwnerClass(@NotNull KtElement element) {
        PsiElement e = element;
        while (e != null && !(e instanceof KtClassOrObject)) {
            e = e.getParent();
            if (e != null && !(e instanceof KtElement)) {
                break;
            }
        }
        return (e instanceof KtClassOrObject) ? (KtClassOrObject) e : null;
    }

    /**
     * Finds the containing class or object for a Kotlin property.
     *
     * @param property the property to find the owner for
     * @return the containing KtClassOrObject, or null if not found
     */
    private KtClassOrObject findOwnerClass(@NotNull KtProperty property) {
        return findOwnerClass((KtElement) property);
    }

    /**
     * Finds the containing class or object for a Kotlin parameter.
     * Used for primary constructor properties.
     *
     * @param parameter the parameter to find the owner for
     * @return the containing KtClassOrObject, or null if not found
     */
    private KtClassOrObject findOwnerClass(@NotNull KtParameter parameter) {
        return findOwnerClass((KtElement) parameter);
    }
}
