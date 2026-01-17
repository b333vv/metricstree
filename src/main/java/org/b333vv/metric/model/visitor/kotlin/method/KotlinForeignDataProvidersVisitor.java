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

package org.b333vv.metric.model.visitor.kotlin.method;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.util.PropertyUtil;
import org.b333vv.metric.model.metric.Metric;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.asJava.classes.KtLightClass;
import org.jetbrains.kotlin.psi.*;

import java.util.HashSet;
import java.util.Set;

import static org.b333vv.metric.model.metric.MetricType.FDP;

/**
 * Visitor for calculating the Foreign Data Providers (FDP) metric for Kotlin
 * functions.
 * 
 * <p>
 * The FDP metric counts the number of distinct external classes (foreign
 * providers)
 * whose data (properties or fields) are accessed within a function. A lower FDP
 * value
 * indicates better encapsulation and reduced coupling.
 * </p>
 * 
 * <h3>What is counted as a foreign data provider:</h3>
 * <ul>
 * <li><b>Direct property access:</b> Qualified expressions like
 * {@code obj.property} or {@code obj?.property},
 * where the property belongs to a class different from the function's
 * containing class</li>
 * <li><b>Getter/Setter calls:</b> Method calls that are recognized as simple
 * property accessors
 * (getters or setters) via {@link PropertyUtil}, counting the class owning the
 * accessor method</li>
 * <li><b>Field references:</b> References to fields from external classes
 * (including Java interop)</li>
 * </ul>
 * 
 * <h3>What is excluded from the count:</h3>
 * <ul>
 * <li><b>Local access:</b> References to {@code this} or {@code super}, and
 * unqualified property names
 * (treated as local to the function or its class)</li>
 * <li><b>Own class:</b> Properties and fields from the function's containing
 * class</li>
 * <li><b>Parent classes:</b> Properties and fields from any superclass or
 * superinterface
 * of the function's containing class (excluded to avoid counting inherited
 * members)</li>
 * </ul>
 * 
 * <h3>Resolution strategy:</h3>
 * <ol>
 * <li>For qualified expressions, attempt to resolve the selector to a
 * {@link KtProperty} or {@link PsiField}</li>
 * <li>Determine the fully qualified name (FQN) of the class owning that
 * property/field</li>
 * <li>If the FQN differs from the function's containing class and its parents,
 * count it as a foreign provider</li>
 * <li>Use textual receiver representation as a fallback key only when
 * resolution fails</li>
 * </ol>
 * 
 * <h3>Example:</h3>
 * 
 * <pre>
 * class OrderService {
 *     fun processOrder(order: Order, user: User) {
 *         val amount = order.totalAmount    // FDP +1 (Order)
 *         val name = user.name              // FDP +1 (User)
 *         val email = user.email            // Still User, no increment
 *         println(amount)                   // println is not data access
 *     }
 * }
 * // Result: FDP = 2 (Order and User)
 * </pre>
 * 
 * @see org.b333vv.metric.model.visitor.method.ForeignDataProvidersVisitor
 */
public class KotlinForeignDataProvidersVisitor extends KotlinMethodVisitor {

    @Override
    public void visitNamedFunction(@NotNull KtNamedFunction function) {
        compute(function, function.getBodyExpression());
    }

    @Override
    public void visitSecondaryConstructor(@NotNull KtSecondaryConstructor constructor) {
        compute(constructor, constructor.getBodyExpression());
    }

    @Override
    public void visitPrimaryConstructor(@NotNull KtPrimaryConstructor constructor) {
        metric = Metric.of(FDP, 0);
    }

    @Override
    public void visitAnonymousInitializer(@NotNull KtAnonymousInitializer initializer) {
        compute(initializer, initializer.getBody());
    }

    private void compute(@NotNull KtElement context, @Nullable KtExpression body) {
        if (body == null) {
            metric = Metric.of(FDP, 0);
            return;
        }

        KtClassOrObject ownerClass = findOwnerClass(context);
        final Set<String> foreignProviderFqns = new HashSet<>();

        body.accept(new KtTreeVisitorVoid() {
            @Override
            public void visitDotQualifiedExpression(@NotNull KtDotQualifiedExpression expression) {
                processQualifiedExpression(expression.getSelectorExpression(),
                        expression.getReceiverExpression(),
                        ownerClass,
                        foreignProviderFqns);
                super.visitDotQualifiedExpression(expression);
            }

            @Override
            public void visitSafeQualifiedExpression(@NotNull KtSafeQualifiedExpression expression) {
                processQualifiedExpression(expression.getSelectorExpression(),
                        expression.getReceiverExpression(),
                        ownerClass,
                        foreignProviderFqns);
                super.visitSafeQualifiedExpression(expression);
            }

            @Override
            public void visitCallExpression(@NotNull KtCallExpression expression) {
                processCallExpression(expression, ownerClass, foreignProviderFqns);
                super.visitCallExpression(expression);
            }
        });

        metric = Metric.of(FDP, foreignProviderFqns.size());
    }

