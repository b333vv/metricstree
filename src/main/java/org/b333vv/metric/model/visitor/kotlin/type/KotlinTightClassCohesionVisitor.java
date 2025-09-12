/*
 * Kotlin Tight Class Cohesion (TCC) - PSI-based implementation
 */
package org.b333vv.metric.model.visitor.kotlin.type;

import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.lexer.KtTokens;
import org.jetbrains.kotlin.psi.*;

import java.util.*;

import static org.b333vv.metric.model.metric.MetricType.TCC;

/**
 * TCC is the ratio of directly connected method pairs to all possible method pairs.
 * Two methods are considered directly connected if they share access to at least one common instance property.
 */
public class KotlinTightClassCohesionVisitor extends KotlinClassVisitor {

    @Override
    public void visitClass(@NotNull KtClass klass) {
        // Collect applicable methods: non-abstract KtNamedFunction declared in class body (exclude companions/nested)
        List<KtNamedFunction> methods = new ArrayList<>();
        Set<String> instanceProps = collectInstancePropertyNames(klass);

        KtClassBody body = klass.getBody();
        if (body != null) {
            for (KtDeclaration decl : body.getDeclarations()) {
                if (decl instanceof KtNamedFunction) {
                    KtNamedFunction f = (KtNamedFunction) decl;
                    if (!f.hasModifier(KtTokens.ABSTRACT_KEYWORD)) {
                        methods.add(f);
                    }
                }
            }
        }

        int n = methods.size();
        int possiblePairs = n * (n - 1) / 2;
        if (possiblePairs == 0) {
            metric = Metric.of(TCC, Value.of(0.0));
            return;
        }

        // For each method, collect accessed instance properties
        List<Set<String>> accessed = new ArrayList<>(n);
        for (KtNamedFunction f : methods) {
            accessed.add(collectAccessedProps(f, instanceProps));
        }

        int connected = 0;
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (shares(accessed.get(i), accessed.get(j))) {
                    connected++;
                }
            }
        }

        if (possiblePairs == 0) {
            metric = Metric.of(TCC, Value.of(0.0));
        } else {
            metric = Metric.of(TCC, Value.of((double) connected).divide(Value.of((double) possiblePairs)));
        }
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

    private boolean shares(Set<String> a, Set<String> b) {
        if (a.isEmpty() || b.isEmpty()) return false;
        for (String x : a) if (b.contains(x)) return true;
        return false;
    }
}
