/*
 * Kotlin Tight Class Cohesion (TCC) - PSI-based implementation
 */
package org.b333vv.metric.model.visitor.kotlin.type;

import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.value.Value;
import org.b333vv.metric.model.visitor.kotlin.KotlinMetricUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.lexer.KtTokens;
import org.jetbrains.kotlin.psi.*;
import com.intellij.psi.PsiElement;

import java.util.*;

import static org.b333vv.metric.model.metric.MetricType.TCC;

/**
 * Calculates Tight Class Cohesion (TCC) metric for Kotlin classes.
 * <p>
 * TCC is the ratio of directly connected method pairs to all possible method pairs.
 * Two methods are considered directly connected if they share access to at least one
 * common instance property.
 * </p>
 *
 * <h3>Formula</h3>
 * <pre>
 * TCC = NP / (N * (N - 1) / 2)
 * where:
 *   NP = number of directly connected method pairs
 *   N  = total number of methods
 * </pre>
 *
 * <h3>Counted as Methods</h3>
 * <ul>
 *   <li>Non-abstract named functions declared in class body</li>
 *   <li>Custom property getters with explicit body</li>
 *   <li>Custom property setters with explicit body</li>
 * </ul>
 *
 * <h3>Excluded from Method Count</h3>
 * <ul>
 *   <li>Abstract functions</li>
 *   <li>Functions in companion objects</li>
 *   <li>Functions in nested/inner classes</li>
 *   <li>Implicit (generated) property accessors</li>
 * </ul>
 *
 * <h3>Counted as Instance Properties</h3>
 * <ul>
 *   <li>Primary constructor parameters with val/var</li>
 *   <li>Properties declared in class body (excluding companion object)</li>
 * </ul>
 *
 * <h3>Property Access Detection</h3>
 * <ul>
 *   <li>Direct property references (e.g., {@code name})</li>
 *   <li>Explicit this references (e.g., {@code this.name})</li>
 *   <li>Backing field keyword in custom accessors (e.g., {@code field})</li>
 * </ul>
 *
 * @see org.b333vv.metric.model.visitor.kotlin.type.KotlinLackOfCohesionOfMethodsVisitor
 */
public class KotlinTightClassCohesionVisitor extends KotlinClassVisitor {

