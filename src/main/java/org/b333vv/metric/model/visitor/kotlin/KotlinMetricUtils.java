/*
 * Kotlin Metric Utilities - Helper methods for Kotlin-specific metric calculations
 */
package org.b333vv.metric.model.visitor.kotlin;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.lexer.KtTokens;
import org.jetbrains.kotlin.psi.*;

/**
 * Utility class providing helper methods for calculating Kotlin-specific metrics.
 * <p>
 * Centralizes logic for handling implicit methods, property accessors, delegated properties,
 * and Kotlin modifiers. This class provides consistent calculation strategies across different
 * metric visitors.
 * </p>
 */
public class KotlinMetricUtils {

    /**
     * Counts the number of implicit accessor methods for all properties in a class.
     * <ul>
     *   <li>For each 'val' property: 1 getter</li>
     *   <li>For each 'var' property: 1 getter + 1 setter = 2 accessors</li>
     * </ul>
     *
     * @param klass the Kotlin class to analyze
     * @return total count of implicit accessors
     */
    public static int countPropertyAccessors(@NotNull KtClass klass) {
        int count = 0;

        // Properties from primary constructor
        KtPrimaryConstructor primary = klass.getPrimaryConstructor();
        if (primary != null) {
            for (KtParameter param : primary.getValueParameters()) {
                if (param.hasValOrVar()) {
                    // val = 1 getter, var = 1 getter + 1 setter
                    count += param.isMutable() ? 2 : 1;
                }
            }
        }

        // Properties in class body
        KtClassBody body = klass.getBody();
        if (body != null) {
            for (KtDeclaration decl : body.getDeclarations()) {
                if (decl instanceof KtProperty) {
                    KtProperty prop = (KtProperty) decl;
                    // Skip properties in companion objects (they're static-like)
                    if (isInCompanionObject(prop))
                        continue;
                    // val = 1 getter, var = 1 getter + 1 setter
                    count += prop.isVar() ? 2 : 1;
                }
            }
        }

        return count;
    }

    /**
     * Counts the number of implicit methods generated for a data class.
     * <p>
     * Data classes automatically generate:
     * <ul>
     *   <li>equals(Any): Boolean - complexity scales with property count</li>
     *   <li>hashCode(): Int - complexity scales with property count</li>
     *   <li>toString(): String - baseline complexity 1</li>
     *   <li>copy(...): ClassName - baseline complexity 1</li>
     *   <li>componentN() for each primary constructor parameter - 1 per component</li>
     * </ul>
     * </p>
     * <p>
     * <b>Improved calculation:</b> equals() and hashCode() have complexity that scales
     * with the number of properties, as they must compare/hash each property.
     * </p>
     *
     * @param klass the Kotlin class to analyze
     * @return total complexity of implicit data class methods, or 0 if not a data class
     */
    public static int countDataClassMethods(@NotNull KtClass klass) {
        if (!klass.isData()) {
            return 0;
        }

        int paramCount = 0;
        KtPrimaryConstructor primary = klass.getPrimaryConstructor();
        if (primary != null) {
            paramCount = primary.getValueParameters().size();
        }

        // equals and hashCode scale with property count (each property adds a comparison/hash)
        // Minimum complexity is 1 for each method
        int equalsComplexity = Math.max(1, paramCount);
        int hashCodeComplexity = Math.max(1, paramCount);

        // toString, copy have baseline complexity 1
        int toStringComplexity = 1;
        int copyComplexity = 1;

        // componentN functions: one per parameter, each with complexity 1
        int componentComplexity = paramCount;

        return equalsComplexity + hashCodeComplexity + toStringComplexity +
                copyComplexity + componentComplexity;
    }

    /**
     * Checks if a property has a custom accessor (getter or setter with a body).
     *
     * @param property the property to check
     * @return true if the property has a custom getter or setter
     */
    public static boolean hasCustomAccessor(@NotNull KtProperty property) {
        KtPropertyAccessor getter = property.getGetter();
        if (getter != null && getter.hasBody()) {
            return true;
        }

        KtPropertyAccessor setter = property.getSetter();
        return setter != null && setter.hasBody();
    }

    /**
     * Calculates the cyclomatic complexity of accessors for a property.
     * <p>
     * Handles three types of properties:
     * <ul>
     *   <li><b>Delegated properties</b> - complexity based on delegate expression</li>
     *   <li><b>Custom accessors</b> - complexity calculated from accessor body</li>
     *   <li><b>Standard properties</b> - baseline complexity (1 for getter, 2 for var)</li>
     * </ul>
     * </p>
     *
     * @param property the property to analyze
     * @return total complexity of accessors (getter + setter if present)
     */
    public static int getAccessorComplexity(@NotNull KtProperty property) {
        // Check for delegated property first
        KtPropertyDelegate delegate = property.getDelegate();
        if (delegate != null) {
            return calculateDelegateComplexity(delegate);
        }

        int complexity = 0;

        // Getter complexity
        KtPropertyAccessor getter = property.getGetter();
        if (getter != null) {
            if (getter.hasBody()) {
                // Custom getter - calculate actual complexity
                complexity += calculateAccessorBodyComplexity(getter.getBodyExpression());
            } else {
                // Standard getter
                complexity += 1;
            }
        } else {
            // Implicit getter
            complexity += 1;
        }

        // Setter complexity (only for var)
        if (property.isVar()) {
            KtPropertyAccessor setter = property.getSetter();
            if (setter != null) {
                if (setter.hasBody()) {
                    // Custom setter - calculate actual complexity
                    complexity += calculateAccessorBodyComplexity(setter.getBodyExpression());
                } else {
                    // Standard setter
                    complexity += 1;
                }
            } else {
                // Implicit setter
                complexity += 1;
            }
        }

        return complexity;
    }

