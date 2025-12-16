/*
 * Kotlin Number Of Methods (NOM) - Enhanced implementation
 */
package org.b333vv.metric.model.visitor.kotlin.type;

import org.b333vv.metric.model.metric.Metric;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.psi.*;
import org.jetbrains.kotlin.lexer.KtTokens;

import static org.b333vv.metric.model.metric.MetricType.NOM;

/**
 * Calculates the Number of Methods (NOM) metric for a Kotlin class.
 * <p>
 * This metric counts all executable members that contribute to the class's behavioral complexity:
 *
 * <h3>Counted elements:</h3>
 * <ul>
 *   <li><b>Constructors:</b> Primary constructor (if present) and all secondary constructors</li>
 *   <li><b>Init blocks:</b> All init blocks (each init block counts as one method-equivalent)</li>
 *   <li><b>Named functions:</b> All named functions declared directly in the class body</li>
 *   <li><b>Property accessors:</b> Custom getters and setters with explicit implementations</li>
 *   <li><b>Companion object methods:</b> Functions declared in companion objects (static-like behavior)</li>
 *   <li><b>Nested object methods:</b> Functions in nested objects and inner classes</li>
 * </ul>
 *
 * <h3>Not counted:</h3>
 * <ul>
 *   <li>Abstract function declarations without implementation</li>
 *   <li>Property declarations without custom accessors (default accessors)</li>
 *   <li>Anonymous functions and lambdas within method bodies</li>
 *   <li>Top-level extension functions defined outside the class</li>
 * </ul>
 *
 * <h3>Rationale:</h3>
 * Unlike the Java interpretation where static members are simply class members, Kotlin's
 * companion objects and nested objects are separate types with their own scope. However,
 * they contribute to the overall complexity of the containing class and should be counted
 * for comprehensive complexity analysis.
 *
 * <p>
 * This enhanced implementation provides a more accurate measure of class complexity
 * in Kotlin by accounting for language-specific features like property accessors,
 * init blocks, and object declarations.
 *
 * @see org.b333vv.metric.model.metric.MetricType#NOM
 */
public class KotlinNumberOfMethodsVisitor extends KotlinClassVisitor {

    @Override
    public void visitClass(@NotNull KtClass klass) {
        int count = 0;

        // 1. Primary constructor counts as a method
        if (klass.getPrimaryConstructor() != null) {
            count += 1;
        }

        // 2. Secondary constructors
        count += klass.getSecondaryConstructors().size();

        KtClassBody body = klass.getBody();
        if (body != null) {
            // 3. Init blocks (each init block is a method-equivalent)
            for (KtAnonymousInitializer initializer : klass.getAnonymousInitializers()) {
                count += 1;
            }

            // 4. Process all declarations in class body
            for (KtDeclaration decl : body.getDeclarations()) {
                // 4a. Named functions
                if (decl instanceof KtNamedFunction) {
                    KtNamedFunction function = (KtNamedFunction) decl;
                    // Count only functions with implementation (not abstract)
                    if (!function.hasModifier(KtTokens.ABSTRACT_KEYWORD) || function.hasBody()) {
                        count += 1;
                    }
                }

                // 4b. Properties with custom accessors
                else if (decl instanceof KtProperty) {
                    count += countPropertyAccessors((KtProperty) decl);
                }

                // 4c. Companion objects and nested objects
                else if (decl instanceof KtObjectDeclaration) {
                    KtObjectDeclaration objectDecl = (KtObjectDeclaration) decl;
                    count += countMethodsInObjectDeclaration(objectDecl);
                }

                // 4d. Inner and nested classes (recursive)
                else if (decl instanceof KtClass) {
                    KtClass nestedClass = (KtClass) decl;
                    // For nested/inner classes, count their methods as part of complexity
                    // (optional: you can make this configurable)
                    count += countMethodsInNestedClass(nestedClass);
                }
            }
        }

        metric = Metric.of(NOM, count);
    }

    /**
     * Counts custom property accessors (getters and setters with explicit implementations).
     * Default accessors are not counted as they don't add behavioral complexity.
     *
     * @param property the property to analyze
     * @return number of custom accessors (0, 1, or 2)
     */
    private int countPropertyAccessors(@NotNull KtProperty property) {
        int count = 0;

        // Custom getter with implementation
        KtPropertyAccessor getter = property.getGetter();
        if (getter != null && hasNonTrivialBody(getter)) {
            count += 1;
        }

        // Custom setter with implementation
        KtPropertyAccessor setter = property.getSetter();
        if (setter != null && hasNonTrivialBody(setter)) {
            count += 1;
        }

        return count;
    }

    /**
     * Checks if a property accessor has a non-trivial implementation.
     * An accessor is non-trivial if it has an explicit body or expression.
     *
     * @param accessor the property accessor to check
     * @return true if the accessor has custom logic
     */
    private boolean hasNonTrivialBody(@NotNull KtPropertyAccessor accessor) {
        // Check for block body: get() { ... }
        if (accessor.hasBody() && accessor.getBodyExpression() != null) {
            return true;
        }

        // Check for expression body: get() = field
        // Note: default accessors don't have explicit bodyExpression set
        return accessor.getBodyBlockExpression() != null;
    }

    /**
     * Counts methods in an object declaration (companion object or nested object).
     * Includes both named functions and property accessors.
     *
     * @param objectDecl the object declaration to analyze
     * @return total method count in the object
     */
    private int countMethodsInObjectDeclaration(@NotNull KtObjectDeclaration objectDecl) {
        int count = 0;
        KtClassBody body = objectDecl.getBody();

        if (body != null) {
            for (KtDeclaration decl : body.getDeclarations()) {
                // Functions in object
                if (decl instanceof KtNamedFunction) {
                    KtNamedFunction function = (KtNamedFunction) decl;
                    if (!function.hasModifier(KtTokens.ABSTRACT_KEYWORD) || function.hasBody()) {
                        count += 1;
                    }
                }

                // Properties with custom accessors in object
                else if (decl instanceof KtProperty) {
                    count += countPropertyAccessors((KtProperty) decl);
                }
            }
        }

        return count;
    }

    /**
     * Counts methods in nested or inner classes.
     * This is optional and can be configured based on whether you want
     * to include nested class complexity in the parent class metric.
     *
     * @param nestedClass the nested class to analyze
     * @return method count in nested class (0 if you want to exclude nested classes)
     */
    private int countMethodsInNestedClass(@NotNull KtClass nestedClass) {
        // Option 1: Include nested class methods (default)
        // This provides a comprehensive view of class complexity
        int count = 0;

        // Constructors
        if (nestedClass.getPrimaryConstructor() != null) {
            count += 1;
        }
        count += nestedClass.getSecondaryConstructors().size();

        // Init blocks
        count += nestedClass.getAnonymousInitializers().size();

        KtClassBody body = nestedClass.getBody();
        if (body != null) {
            for (KtDeclaration decl : body.getDeclarations()) {
                if (decl instanceof KtNamedFunction) {
                    KtNamedFunction function = (KtNamedFunction) decl;
                    if (!function.hasModifier(KtTokens.ABSTRACT_KEYWORD) || function.hasBody()) {
                        count += 1;
                    }
                }
                else if (decl instanceof KtProperty) {
                    count += countPropertyAccessors((KtProperty) decl);
                }
            }
        }

        return count;

        // Option 2: Exclude nested class methods
        // Return 0 if you want to measure only direct class complexity
        // return 0;
    }
}