    @Override
    public void visitClass(@NotNull KtClass klass) {
        Set<PsiElement> instanceProps = collectInstanceProperties(klass);
        List<KtDeclarationWithBody> methods = collectMethods(klass);

        int n = methods.size();
        int possiblePairs = n * (n - 1) / 2;

        if (possiblePairs == 0) {
            metric = Metric.of(TCC, Value.of(0.0));
            return;
        }

        // For each method, collect accessed instance properties
        List<Set<PsiElement>> accessed = new ArrayList<>(n);
        for (KtDeclarationWithBody method : methods) {
            KtProperty contextProperty = (method instanceof KtPropertyAccessor)
                    ? ((KtPropertyAccessor) method).getProperty()
                    : null;
            accessed.add(collectAccessedProps(method, instanceProps, contextProperty));
        }

        int connected = 0;
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (shares(accessed.get(i), accessed.get(j))) {
                    connected++;
                }
            }
        }

        metric = Metric.of(TCC, Value.of((double) connected).divide(Value.of((double) possiblePairs)));
    }

    /**
     * Collects all instance properties of the class.
     * <p>
     * Instance properties include:
     * </p>
     * <ul>
     *   <li>Primary constructor parameters declared with val/var keywords</li>
     *   <li>Properties declared in class body (excluding companion object)</li>
     * </ul>
     *
     * <p>
     * The method uses PsiElement instances for accurate element identification
     * and resolution, avoiding false matches with local variables or parameters.
     * </p>
     *
     * @param klass the Kotlin class to analyze
     * @return set of PsiElement instances representing instance properties
     */
    private Set<PsiElement> collectInstanceProperties(KtClass klass) {
        Set<PsiElement> props = new HashSet<>();

        // Primary constructor properties
        KtPrimaryConstructor primary = klass.getPrimaryConstructor();
        if (primary != null) {
            for (KtParameter p : primary.getValueParameters()) {
                if (p.hasValOrVar()) {
                    props.add(p);
                }
            }
        }

        // Class body properties (excluding companion)
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

    /**
     * Collects all methods that should be counted for TCC calculation.
     * <p>
     * Counted methods include:
     * </p>
     * <ul>
     *   <li><b>Non-abstract named functions</b> - Regular functions with implementation</li>
     *   <li><b>Custom property getters</b> - Getters with explicit body
     *       (e.g., {@code get() = field.toString()})</li>
     *   <li><b>Custom property setters</b> - Setters with explicit body
     *       (e.g., {@code set(value) { field = value.trim() }})</li>
     * </ul>
     *
     * <p>
     * Excluded from count:
     * </p>
     * <ul>
     *   <li>Abstract functions (no implementation)</li>
     *   <li>Functions in companion objects (static-like behavior)</li>
     *   <li>Functions in nested or inner classes (separate cohesion scope)</li>
     *   <li>Implicit property accessors (generated by compiler)</li>
     * </ul>
     *
     * @param klass the Kotlin class to analyze
     * @return list of method declarations (functions and custom accessors)
     */
    private List<KtDeclarationWithBody> collectMethods(KtClass klass) {
        List<KtDeclarationWithBody> methods = new ArrayList<>();
        KtClassBody body = klass.getBody();
        if (body == null) return methods;

        for (KtDeclaration decl : body.getDeclarations()) {
            // Named functions
            if (decl instanceof KtNamedFunction) {
                KtNamedFunction f = (KtNamedFunction) decl;
                if (!f.hasModifier(KtTokens.ABSTRACT_KEYWORD)) {
                    methods.add(f);
                }
            }
            // Property accessors
            else if (decl instanceof KtProperty) {
                KtProperty prop = (KtProperty) decl;
                if (!KotlinMetricUtils.isInCompanionObject(prop)) {
                    // Custom getter
                    KtPropertyAccessor getter = prop.getGetter();
                    if (getter != null && getter.hasBody()) {
                        methods.add(getter);
                    }
                    // Custom setter
                    if (prop.isVar()) {
                        KtPropertyAccessor setter = prop.getSetter();
                        if (setter != null && setter.hasBody()) {
                            methods.add(setter);
                        }
                    }
                }
            }
        }
        return methods;
    }

    /**
     * Collects instance properties accessed by a given method.
     * <p>
     * Detection methods:
     * </p>
     * <ul>
     *   <li><b>Direct references</b> - Simple name expressions that resolve to instance properties
     *       (e.g., {@code println(name)})</li>
     *   <li><b>Explicit this</b> - Qualified expressions with this receiver
     *       (e.g., {@code this.name})</li>
     *   <li><b>Backing field</b> - The {@code field} keyword in custom accessors, which refers
     *       to the property's backing field</li>
     * </ul>
     *
     * <p>
     * The method uses PSI reference resolution to accurately identify property access,
     * avoiding false positives from local variables or parameters with the same name.
     * </p>
     *
     * @param method the method (function or accessor) to analyze
     * @param instanceProps set of all instance properties in the class
     * @param contextProperty the property being accessed (for accessors), or null for regular functions
     * @return set of PsiElement instances representing accessed properties
     */
    private Set<PsiElement> collectAccessedProps(KtDeclarationWithBody method,
                                                 Set<PsiElement> instanceProps,
                                                 KtProperty contextProperty) {
        Set<PsiElement> used = new HashSet<>();
        KtExpression body = method.getBodyExpression();
        if (body == null) return used;

        body.accept(new KtTreeVisitorVoid() {
            @Override
            public void visitSimpleNameExpression(@NotNull KtSimpleNameExpression expression) {
                // Handle 'field' keyword in custom accessors
                if (contextProperty != null && "field".equals(expression.getReferencedName())) {
                    used.add(contextProperty);
                } else {
                    // Use reference resolution for accurate targeting
                    PsiElement target = expression.getReference() != null
                            ? expression.getReference().resolve()
                            : null;
                    if (target != null && instanceProps.contains(target)) {
                        used.add(target);
                    }
                }
                super.visitSimpleNameExpression(expression);
            }

            @Override
            public void visitDotQualifiedExpression(@NotNull KtDotQualifiedExpression expression) {
                // Handle this.property pattern
                KtExpression receiver = expression.getReceiverExpression();
                if (receiver instanceof KtThisExpression) {
                    KtExpression selector = expression.getSelectorExpression();
                    if (selector instanceof KtNameReferenceExpression) {
                        PsiElement target = ((KtNameReferenceExpression) selector).getReference() != null
                                ? ((KtNameReferenceExpression) selector).getReference().resolve()
                                : null;
                        if (target != null && instanceProps.contains(target)) {
                            used.add(target);
                        }
                    }
                }
                super.visitDotQualifiedExpression(expression);
            }
        });
        return used;
    }

    /**
     * Checks if two sets of accessed properties have at least one common element.
     * <p>
     * Two methods are considered directly connected if they both access at least
     * one common instance property. Empty sets never share properties.
     * </p>
     *
     * @param a first set of accessed properties
     * @param b second set of accessed properties
     * @return true if sets have at least one common element, false otherwise
     */
    private boolean shares(Set<PsiElement> a, Set<PsiElement> b) {
        if (a.isEmpty() || b.isEmpty()) return false;
        for (PsiElement x : a) {
            if (b.contains(x)) return true;
        }
        return false;
    }
}
