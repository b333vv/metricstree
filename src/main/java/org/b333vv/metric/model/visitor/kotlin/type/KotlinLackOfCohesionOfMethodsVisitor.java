/*
 * Copyright 2020 b333vv
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
 * Visitor that computes the Lack of Cohesion of Methods (LCOM) metric for Kotlin classes.
 * <p>
 * LCOM is calculated as the number of connected components in a graph where:
 * <ul>
 *   <li>Each node represents a method, accessor, constructor, or init block</li>
 *   <li>An edge exists between two nodes if they access at least one common instance property</li>
 * </ul>
 * <p>
 * The metric considers the following elements and their property accesses:
 * <ul>
 *   <li><b>Named functions</b> - All functions declared in the class body</li>
 *   <li><b>Property accessors</b> - Both custom and implicit getters/setters for instance properties</li>
 *   <li><b>Primary constructor properties</b> - Properties declared with val/var in primary constructor</li>
 *   <li><b>Secondary constructors</b> - Additional constructors and their property accesses</li>
 *   <li><b>Init blocks</b> - Initialization code that may access properties</li>
 *   <li><b>Property initializers</b> - Default values that reference other instance properties</li>
 *   <li><b>Delegated properties</b> - Properties using delegation with by keyword</li>
 *   <li><b>Backing field references</b> - Uses of the 'field' keyword in custom accessors</li>
 *   <li><b>Nested expressions</b> - Lambda expressions and other nested scopes that capture properties</li>
 * </ul>
 * <p>
 * The following are <b>excluded</b> from the calculation:
 * <ul>
 *   <li>Companion object members and their properties</li>
 *   <li>Methods or accessors that don't access any instance properties</li>
 *   <li>Static/companion functions</li>
 * </ul>
 * <p>
 * A higher LCOM value indicates lower cohesion, suggesting the class may have multiple
 * responsibilities and could benefit from decomposition. A value of 1 indicates perfect
 * cohesion (all methods are connected through shared property access).
 *
 * @author b333vv
 * @since 1.0
 */
public class KotlinLackOfCohesionOfMethodsVisitor extends KotlinClassVisitor {

