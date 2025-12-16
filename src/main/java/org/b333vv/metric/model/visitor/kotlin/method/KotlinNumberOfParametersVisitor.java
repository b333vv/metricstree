/*
 * Kotlin Number Of Parameters - Phase 2.4.0
 */
package org.b333vv.metric.model.visitor.kotlin.method;

import org.b333vv.metric.model.metric.Metric;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.psi.KtNamedFunction;
import org.jetbrains.kotlin.psi.KtParameter;
import org.jetbrains.kotlin.psi.KtPrimaryConstructor;
import org.jetbrains.kotlin.psi.KtSecondaryConstructor;

import java.util.List;

import static org.b333vv.metric.model.metric.MetricType.NOPM;

/**
 * Visitor for calculating the Number of Parameters (NOPM) metric for Kotlin functions and constructors.
 * 
 * <p>This metric counts all formal parameters declared in a function or constructor signature.
 * The calculation includes:</p>
 * <ul>
 *   <li><b>Regular parameters</b> - standard function parameters with or without default values</li>
 *   <li><b>Vararg parameters</b> - parameters marked with {@code vararg} modifier (counted as 1 parameter)</li>
 *   <li><b>Extension receiver</b> - implicit receiver type in extension functions (counted as 1 parameter)</li>
 *   <li><b>Constructor property parameters</b> - primary constructor parameters declared with {@code val/var}</li>
 *   <li><b>Lambda parameters</b> - function-type parameters, including trailing lambdas</li>
 * </ul>
 * 
 * <p>The metric does <b>not</b> include:</p>
 * <ul>
 *   <li>Lambda receiver types (e.g., in {@code String.() -> Unit}, the String receiver is not counted)</li>
 *   <li>Generic type parameters (e.g., {@code <T>} in function signatures)</li>
 *   <li>Context receivers (experimental Kotlin feature)</li>
 * </ul>
 * 
 * <p><b>Rationale:</b> The NOPM metric helps identify functions with high parameter counts, which may indicate:</p>
 * <ul>
 *   <li>Poor function cohesion and potential violation of Single Responsibility Principle</li>
 *   <li>Difficulty in testing due to complex parameter combinations</li>
 *   <li>Reduced readability and maintainability</li>
 *   <li>Opportunities for parameter object refactoring</li>
 * </ul>
 * 
 * <p><b>Extension Functions:</b> For Kotlin extension functions, the extension receiver type is counted
 * as an additional parameter since it represents an implicit first parameter that affects the function's
 * interface and complexity.</p>
 * 
 * <p><b>Examples:</b></p>
 * <pre>{@code
 * // NOPM = 2 (regular parameters)
 * fun calculate(x: Int, y: Int): Int = x + y
 * 
 * // NOPM = 3 (2 regular + 1 extension receiver)
 * fun String.format(width: Int, align: Char): String = ...
 * 
 * // NOPM = 2 (1 regular + 1 vararg, vararg counts as 1)
 * fun sum(initial: Int, vararg values: Int): Int = ...
 * 
 * // NOPM = 3 (3 constructor parameters, all are properties)
 * class User(val name: String, val age: Int, var email: String)
 * 
 * // NOPM = 1 (function-type parameter)
 * fun process(handler: (String) -> Unit) = ...
 * }</pre>
 * 
 * @see org.b333vv.metric.model.metric.MetricType#NOPM
 * @since 2.4.0
 */
public class KotlinNumberOfParametersVisitor extends KotlinMethodVisitor {
    
    /**
     * Visits a named function (including extension functions) and calculates the NOPM metric.
     * For extension functions, adds 1 to account for the implicit receiver parameter.
     * 
     * @param function the Kotlin named function to analyze
     */
    @Override
    public void visitNamedFunction(@NotNull KtNamedFunction function) {
        int count = countParameters(function.getValueParameters());
        
        // Extension functions have an implicit receiver parameter
        if (function.getReceiverTypeReference() != null) {
            count++;
        }
        
        metric = Metric.of(NOPM, count);
    }

    /**
     * Visits a primary constructor and calculates the NOPM metric.
     * Counts all parameters including property declarations (val/var).
     * 
     * @param constructor the Kotlin primary constructor to analyze
     */
    @Override
    public void visitPrimaryConstructor(@NotNull KtPrimaryConstructor constructor) {
        int count = countParameters(constructor.getValueParameters());
        metric = Metric.of(NOPM, count);
    }

    /**
     * Visits a secondary constructor and calculates the NOPM metric.
     * 
     * @param constructor the Kotlin secondary constructor to analyze
     */
    @Override
    public void visitSecondaryConstructor(@NotNull KtSecondaryConstructor constructor) {
        int count = countParameters(constructor.getValueParameters());
        metric = Metric.of(NOPM, count);
    }
    
    /**
     * Counts the number of parameters in the given list.
     * Each parameter is counted as 1, regardless of whether it's a vararg, has a default value,
     * or is a property parameter (val/var in primary constructor).
     * 
     * @param parameters the list of parameters to count
     * @return the total number of parameters
     */
    private int countParameters(List<KtParameter> parameters) {
        if (parameters == null) {
            return 0;
        }
        return parameters.size();
    }
}