    /**
     * Calculates complexity for a delegated property.
     * <p>
     * Delegated properties (e.g., {@code by lazy}, custom delegates) have implicit
     * getter/setter methods that delegate to the delegate object. The complexity
     * includes the delegate expression itself plus baseline accessor complexity.
     * </p>
     * <p>
     * For {@code val} properties: 1 (getter delegation) + delegate expression complexity<br>
     * For {@code var} properties: 2 (getter + setter delegation) + delegate expression complexity
     * </p>
     *
     * @param delegate the property delegate to analyze
     * @return total complexity of the delegated property
     */
    private static int calculateDelegateComplexity(@NotNull KtPropertyDelegate delegate) {
        KtExpression delegateExpr = delegate.getExpression();
        int delegateExprComplexity = calculateAccessorBodyComplexity(delegateExpr);

        // Delegated properties have implicit getter (and setter for var)
        // The delegation itself adds complexity
        KtProperty property = (KtProperty) delegate.getParent();
        int accessorCount = property.isVar() ? 2 : 1; // getter + setter for var, just getter for val

        return accessorCount + delegateExprComplexity;
    }

    /**
     * Calculates cyclomatic complexity for an accessor body.
     * <p>
     * This is a simplified version that counts basic control flow structures:
     * <ul>
     *   <li>if expressions</li>
     *   <li>when expressions (each entry)</li>
     *   <li>loops (for, while)</li>
     *   <li>boolean operators (&&, ||, elvis ?:)</li>
     * </ul>
     * </p>
     *
     * @param body the accessor body expression
     * @return cyclomatic complexity (minimum 1)
     */
    private static int calculateAccessorBodyComplexity(KtExpression body) {
        if (body == null)
            return 1;

        final int[] complexity = { 1 }; // baseline

        body.accept(new KtTreeVisitorVoid() {
            @Override
            public void visitIfExpression(@NotNull KtIfExpression expression) {
                complexity[0] += 1;
                super.visitIfExpression(expression);
            }

            @Override
            public void visitWhenExpression(@NotNull KtWhenExpression expression) {
                complexity[0] += Math.max(1, expression.getEntries().size());
                super.visitWhenExpression(expression);
            }

            @Override
            public void visitForExpression(@NotNull KtForExpression expression) {
                complexity[0] += 1;
                super.visitForExpression(expression);
            }

            @Override
            public void visitWhileExpression(@NotNull KtWhileExpression expression) {
                complexity[0] += 1;
                super.visitWhileExpression(expression);
            }

            @Override
            public void visitBinaryExpression(@NotNull KtBinaryExpression expression) {
                if (expression.getOperationToken() == KtTokens.ANDAND ||
                        expression.getOperationToken() == KtTokens.OROR ||
                        expression.getOperationToken() == KtTokens.ELVIS) {
                    complexity[0] += 1;
                }
                super.visitBinaryExpression(expression);
            }
        });

        return complexity[0];
    }

    /**
     * Checks if a declaration has the 'internal' visibility modifier.
     *
     * @param element the element to check
     * @return true if the element has 'internal' visibility
     */
    public static boolean isInternalVisibility(@NotNull KtModifierListOwner element) {
        return element.hasModifier(KtTokens.INTERNAL_KEYWORD);
    }

    /**
     * Checks if a class can be inherited (is open or abstract).
     * In Kotlin, classes are final by default.
     *
     * @param klass the class to check
     * @return true if the class is open or abstract
     */
    public static boolean isOpenOrAbstract(@NotNull KtClass klass) {
        return klass.hasModifier(KtTokens.OPEN_KEYWORD) ||
                klass.hasModifier(KtTokens.ABSTRACT_KEYWORD) ||
                klass.isInterface() ||
                klass.isSealed();
    }

    /**
     * Checks if a property is declared inside a companion object.
     *
     * @param property the property to check
     * @return true if the property is in a companion object
     */
    public static boolean isInCompanionObject(@NotNull KtProperty property) {
        PsiElement parent = property.getParent();
        while (parent != null && !(parent instanceof KtClassBody)) {
            parent = parent.getParent();
        }
        if (parent instanceof KtClassBody) {
            PsiElement maybeCompanion = parent.getParent();
            if (maybeCompanion instanceof KtObjectDeclaration) {
                return ((KtObjectDeclaration) maybeCompanion).isCompanion();
            }
        }
        return false;
    }

    /**
     * Checks if a property is effectively public (not private, protected, or internal).
     *
     * @param property the property to check
     * @return true if the property is public
     */
    public static boolean isPublicProperty(@NotNull KtProperty property) {
        return !property.hasModifier(KtTokens.PRIVATE_KEYWORD) &&
                !property.hasModifier(KtTokens.PROTECTED_KEYWORD) &&
                !property.hasModifier(KtTokens.INTERNAL_KEYWORD);
    }
}