    @Override
    public void visitClass(@NotNull KtClass klass) {
        Set<PsiElement> instanceProps = collectInstanceProperties(klass);

        // List of sets of accessed properties (each element representing a method/accessor/block)
        List<Set<PsiElement>> accesses = new ArrayList<>();

        KtClassBody body = klass.getBody();
        if (body != null) {
            // Process regular functions
            for (KtDeclaration decl : body.getDeclarations()) {
                if (decl instanceof KtNamedFunction) {
                    KtNamedFunction f = (KtNamedFunction) decl;
                    Set<PsiElement> accessed = collectAccessedFields(f, instanceProps, null);
                    if (!accessed.isEmpty()) {
                        accesses.add(accessed);
                    }
                } else if (decl instanceof KtProperty) {
                    KtProperty prop = (KtProperty) decl;
                    if (!KotlinMetricUtils.isInCompanionObject(prop) && instanceProps.contains(prop)) {
                        // Process property initializer
                        KtExpression initializer = prop.getInitializer();
                        if (initializer != null) {
                            Set<PsiElement> initAccessed = collectAccessedFieldsFromExpression(
                                    initializer, instanceProps, prop);
                            if (!initAccessed.isEmpty()) {
                                accesses.add(initAccessed);
                            }
                        }

                        // Process delegated property
                        KtPropertyDelegate delegate = prop.getDelegate();
                        if (delegate != null) {
                            Set<PsiElement> delegateAccessed = collectAccessedFieldsFromExpression(
                                    delegate.getExpression(), instanceProps, prop);
                            if (!delegateAccessed.isEmpty()) {
                                accesses.add(delegateAccessed);
                            }
                        }

                        // Getter
                        KtPropertyAccessor getter = prop.getGetter();
                        if (getter != null && getter.hasBody()) {
                            Set<PsiElement> getterAccessed = collectAccessedFields(getter, instanceProps, prop);
                            if (!getterAccessed.isEmpty()) {
                                accesses.add(getterAccessed);
                            }
                        } else if (delegate == null) {
                            // Implicit getter accesses its backing field (the property itself)
                            // Skip if property is delegated as it uses different access mechanism
                            Set<PsiElement> getterAccess = new HashSet<>();
                            getterAccess.add(prop);
                            accesses.add(getterAccess);
                        }

                        // Setter
                        if (prop.isVar()) {
                            KtPropertyAccessor setter = prop.getSetter();
                            if (setter != null && setter.hasBody()) {
                                Set<PsiElement> setterAccessed = collectAccessedFields(setter, instanceProps, prop);
                                if (!setterAccessed.isEmpty()) {
                                    accesses.add(setterAccessed);
                                }
                            } else if (delegate == null) {
                                // Implicit setter accesses its backing field
                                Set<PsiElement> setterAccess = new HashSet<>();
                                setterAccess.add(prop);
                                accesses.add(setterAccess);
                            }
                        }
                    }
                } else if (decl instanceof KtSecondaryConstructor) {
                    // Process secondary constructors
                    KtSecondaryConstructor ctor = (KtSecondaryConstructor) decl;
                    Set<PsiElement> ctorAccessed = collectAccessedFields(ctor, instanceProps, null);
                    if (!ctorAccessed.isEmpty()) {
                        accesses.add(ctorAccessed);
                    }
                }
            }

            // Process init blocks
            for (KtAnonymousInitializer init : body.getAnonymousInitializers()) {
                Set<PsiElement> initAccessed = collectAccessedFieldsFromExpression(
                        init.getBody(), instanceProps, null);
                if (!initAccessed.isEmpty()) {
                    accesses.add(initAccessed);
                }
            }
        }

        // Process primary constructor and its properties
        KtPrimaryConstructor primary = klass.getPrimaryConstructor();
        if (primary != null) {
            // Check for property access in primary constructor body (delegation calls, etc.)
            Set<PsiElement> primaryCtorAccessed = collectAccessedFields(primary, instanceProps, null);
            if (!primaryCtorAccessed.isEmpty()) {
                accesses.add(primaryCtorAccessed);
            }

            // Add implicit accessors for primary constructor properties
            for (KtParameter param : primary.getValueParameters()) {
                if (instanceProps.contains(param)) {
                    // Check if parameter has default value that accesses other properties
                    KtExpression defaultValue = param.getDefaultValue();
                    if (defaultValue != null) {
                        Set<PsiElement> defaultAccessed = collectAccessedFieldsFromExpression(
                                defaultValue, instanceProps, param);
                        if (!defaultAccessed.isEmpty()) {
                            accesses.add(defaultAccessed);
                        }
                    }

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

    /**
     * Collects all instance properties from the class, including both primary constructor
     * properties (declared with val/var) and properties declared in the class body.
     * Excludes companion object properties.
     *
     * @param klass the Kotlin class to analyze
     * @return set of PSI elements representing instance properties
     */
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

    /**
     * Collects all instance properties accessed within a method or accessor body.
     * Handles references to the 'field' keyword in custom accessors.
     *
     * @param method the method or accessor to analyze
     * @param instanceProps set of instance properties in the class
     * @param contextProperty the property being accessed (for 'field' keyword resolution)
     * @return set of accessed properties
     */
    private Set<PsiElement> collectAccessedFields(KtDeclarationWithBody method, Set<PsiElement> instanceProps,
            KtProperty contextProperty) {
        Set<PsiElement> used = new HashSet<>();
        KtExpression body = method.getBodyExpression();
        if (body == null)
            return used;

        return collectAccessedFieldsFromExpression(body, instanceProps, contextProperty);
    }

    /**
     * Recursively collects all instance properties accessed within an expression.
     * This method handles nested scopes including lambdas, anonymous functions,
     * and other complex expressions.
     *
     * @param expression the expression to analyze
     * @param instanceProps set of instance properties in the class
     * @param contextProperty the property being accessed (for 'field' keyword resolution)
     * @return set of accessed properties
     */
    private Set<PsiElement> collectAccessedFieldsFromExpression(KtExpression expression,
            Set<PsiElement> instanceProps, KtProperty contextProperty) {
        Set<PsiElement> used = new HashSet<>();
        if (expression == null)
            return used;

        expression.accept(new KtTreeVisitorVoid() {
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

            @Override
            public void visitLambdaExpression(@NotNull KtLambdaExpression expression) {
                // Lambdas can capture and access instance properties
                super.visitLambdaExpression(expression);
            }

            @Override
            public void visitNamedFunction(@NotNull KtNamedFunction function) {
                // Local functions can access instance properties
                super.visitNamedFunction(function);
            }
        });
        return used;
    }

    /**
     * Computes the number of connected components in the method-property access graph.
     * Uses depth-first search to identify components where methods are connected if they
     * share at least one accessed property.
     *
     * @param accesses list of property access sets, one per method/accessor
     * @return number of disconnected components (LCOM value)
     */
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

    /**
     * Determines if two property access sets share at least one common property.
     *
     * @param a first property access set
     * @param b second property access set
     * @return true if sets have at least one common element
     */
    private boolean shares(Set<PsiElement> a, Set<PsiElement> b) {
        if (a.isEmpty() || b.isEmpty())
            return false;
        for (PsiElement x : a)
            if (b.contains(x))
                return true;
        return false;
    }
}
