/*
 * Kotlin Number Of Children (NOC) - optimized for Kotlin's final-by-default semantics
 */
package org.b333vv.metric.model.visitor.kotlin.type;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.ClassInheritorsSearch;
import com.intellij.util.Query;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.visitor.kotlin.KotlinMetricUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.psi.KtClass;
import org.jetbrains.uast.UClass;
import org.jetbrains.uast.UastContextKt;

import java.util.Collection;

import static org.b333vv.metric.model.metric.MetricType.NOC;

/**
 * Visitor for calculating the Number Of Children (NOC) metric for Kotlin classes.
 * 
 * <h2>Metric Definition</h2>
 * NOC measures the number of immediate subclasses of a class. This metric provides insights into:
 * <ul>
 *   <li>The breadth of inheritance hierarchy at a given level</li>
 *   <li>Potential reusability and abstraction level of the class</li>
 *   <li>Scope of impact when modifying the parent class</li>
 * </ul>
 * 
 * <h2>What is Counted</h2>
 * The metric includes:
 * <ul>
 *   <li><b>Direct subclasses</b>: Classes that explicitly extend the target class using {@code : SuperClass}</li>
 *   <li><b>Interface implementations</b>: Classes that implement the target interface</li>
 *   <li><b>Sealed class subclasses</b>: All permitted subclasses of sealed classes (within same file/package restrictions)</li>
 *   <li><b>Open class subclasses</b>: Subclasses of classes marked with {@code open} keyword</li>
 *   <li><b>Abstract class subclasses</b>: Implementations of abstract classes</li>
 *   <li><b>Mixed-language inheritors</b>: Both Kotlin and Java classes inheriting from the target class</li>
 * </ul>
 * 
 * <h2>What is NOT Counted</h2>
 * The metric excludes:
 * <ul>
 *   <li><b>Final classes</b>: Classes without {@code open}, {@code abstract}, or {@code sealed} modifiers (returns 0 immediately)</li>
 *   <li><b>Inner/nested classes</b>: Nested class relationships are not inheritance</li>
 *   <li><b>Companion objects</b>: Companion object relationships to their containing class</li>
 *   <li><b>Anonymous classes</b>: Anonymous object expressions are not counted as children</li>
 *   <li><b>Indirect descendants</b>: Only direct children are counted, not grandchildren or further descendants</li>
 *   <li><b>Classes outside project scope</b>: Library classes inheriting from project classes are excluded for performance</li>
 * </ul>
 * 
 * <h2>Kotlin-Specific Considerations</h2>
 * <ul>
 *   <li><b>Final by default</b>: Unlike Java, Kotlin classes cannot be inherited unless explicitly marked</li>
 *   <li><b>Sealed classes</b>: Subclasses are restricted and known at compile time</li>
 *   <li><b>Data classes</b>: Cannot be inherited (always final), so NOC is always 0</li>
 *   <li><b>Enum classes</b>: Cannot be inherited, NOC is always 0</li>
 *   <li><b>Object declarations</b>: Singletons cannot be inherited, NOC is always 0</li>
 * </ul>
 * 
 * <h2>Search Scope</h2>
 * The search is limited to the current project scope ({@link GlobalSearchScope#projectScope(Project)}) for:
 * <ul>
 *   <li>Performance optimization (avoids searching through all library dependencies)</li>
 *   <li>Relevance (external library inheritors are typically not under developer control)</li>
 *   <li>Accuracy (counts only project-specific inheritance relationships)</li>
 * </ul>
 * 
 * <h2>Example Calculations</h2>
 * <pre>
 * // Example 1: Final class (default in Kotlin)
 * class FinalClass { }
 * // NOC = 0 (cannot be inherited)
 * 
 * // Example 2: Open class with children
 * open class Parent { }
 * class Child1 : Parent()
 * class Child2 : Parent()
 * // NOC(Parent) = 2
 * 
 * // Example 3: Sealed class
 * sealed class Result {
 *     class Success : Result()
 *     class Error : Result()
 * }
 * // NOC(Result) = 2
 * 
 * // Example 4: Interface
 * interface MyInterface { }
 * class Impl1 : MyInterface
 * class Impl2 : MyInterface
 * // NOC(MyInterface) = 2
 * 
 * // Example 5: Nested classes (NOT counted as children)
 * open class Outer {
 *     class Nested  // Not inheritance, just nesting
 * }
 * // NOC(Outer) = 0 (unless another class extends Outer)
 * </pre>
 * 
 * @see org.b333vv.metric.model.metric.MetricType#NOC
 * @see KotlinMetricUtils#isOpenOrAbstract(KtClass)
 */
public class KotlinNumberOfChildrenVisitor extends KotlinClassVisitor {
    private static final Logger LOG = Logger.getInstance(KotlinNumberOfChildrenVisitor.class);

    /**
     * Visits a Kotlin class and calculates its Number Of Children (NOC) metric.
     * 
     * <p>The calculation follows this algorithm:
     * <ol>
     *   <li>Check if the class can be inherited (open/abstract/sealed/interface)</li>
     *   <li>If final (default), return NOC = 0 immediately</li>
     *   <li>Convert Kotlin PSI to UAST for cross-language inheritor search</li>
     *   <li>Search for direct inheritors within project scope</li>
     *   <li>Count all found inheritors</li>
     * </ol>
     * 
     * @param klass the Kotlin class to analyze
     */
    @Override
    public void visitClass(@NotNull KtClass klass) {
        // Kotlin classes are final by default - they cannot have children unless
        // open/abstract/sealed/interface
        if (!KotlinMetricUtils.isOpenOrAbstract(klass)) {
            metric = Metric.of(NOC, 0);
            return;
        }

        // For open/abstract/sealed/interface classes, search for inheritors
        // using UAST for cross-language support (handles both Kotlin and Java inheritors)
        UClass uClass = UastContextKt.toUElement(klass, UClass.class);
        if (uClass == null) {
            // UAST conversion failed - this can happen for malformed code or during indexing
            LOG.debug("Failed to convert KtClass to UClass for NOC calculation: " + klass.getName());
            metric = Metric.of(NOC, 0);
            return;
        }

        PsiClass psiClass = uClass.getJavaPsi();
        if (psiClass == null) {
            // Unlikely but possible if the Java PSI representation is unavailable
            LOG.debug("Failed to get PsiClass from UClass for NOC calculation: " + klass.getName());
            metric = Metric.of(NOC, 0);
            return;
        }

        try {
            // Search for direct inheritors within project scope only
            // useDeepSearch = false ensures we only find direct children, not all descendants
            Project project = klass.getProject();
            GlobalSearchScope scope = GlobalSearchScope.projectScope(project);
            
            Query<PsiClass> inheritorsQuery = ClassInheritorsSearch.search(
                psiClass, 
                scope,
                false  // checkDeep = false: only direct inheritors
            );
            
            Collection<PsiClass> inheritors = inheritorsQuery.findAll();
            int count = inheritors.size();
            
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format(
                    "NOC for class '%s': found %d direct inheritor(s) in project scope",
                    klass.getName(),
                    count
                ));
            }
            
            metric = Metric.of(NOC, count);
            
        } catch (Exception e) {
            // Handle any unexpected exceptions during inheritor search
            // This ensures the plugin doesn't crash on edge cases
            LOG.warn("Error calculating NOC for class: " + klass.getName(), e);
            metric = Metric.of(NOC, 0);
        }
    }
}
