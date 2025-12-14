/*
 * Kotlin Lack Of Cohesion Of Methods (LCOM - components count) - initial implementation
 */
package org.b333vv.metric.model.visitor.kotlin.type;

import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.visitor.kotlin.KotlinMetricUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.psi.*;

import com.intellij.psi.PsiElement;
import java.util.*;

import static org.b333vv.metric.model.metric.MetricType.LCOM;

/**
 * Computes LCOM as the number of connected components among methods where edges
 * exist when
 * two functions access at least one common instance property.
 *
 * Simplifications:
 * - Instance properties are those declared as val/var in primary constructor or
 * as KtProperty in class body
 * outside companion/nested objects.
 * - Access detection is name-based: unqualified references matching property
 * names or references via 'this'.
 * - Companion object members are ignored for cohesion.
 */
public class KotlinLackOfCohesionOfMethodsVisitor extends KotlinClassVisitor {

    @Override
    public void visitClass(@NotNull KtClass klass) {
        Set<PsiElement> instanceProps = collectInstanceProperties(klass);

        // List of sets of accessed properties (each element representing a
        // method/accessor)
        List<Set<PsiElement>> accesses = new ArrayList<>();

        KtClassBody body = klass.getBody();
        if (body != null) {
            for (KtDeclaration decl : body.getDeclarations()) {
                if (decl instanceof KtNamedFunction) {
                    KtNamedFunction f = (KtNamedFunction) decl;
                    accesses.add(collectAccessedFields(f, instanceProps, null));
                } else if (decl instanceof KtProperty) {
                    KtProperty prop = (KtProperty) decl;
                    if (!KotlinMetricUtils.isInCompanionObject(prop) && instanceProps.contains(prop)) {
                        // Getter
                        KtPropertyAccessor getter = prop.getGetter();
                        if (getter != null && getter.hasBody()) {
                            accesses.add(collectAccessedFields(getter, instanceProps, prop));
                        } else {
                            // Implicit getter accesses its backing field (the property itself)
                            Set<PsiElement> getterAccess = new HashSet<>();
                            getterAccess.add(prop);
                            accesses.add(getterAccess);
                        }

                        // Setter
                        if (prop.isVar()) {
                            KtPropertyAccessor setter = prop.getSetter();
                            if (setter != null && setter.hasBody()) {
                                accesses.add(collectAccessedFields(setter, instanceProps, prop));
                            } else {
                                // Implicit setter accesses its backing field
                                Set<PsiElement> setterAccess = new HashSet<>();
                                setterAccess.add(prop);
                                accesses.add(setterAccess);
                            }
                        }
                    }
                }
            }
        }

        // Add implicit accessors for primary constructor properties
        KtPrimaryConstructor primary = klass.getPrimaryConstructor();
        if (primary != null) {
            for (KtParameter param : primary.getValueParameters()) {
                if (instanceProps.contains(param)) {
                    // Getter accesses the property
                    Set<PsiElement> getterAccess = new HashSet<>();
                    getterAccess.add(param);
                    accesses.add(getterAccess);

                    // Setter
                    if (param.isMutable()) {
                        Set<PsiElement> setterAccess = new HashSet<>();
                        setterAccess.add(param);
                        accesses.add(setterAccess);
                    }
                }
            }
        }

        int components = connectedComponents(accesses);
        metric = Metric.of(LCOM, components);
    }

    private Set<PsiElement> collectInstanceProperties(KtClass klass) {
        Set<PsiElement> props = new HashSet<>();
        KtPrimaryConstructor primary = klass.getPrimaryConstructor();
        if (primary != null) {
            for (KtParameter p : primary.getValueParameters()) {
                if (p.hasValOrVar()) {
                    props.add(p);
                }
            }
        }
        KtClassBody body = klass.getBody();
        if (body != null) {
            for (KtDeclaration decl : body.getDeclarations()) {
                if (decl instanceof KtProperty) {
                    if (!KotlinMetricUtils.isInCompanionObject((KtProperty) decl)) {
                        props.add(decl);
                    }
                }
            }
        }
        return props;
    }

    private Set<PsiElement> collectAccessedFields(KtDeclarationWithBody method, Set<PsiElement> instanceProps,
            KtProperty contextProperty) {
        Set<PsiElement> used = new HashSet<>();
        KtExpression body = method.getBodyExpression();
        if (body == null)
            return used;

        body.accept(new KtTreeVisitorVoid() {
            @Override
            public void visitSimpleNameExpression(@NotNull KtSimpleNameExpression expression) {
                // Check for 'field' keyword in custom accessors
                if (contextProperty != null && "field".equals(expression.getReferencedName())) {
                    used.add(contextProperty);
                } else {
                    PsiElement target = expression.getReference() != null ? expression.getReference().resolve() : null;
                    if (target != null && instanceProps.contains(target)) {
                        used.add(target);
                    }
                }
                super.visitSimpleNameExpression(expression);
            }
        });
        return used;
    }

    private int connectedComponents(List<Set<PsiElement>> accesses) {
        int n = accesses.size();
        if (n == 0)
            return 0;
        boolean[] visited = new boolean[n];
        int comps = 0;
        for (int i = 0; i < n; i++) {
            if (!visited[i]) {
                comps++;
                Deque<Integer> dq = new ArrayDeque<>();
                dq.add(i);
                visited[i] = true;
                while (!dq.isEmpty()) {
                    int u = dq.poll();
                    for (int v = 0; v < n; v++) {
                        if (!visited[v] && shares(accesses.get(u), accesses.get(v))) {
                            visited[v] = true;
                            dq.add(v);
                        }
                    }
                }
            }
        }
        return comps;
    }

    private boolean shares(Set<PsiElement> a, Set<PsiElement> b) {
        if (a.isEmpty() || b.isEmpty())
            return false;
        for (PsiElement x : a)
            if (b.contains(x))
                return true;
        return false;
    }
}
