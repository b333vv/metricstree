package org.b333vv.metric.model.visitor.kotlin.type;

import org.b333vv.metric.model.metric.Metric;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.psi.*;

import static org.b333vv.metric.model.metric.MetricType.NOO;

/**
 * Calculates the Number of Operations (NOO) metric for a Kotlin class.
 * <p>
 * This metric counts all declared operations (methods and property accessors)
 * that contribute to the class's behavioral interface. The metric focuses on
 * explicitly declared operations, excluding constructors and inherited members
 * (due to PSI-only analysis limitations).
 *
 * <h3>Counted elements:</h3>
 * <ul>
 *   <li><b>Named functions:</b> All named functions declared directly in the class body,
 *       including regular methods, operator functions, infix functions, and extension functions</li>
 *   <li><b>Custom property accessors:</b> Getters and setters with explicit implementations
 *       (properties with custom logic in get() or set() blocks)</li>
 *   <li><b>Companion object functions:</b> Named functions declared in companion objects
 *       (static-like behavior accessible via class name)</li>
 *   <li><b>Nested object functions:</b> Named functions in nested object declarations
 *       and their custom property accessors</li>
 * </ul>
 *
 * <h3>Not counted:</h3>
 * <ul>
 *   <li>Constructors (primary and secondary) - excluded by metric definition</li>
 *   <li>Init blocks - initialization logic, not operations</li>
 *   <li>Properties with default accessors - no custom behavioral logic</li>
 *   <li>Inherited or overridden methods - PSI-only analysis limitation</li>
 *   <li>Anonymous functions and lambdas - not class-level operations</li>
 *   <li>Abstract function declarations - no implementation</li>
 * </ul>
 *
 * <h3>Examples:</h3>
 * <pre>{@code
 * class Example {
 *     // Counted: 1 (named function)
 *     fun doSomething() { }
 *
 *     // Counted: 2 (custom getter + custom setter)
 *     var value: Int = 0
 *         get() = field * 2
 *         set(v) { field = v }
 *
 *     // Not counted: default accessors
 *     var name: String = ""
 *
 *     // Counted: 1 (operator function)
 *     operator fun plus(other: Example): Example = this
 *
 *     companion object {
 *         // Counted: 1 (companion object function)
 *         fun create(): Example = Example()
 *     }
 * }
 * // Total NOO = 5
 * }</pre>
 *
 * <h3>Rationale:</h3>
 * The NOO metric measures the behavioral complexity and public interface of a class.
 * Kotlin's property accessors with custom logic are equivalent to methods in terms of
 * behavioral complexity. Companion object functions are included as they represent
 * static-like operations accessible through the class interface.
 *
 * @see org.b333vv.metric.model.metric.MetricType#NOO
 */
public class KotlinNumberOfOperationsVisitor extends KotlinClassVisitor {

    @Override
    public void visitClass(@NotNull KtClass klass) {
        int count = 0;
        KtClassBody body = klass.getBody();
        if (body != null) {
            count = countOperations(body);
        }
        metric = Metric.of(NOO, count);
    }

    /**
     * Recursively counts operations in a class body, including functions,
     * custom property accessors, and operations in nested objects.
     *
     * @param body the class body to analyze
     * @return total count of operations
     */
    private int countOperations(@NotNull KtClassBody body) {
        int count = 0;

        for (KtDeclaration d : body.getDeclarations()) {
            // Count named functions (includes regular, operator, infix, extension functions)
            if (d instanceof KtNamedFunction) {
                count++;
            }
            // Count custom property accessors
            else if (d instanceof KtProperty) {
                KtProperty prop = (KtProperty) d;

                // Custom getter with implementation
                KtPropertyAccessor getter = prop.getGetter();
                if (getter != null && getter.hasBody()) {
                    count++;
                }

                // Custom setter with implementation
                KtPropertyAccessor setter = prop.getSetter();
                if (setter != null && setter.hasBody()) {
                    count++;
                }
            }
            // Recursively count operations in companion/nested objects
            else if (d instanceof KtObjectDeclaration) {
                KtObjectDeclaration obj = (KtObjectDeclaration) d;
                KtClassBody objBody = obj.getBody();
                if (objBody != null) {
                    count += countOperations(objBody);
                }
            }
        }

        return count;
    }
}