    /**
     * Processes qualified expressions (dot or safe call) to identify foreign data
     * providers.
     */
    private void processQualifiedExpression(@Nullable KtExpression selector,
            @Nullable KtExpression receiver,
            @Nullable KtClassOrObject ownerClass,
            @NotNull Set<String> foreignProviderFqns) {
        if (selector == null || receiver == null) {
            return;
        }

        // Exclude this/super references
        if (receiver instanceof KtThisExpression || receiver instanceof KtSuperExpression) {
            return;
        }

        String providerFqn = resolveProviderFqn(selector, ownerClass);
        if (providerFqn != null) {
            foreignProviderFqns.add(providerFqn);
        }
    }

    /**
     * Processes call expressions to detect getter/setter method calls.
     */
    private void processCallExpression(@NotNull KtCallExpression callExpression,
            @Nullable KtClassOrObject ownerClass,
            @NotNull Set<String> foreignProviderFqns) {
        for (var ref : callExpression.getReferences()) {
            PsiElement resolved = ref.resolve();
            if (resolved instanceof PsiMethod) {
                PsiMethod method = (PsiMethod) resolved;

                // Check if it's a getter or setter
                if (PropertyUtil.isSimpleGetter(method) || PropertyUtil.isSimpleSetter(method)) {
                    PsiClass containingClass = method.getContainingClass();
                    if (containingClass != null) {
                        String fqn = containingClass.getQualifiedName();
                        if (fqn != null && isForeignProvider(fqn, ownerClass)) {
                            foreignProviderFqns.add(fqn);
                        }
                    }
                }
            }
        }
    }

    /**
     * Resolves the fully qualified name of the class owning the accessed
     * property/field.
     * 
     * @param selector   the property access expression
     * @param ownerClass the class containing the current function
     * @return FQN of the foreign provider class, or null if local/unresolved
     */
    @Nullable
    private String resolveProviderFqn(@NotNull KtExpression selector, @Nullable KtClassOrObject ownerClass) {
        if (!(selector instanceof KtSimpleNameExpression)) {
            return null;
        }

        for (var ref : selector.getReferences()) {
            PsiElement resolved = ref.resolve();

            // Handle Kotlin properties
            if (resolved instanceof KtProperty) {
                KtClassOrObject propertyOwner = findOwnerClass((KtProperty) resolved);
                if (propertyOwner != null && propertyOwner.getFqName() != null) {
                    String fqn = propertyOwner.getFqName().asString();
                    if (isForeignProvider(fqn, ownerClass)) {
                        return fqn;
                    }
                }
            }

            // Handle Java fields (for interop)
            if (resolved instanceof PsiField) {
                PsiField field = (PsiField) resolved;
                PsiClass fieldClass = field.getContainingClass();
                if (fieldClass != null) {
                    String fqn = fieldClass.getQualifiedName();
                    if (fqn != null && isForeignProvider(fqn, ownerClass)) {
                        return fqn;
                    }
                }
            }
        }

        return null;
    }

    /**
     * Determines if a given FQN represents a foreign provider (not own class or
     * parent).
     * 
     * @param candidateFqn the FQN to check
     * @param ownerClass   the function's containing class
     * @return true if the FQN is foreign, false if it's the own class or a parent
     */
    private boolean isForeignProvider(@NotNull String candidateFqn, @Nullable KtClassOrObject ownerClass) {
        if (ownerClass == null) {
            return true;
        }

        // Check if it's the owner class itself
        if (ownerClass.getFqName() != null && candidateFqn.equals(ownerClass.getFqName().asString())) {
            return false;
        }

        // Check if it's a parent class (superclass or interface)
        for (KtSuperTypeListEntry superTypeEntry : ownerClass.getSuperTypeListEntries()) {
            KtTypeReference typeRef = superTypeEntry.getTypeReference();
            if (typeRef != null) {
                for (var ref : typeRef.getReferences()) {
                    PsiElement resolved = ref.resolve();

                    // Handle Kotlin classes
                    if (resolved instanceof KtClassOrObject) {
                        KtClassOrObject superClass = (KtClassOrObject) resolved;
                        if (superClass.getFqName() != null &&
                                candidateFqn.equals(superClass.getFqName().asString())) {
                            return false;
                        }
                    }

                    // Handle Java classes (for interop)
                    if (resolved instanceof KtLightClass) {
                        KtLightClass lightClass = (KtLightClass) resolved;
                        String superFqn = lightClass.getQualifiedName();
                        if (superFqn != null && candidateFqn.equals(superFqn)) {
                            return false;
                        }
                    }

                    if (resolved instanceof PsiClass) {
                        PsiClass psiClass = (PsiClass) resolved;
                        String superFqn = psiClass.getQualifiedName();
                        if (superFqn != null && candidateFqn.equals(superFqn)) {
                            return false;
                        }
                    }
                }
            }
        }

        return true;
    }

    /**
     * Finds the containing class for a given element by traversing up the PSI tree.
     */
    @Nullable
    private KtClassOrObject findOwnerClass(@NotNull KtElement element) {
        PsiElement current = element;
        while (current != null) {
            if (current instanceof KtClassOrObject) {
                return (KtClassOrObject) current;
            }
            current = current.getParent();
        }
        return null;
    }
}
