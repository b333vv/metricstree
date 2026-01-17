/*
 * Kotlin Depth Of Inheritance Tree (DIT) - fully resolved implementation
 */
package org.b333vv.metric.model.visitor.kotlin.type;

import com.intellij.psi.PsiClass;
import org.b333vv.metric.model.metric.Metric;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.psi.*;
import org.jetbrains.uast.UClass;
import org.jetbrains.uast.UastContextKt;

import java.util.HashSet;
import java.util.Set;

import static org.b333vv.metric.model.metric.MetricType.DIT;

/**
 * Calculates the Depth of Inheritance Tree (DIT) metric for Kotlin classes.
 * <p>
 * The DIT metric represents the maximum length from a class to the root of the
 * inheritance hierarchy.
 * It measures how deep a class is in the inheritance tree, which affects
 * complexity and reusability.
 * </p>
 *
 * <h3>Metric Calculation Rules:</h3>
 * <ul>
 * <li><b>Counted in depth:</b>
 * <ul>
 * <li>Direct superclasses (classes extended via inheritance)</li>
 * <li>All ancestor classes in the inheritance chain up to the root</li>
 * </ul>
 * </li>
 * <li><b>NOT counted in depth:</b>
 * <ul>
 * <li>{@code java.lang.Object} - the implicit root of all Java/Kotlin
 * classes</li>
 * <li>{@code kotlin.Any} - Kotlin's root type</li>
 * <li>Interfaces - only class inheritance is considered for DIT</li>
 * </ul>
 * </li>
 * <li><b>Special cases:</b>
 * <ul>
 * <li>Classes with no explicit superclass have DIT = 0</li>
 * <li>Classes directly extending a root class (Object/Any) have DIT = 0</li>
 * <li>Circular inheritance is detected and treated as DIT = 0</li>
 * <li>Unresolved type references are treated as DIT = 0</li>
 * </ul>
 * </li>
 * </ul>
 *
 * <h3>Examples:</h3>
 * 
 * <pre>
 * class A                     // DIT = 0 (no explicit superclass)
 * class B : A                 // DIT = 1 (A is 1 level deep)
 * class C : B                 // DIT = 2 (B -> A, 2 levels deep)
 * interface I
 * class D : I                 // DIT = 0 (interfaces don't count)
 * class E : A, I              // DIT = 1 (only class A counts)
 * </pre>
 *
 * <h3>Implementation Details:</h3>
 * <p>
 * Uses UAST (Unified Abstract Syntax Tree) to resolve Kotlin types to PSI
 * representations,
 * enabling accurate traversal of the inheritance hierarchy across Kotlin and
 * Java classes.
 * Implements cycle detection to handle malformed or circular inheritance
 * gracefully.
 * </p>
 *
 * @see org.b333vv.metric.model.visitor.kotlin.type.KotlinClassVisitor
 */
public class KotlinDepthOfInheritanceTreeVisitor extends KotlinClassVisitor {

    @Override
    public void visitClass(@NotNull KtClass klass) {
        compute(klass);
    }

    @Override
    public void visitObjectDeclaration(@NotNull KtObjectDeclaration declaration) {
        compute(declaration);
    }

    @Override
    public void visitKtFile(@NotNull KtFile file) {
        compute(file);
    }

    private void compute(@NotNull KtElement element) {
        int depth = 0;
        if (element instanceof KtClassOrObject) {
            UClass uClass = UastContextKt.toUElement(element, UClass.class);
            if (uClass != null) {
                PsiClass psiClass = uClass.getJavaPsi();
                if (psiClass != null) {
                    Set<String> visitedClasses = new HashSet<>();
                    depth = calculateInheritanceDepth(psiClass, visitedClasses);
                }
            }
        }
        metric = Metric.of(DIT, depth);
    }

    /**
     * Recursively calculates the inheritance depth for a given PSI class.
     * <p>
     * Traverses up the inheritance hierarchy, counting each level until reaching
     * a root class (Object/Any) or detecting a cycle. Maintains a set of visited
     * classes to prevent infinite recursion in circular inheritance scenarios.
     * </p>
     *
     * @param psiClass       the class to calculate depth for
     * @param visitedClasses set of fully qualified class names already visited in
     *                       this traversal
     * @return the inheritance depth (0 if no superclass or root reached)
     */
    private int calculateInheritanceDepth(PsiClass psiClass, Set<String> visitedClasses) {
        // Null check for robustness
        if (psiClass == null) {
            return 0;
        }

        // Get fully qualified name for cycle detection
        String qualifiedName = psiClass.getQualifiedName();

        // Cycle detection: if we've seen this class before, stop recursion
        if (qualifiedName != null && !visitedClasses.add(qualifiedName)) {
            return 0;
        }

        // Get the superclass
        PsiClass superClass = psiClass.getSuperClass();

        // Base case: no superclass
        if (superClass == null) {
            return 0;
        }

        // Get superclass qualified name
        String superQualifiedName = superClass.getQualifiedName();

        // Exclude root classes from counting (standard DIT practice)
        // java.lang.Object and kotlin.Any are implicit roots and don't add depth
        if ("java.lang.Object".equals(superQualifiedName) ||
                "kotlin.Any".equals(superQualifiedName)) {
            return 0;
        }

        // Recursive case: count this level + depth of superclass
        return 1 + calculateInheritanceDepth(superClass, visitedClasses);
    }
}
