/*
 * Kotlin Lack Of Cohesion Of Methods (LCOM - components count) - initial implementation
 */
package org.b333vv.metric.model.visitor.kotlin.type;

import org.b333vv.metric.model.metric.Metric;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.psi.*;

import java.util.*;

import static org.b333vv.metric.model.metric.MetricType.LCOM;

/**
 * Computes LCOM as the number of connected components among methods where edges exist when
 * two functions access at least one common instance property.
 *
 * Simplifications:
 * - Instance properties are those declared as val/var in primary constructor or as KtProperty in class body
 *   outside companion/nested objects.
 * - Access detection is name-based: unqualified references matching property names or references via 'this'.
 * - Companion object members are ignored for cohesion.
 */
public class KotlinLackOfCohesionOfMethodsVisitor extends KotlinClassVisitor {

    @Override
    public void visitClass(@NotNull KtClass klass) {
        Set<String> instanceProps = collectInstancePropertyNames(klass);

        // Map function -> accessed property names
        List<KtNamedFunction> methods = new ArrayList<>();
        List<Set<String>> accesses = new ArrayList<>();

        KtClassBody body = klass.getBody();
        if (body != null) {
            for (KtDeclaration decl : body.getDeclarations()) {
                if (decl instanceof KtNamedFunction) {
                    KtNamedFunction f = (KtNamedFunction) decl;
                    methods.add(f);
                    accesses.add(collectAccessedProps(f, instanceProps));
                }
                // skip nested objects/companions for this cohesion definition
            }
        }

        int components = connectedComponentsBySharedProps(accesses);
        metric = Metric.of(LCOM, components);
    }

    private Set<String> collectInstancePropertyNames(KtClass klass) {
        Set<String> names = new HashSet<>();
        KtPrimaryConstructor primary = klass.getPrimaryConstructor();
        if (primary != null) {
            for (KtParameter p : primary.getValueParameters()) {
                if (p.hasValOrVar()) {
                    String n = p.getName();
                    if (n != null) names.add(n);
                }
            }
        }
        KtClassBody body = klass.getBody();
        if (body != null) {
            for (KtDeclaration decl : body.getDeclarations()) {
                if (decl instanceof KtProperty) {
                    String n = ((KtProperty) decl).getName();
                    if (n != null) names.add(n);
                }
            }
        }
        return names;
    }

    private Set<String> collectAccessedProps(KtNamedFunction f, Set<String> instanceProps) {
        Set<String> used = new HashSet<>();
        KtExpression body = f.getBodyExpression();
        if (body == null) return used;
        body.accept(new KtTreeVisitorVoid() {
            @Override
            public void visitSimpleNameExpression(@NotNull KtSimpleNameExpression expression) {
                String ref = expression.getReferencedName();
                if (instanceProps.contains(ref)) {
                    used.add(ref);
                }
                super.visitSimpleNameExpression(expression);
            }

            @Override
            public void visitDotQualifiedExpression(@NotNull KtDotQualifiedExpression expression) {
                // match this.prop style
                KtExpression receiver = expression.getReceiverExpression();
                if (receiver instanceof KtThisExpression) {
                    KtExpression selector = expression.getSelectorExpression();
                    if (selector instanceof KtNameReferenceExpression) {
                        String name = ((KtNameReferenceExpression) selector).getReferencedName();
                        if (instanceProps.contains(name)) {
                            used.add(name);
                        }
                    }
                }
                super.visitDotQualifiedExpression(expression);
            }
        });
        return used;
    }

    private int connectedComponentsBySharedProps(List<Set<String>> accesses) {
        int n = accesses.size();
        if (n == 0) return 0;
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

    private boolean shares(Set<String> a, Set<String> b) {
        if (a.isEmpty() || b.isEmpty()) return false;
        for (String x : a) if (b.contains(x)) return true;
        return false;
    }
}
