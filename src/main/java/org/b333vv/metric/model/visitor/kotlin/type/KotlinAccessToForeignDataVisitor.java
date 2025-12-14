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
 * Kotlin Access To Foreign Data (ATFD) - counts distinct external classes whose
 * data
 * (properties/fields via direct access or via accessor calls) is accessed
 * within the class body.
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

                private void collectFromQualified(KtExpression selector, KtExpression receiver) {
                    if (selector == null)
                        return;

                    PsiElement resolved = null;
                    String referenceName = null;

                    // 1. Identify selector type and try to resolve
                    if (selector instanceof KtSimpleNameExpression) {
                        // likely a property access
                        for (com.intellij.psi.PsiReference ref : ((KtSimpleNameExpression) selector).getReferences()) {
                            resolved = ref.resolve();
                            if (resolved != null)
                                break;
                        }
                    } else if (selector instanceof KtCallExpression) {
                        // likely a method call
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
                        // Resolved Case
                        if (resolved instanceof PsiField) {
                            addProvider(((PsiField) resolved).getContainingClass());
                            return;
                        }
                        if (resolved instanceof KtProperty) {
                            addProvider(findOwnerClass((KtProperty) resolved));
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
                            if (isAccessorName(((KtNamedFunction) resolved).getName())) {
                                addProvider(findOwnerClass((KtNamedFunction) resolved));
                            }
                            return; // Resolved to function.
                        }
                    }

                    // Fallback Case: Resolution failed (or we are in a context where resolution is
                    // partial).
                    // Only guess it is data if the shape looks like data.

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

    private boolean isAccessorName(String name) {
        if (name == null)
            return false;
        return name.startsWith("get") || name.startsWith("set") || name.startsWith("is");
    }

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

    private KtClassOrObject findOwnerClass(@NotNull KtProperty property) {
        return findOwnerClass((KtElement) property);
    }
}
