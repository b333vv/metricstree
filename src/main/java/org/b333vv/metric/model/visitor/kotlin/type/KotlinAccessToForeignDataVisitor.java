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
 * Kotlin Access To Foreign Data (ATFD) - counts distinct external classes whose data
 * (properties/fields via direct access or via accessor calls) is accessed within the class body.
 *
 * Heuristics and limitations:
 * - We attempt to resolve property references to discover the declaring class; if unavailable,
 *   we fallback to textual receiver keys and will de-duplicate by receiver text.
 * - Implicit/explicit this/super are excluded.
 * - Counts unique providers across all functions and initializers of the class.
 */
public class KotlinAccessToForeignDataVisitor extends KotlinClassVisitor {

    @Override
    public void visitClass(@NotNull KtClass klass) {
        final Set<String> providers = new HashSet<>();

        // Walk through the entire class body collecting provider identifiers
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
                public void visitCallExpression(@NotNull KtCallExpression expression) {
                    // Java-style accessors: getX()/setX()/isX()
                    KtExpression callee = expression.getCalleeExpression();
                    if (callee instanceof KtSimpleNameExpression) {
                        String name = ((KtSimpleNameExpression) callee).getReferencedName();
                        if (isAccessorName(name)) {
                            // If there is a qualified receiver like a.getX(), it will be handled by qualified visitor
                            // For unqualified, assume local -> ignore
                        }
                    }
                    super.visitCallExpression(expression);
                }

                private void collectFromQualified(KtExpression selector, KtExpression receiver) {
                    if (selector instanceof KtSimpleNameExpression) {
                        // Try resolve to field/property to get declaring class/object FQN
                        for (var ref : selector.getReferences()) {
                            PsiElement resolved = ref.resolve();
                            if (resolved instanceof PsiField) {
                                PsiField f = (PsiField) resolved;
                                if (f.getContainingClass() != null && f.getContainingClass().getQualifiedName() != null) {
                                    providers.add(f.getContainingClass().getQualifiedName());
                                    return;
                                }
                            }
                            if (resolved instanceof KtProperty) {
                                KtClassOrObject owner = findOwnerClass((KtProperty) resolved);
                                if (owner != null && owner.getFqName() != null) {
                                    providers.add(owner.getFqName().asString());
                                    return;
                                }
                            }
                        }
                    }
                    if (receiver == null) return;
                    if (receiver instanceof KtThisExpression || receiver instanceof KtSuperExpression) return;
                    String text = receiver.getText();
                    if (text == null) return;
                    text = text.trim();
                    if (text.isEmpty() || "this".equals(text) || "super".equals(text)) return;
                    providers.add("#RX#:" + text); // textual receiver key fallback
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
        if (name == null) return false;
        if (name.startsWith("get") || name.startsWith("set")) return true;
        if (name.startsWith("is")) return true;
        return false;
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
