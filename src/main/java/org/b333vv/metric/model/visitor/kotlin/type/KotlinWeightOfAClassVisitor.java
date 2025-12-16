package org.b333vv.metric.model.visitor.kotlin.type;

import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.lexer.KtTokens;
import org.jetbrains.kotlin.psi.*;

import com.intellij.psi.PsiElement;
import java.util.List;

import static org.b333vv.metric.model.metric.MetricType.WOC;

/**
 * Kotlin Weight Of A Class (WOC) Metric Calculator
 *
 * <p><b>Definition:</b> WOC = (number of functional methods) / (total declared methods)</p>
 *
 * <p><b>Purpose:</b> Measures the proportion of methods in a class that contain actual business logic
 * versus trivial accessors, delegations, or boilerplate code. Higher WOC indicates more behavioral
 * complexity, while lower WOC suggests a data-centric class design.</p>
 *
 * <h3>What is Counted as TOTAL Methods:</h3>
 * <ul>
 *   <li>All named functions declared in the class body ({@link KtNamedFunction})</li>
 *   <li>Excludes constructors (primary and secondary)</li>
 *   <li>Excludes init blocks</li>
 *   <li>Excludes property declarations themselves (but counts explicit accessor methods)</li>
 * </ul>
 *
 * <h3>What is Counted as FUNCTIONAL (Non-Trivial) Methods:</h3>
 * <ul>
 *   <li><b>Included:</b>
 *     <ul>
 *       <li>Methods with multiple statements in body (> 2 lines)</li>
 *       <li>Methods with complex expressions (when, if, lambdas)</li>
 *       <li>Business logic methods calling external services/repositories</li>
 *       <li>Operator overloading functions with non-trivial logic</li>
 *       <li>Methods performing computations or transformations</li>
 *     </ul>
 *   </li>
 *   <li><b>Excluded (Non-Functional):</b>
 *     <ul>
 *       <li>Abstract methods (no implementation)</li>
 *       <li>External methods (delegated to native code)</li>
 *       <li>Property accessors (getters/setters matching class properties)</li>
 *       <li>Simple delegations: {@code return field} or {@code field = value}</li>
 *       <li>Trivial single-call delegations: {@code return otherObject.method()}</li>
 *       <li>Data class component methods (componentN for destructuring)</li>
 *       <li>Empty methods or methods with only return statement of a simple reference</li>
 *       <li>Simple field assignments in expression body</li>
 *     </ul>
 *   </li>
 * </ul>
 *
 * <h3>Kotlin-Specific Handling:</h3>
 * <ul>
 *   <li><b>Properties:</b> Automatically generated property accessors are excluded. Explicit
 *       custom getters/setters with logic are counted as functional.</li>
 *   <li><b>Backing Fields:</b> Methods that simply return or assign to backing fields
 *       (using {@code field} keyword) are considered non-functional.</li>
 *   <li><b>Operator Overloading:</b> Operators like {@code plus}, {@code invoke}, {@code get}
 *       are analyzed for complexity. Simple delegation operators are excluded.</li>
 *   <li><b>Data Classes:</b> Returns special value (-1.0) as WOC is not meaningful for
 *       data-centric classes by design.</li>
 *   <li><b>Expression Body:</b> Single-expression functions are analyzed contextually:
 *     <ul>
 *       <li>Trivial: {@code fun getX() = x}</li>
 *       <li>Functional: {@code fun calculate() = repository.findAll().map { transform(it) }}</li>
 *     </ul>
 *   </li>
 * </ul>
 *
 * <h3>Examples:</h3>
 * <pre>{@code
 * class UserService(private val repository: UserRepository) {
 *     // NON-FUNCTIONAL (1): simple property accessor
 *     fun getRepository() = repository
 *
 *     // NON-FUNCTIONAL (2): trivial delegation
 *     fun findById(id: Long) = repository.findById(id)
 *
 *     // FUNCTIONAL (1): contains business logic
 *     fun createUser(dto: UserDto): User {
 *         validate(dto)
 *         val user = User(dto.name, dto.email)
 *         return repository.save(user)
 *     }
 *
 *     // FUNCTIONAL (2): complex expression
 *     fun findActive() = repository.findAll()
 *         .filter { it.isActive }
 *         .map { it.toDto() }
 *
 *     // NON-FUNCTIONAL (3): operator delegation
 *     operator fun get(index: Int) = users[index]
 *
 *     // FUNCTIONAL (3): operator with logic
 *     operator fun plus(other: UserService) = UserService(
 *         CombinedRepository(this.repository, other.repository)
 *     )
 * }
 * // WOC = 3 functional / 6 total = 0.50
 * }</pre>
 *
 * <h3>Interpretation Guidelines:</h3>
 * <ul>
 *   <li><b>WOC &lt; 0.3:</b> Possible Data Class antipattern - consider refactoring to Kotlin data class</li>
 *   <li><b>0.3 ≤ WOC ≤ 0.7:</b> Balanced mix of data and behavior (typical for well-designed classes)</li>
 *   <li><b>WOC &gt; 0.7:</b> Behavior-heavy class - ensure single responsibility is maintained</li>
 *   <li><b>WOC = -1.0:</b> Data class marker (metric not applicable)</li>
 * </ul>
 *
 * @see <a href="https://dcm.dev/docs/metrics/class/weight-of-class/">WOC Metric Definition</a>
 */
public class KotlinWeightOfAClassVisitor extends KotlinClassVisitor {

    @Override
    public void visitClass(@NotNull KtClass klass) {
        // Special handling for data classes - WOC is not meaningful for them
        if (klass.isData()) {
            metric = Metric.of(WOC, -1.0); // marker value indicating data class
            return;
        }

        long total = 0;
        long functional = 0;
        KtClassBody body = klass.getBody();

        if (body != null) {
            for (KtDeclaration d : body.getDeclarations()) {
                if (d instanceof KtNamedFunction) {
                    KtNamedFunction f = (KtNamedFunction) d;
                    total++;
                    if (isFunctional(f)) {
                        functional++;
                    }
                }
            }
        }

        if (total == 0) {
            metric = Metric.of(WOC, 0.00);
        } else {
            metric = Metric.of(WOC, Value.of((double) functional).divide(Value.of((double) total)));
        }
    }

    /**
     * Determines if a method is functional (contains non-trivial logic).
     *
     * @param f the function to analyze
     * @return true if the function is considered functional, false for trivial/accessor methods
     */
    private boolean isFunctional(@NotNull KtNamedFunction f) {
        // Exclude property accessors (compiler-generated or explicit)
        if (isPropertyAccessor(f)) {
            return false;
        }

        // Operator overloading - context-dependent evaluation
        if (f.hasModifier(KtTokens.OPERATOR_KEYWORD)) {
            return !isTrivialOperator(f);
        }

        // Abstract or external methods have no implementation
        if (f.hasModifier(KtTokens.ABSTRACT_KEYWORD) || f.hasModifier(KtTokens.EXTERNAL_KEYWORD)) {
            return false;
        }

        // Expression body (single-expression functions)
        KtExpression expr = f.getBodyExpression();
        if (expr != null && f.getBodyBlockExpression() == null) {
            return isFunctionalExpression(expr, f);
        }

        // Block body analysis
        KtBlockExpression body = f.getBodyBlockExpression();
        if (body == null) {
            return false;
        }

        return analyzeFunctionalComplexity(body);
    }

    /**
     * Checks if a method is a property accessor (getter/setter).
     *
     * <p>Identifies both compiler-generated accessors and explicit accessor methods
     * that match property names in the class.</p>
     *
     * @param f the function to check
     * @return true if the method is a property accessor
     */
    private boolean isPropertyAccessor(@NotNull KtNamedFunction f) {
        // Check if method is inside a property declaration
        PsiElement parent = f.getParent();
        if (parent instanceof KtProperty) {
            return true;
        }

        // Check for synthetic getters/setters matching property names
        String name = f.getName();
        if (name != null) {
            List<KtParameter> params = f.getValueParameters();

            // Getter pattern: getXxx() with no parameters
            if (name.startsWith("get") && name.length() > 3 && params.isEmpty()) {
                String propName = Character.toLowerCase(name.charAt(3)) + name.substring(4);
                if (hasMatchingProperty(f, propName)) {
                    return true;
                }
            }

            // Setter pattern: setXxx(value) with one parameter
            if (name.startsWith("set") && name.length() > 3 && params.size() == 1) {
                String propName = Character.toLowerCase(name.charAt(3)) + name.substring(4);
                if (hasMatchingProperty(f, propName)) {
                    return true;
                }
            }

            // Boolean getter pattern: isXxx() with no parameters
            if (name.startsWith("is") && name.length() > 2 && params.isEmpty()) {
                String propName = Character.toLowerCase(name.charAt(2)) + name.substring(3);
                if (hasMatchingProperty(f, propName)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Checks if the class containing the function has a property matching the given name.
     *
     * @param f the function to check
     * @param propertyName the property name to look for
     * @return true if a matching property exists
     */
    private boolean hasMatchingProperty(@NotNull KtNamedFunction f, @NotNull String propertyName) {
        PsiElement parent = f.getParent();
        if (parent instanceof KtClassBody) {
            KtClassBody classBody = (KtClassBody) parent;
            for (KtDeclaration decl : classBody.getDeclarations()) {
                if (decl instanceof KtProperty) {
                    KtProperty prop = (KtProperty) decl;
                    if (propertyName.equals(prop.getName())) {
                        return true;
                    }
                }
            }
        }

        // Check constructor properties
        KtClass ktClass = (KtClass) parent.getParent();
        if (ktClass != null) {
            KtPrimaryConstructor constructor = ktClass.getPrimaryConstructor();
            if (constructor != null) {
                for (KtParameter param : constructor.getValueParameters()) {
                    if (param.hasValOrVar() && propertyName.equals(param.getName())) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Determines if an operator overloading function is trivial.
     *
     * <p>Examples of trivial operators:
     * <ul>
     *   <li>componentN() for destructuring declarations</li>
     *   <li>equals() with simple field comparison</li>
     *   <li>get/set operators that directly delegate to collection</li>
     * </ul>
     *
     * @param f the operator function to analyze
     * @return true if the operator is trivial (non-functional)
     */
    private boolean isTrivialOperator(@NotNull KtNamedFunction f) {
        String name = f.getName();
        if (name == null) {
            return false;
        }

        // Component operators for destructuring (componentN) - trivial
        if (name.matches("component\\d+")) {
            return true;
        }

        // Simple indexing operators
        if (("get".equals(name) || "set".equals(name)) && isSingleStatementDelegation(f)) {
            return true;
        }

        return false;
    }

    /**
     * Analyzes whether a single-expression function body is functional.
     *
     * <p>Expression bodies are functional unless they are:
     * <ul>
     *   <li>Simple property references: {@code fun getX() = x}</li>
     *   <li>Direct method delegations to fields: {@code fun find() = repository.find()}</li>
     * </ul>
     *
     * @param expr the expression to analyze
     * @param f the containing function
     * @return true if the expression represents functional logic
     */
    private boolean isFunctionalExpression(@NotNull KtExpression expr, @NotNull KtNamedFunction f) {
        // Simple property reference - non-functional
        if (expr instanceof KtNameReferenceExpression) {
            return false;
        }

        // Simple method call delegation - check if it's trivial
        if (expr instanceof KtCallExpression) {
            KtCallExpression call = (KtCallExpression) expr;
            // If it's a simple delegation with no additional logic, non-functional
            if (call.getValueArguments().isEmpty() || isTrivialDelegationCall(call)) {
                return false;
            }
            return true;
        }

        // Qualified expressions (a.b.c) - check complexity
        if (expr instanceof KtDotQualifiedExpression) {
            KtDotQualifiedExpression dotExpr = (KtDotQualifiedExpression) expr;
            // Single-level delegation is non-functional: field.method()
            // Multi-level or with operators is functional: field.method().map {...}
            return hasChainedCalls(dotExpr) || hasLambdaArguments(dotExpr);
        }

        // Complex expressions (when, if, lambdas, etc.) - functional
        return true;
    }

    /**
     * Analyzes the complexity of a block expression body.
     *
     * @param body the block expression to analyze
     * @return true if the block contains functional logic
     */
    private boolean analyzeFunctionalComplexity(@NotNull KtBlockExpression body) {
        KtExpression[] statements = body.getStatements().toArray(new KtExpression[0]);

        // Empty body - non-functional
        if (statements.length == 0) {
            return false;
        }

        // Multiple statements (> 2) - functional
        if (statements.length > 2) {
            return true;
        }

        // Single statement - analyze in detail
        if (statements.length == 1) {
            return analyzeSingleStatement(statements[0]);
        }

        // Two statements - check for validation + delegation pattern
        return !isValidationDelegationPattern(statements);
    }

    /**
     * Analyzes a single statement for functional complexity.
     *
     * @param statement the statement to analyze
     * @return true if the statement is functional
     */
    private boolean analyzeSingleStatement(@NotNull KtExpression statement) {
        // Return statement with simple reference - non-functional
        if (statement instanceof KtReturnExpression) {
            KtReturnExpression ret = (KtReturnExpression) statement;
            KtExpression returnValue = ret.getReturnedExpression();

            if (returnValue instanceof KtNameReferenceExpression) {
                return false; // return x
            }

            if (returnValue instanceof KtCallExpression) {
                KtCallExpression call = (KtCallExpression) returnValue;
                // Simple delegation call - non-functional
                if (isTrivialDelegationCall(call)) {
                    return false;
                }
            }

            return true;
        }

        // Simple call without assignment - check if trivial
        if (statement instanceof KtCallExpression) {
            return !isTrivialDelegationCall((KtCallExpression) statement);
        }

        // Binary expression (assignment) - check if simple field assignment
        if (statement instanceof KtBinaryExpression) {
            KtBinaryExpression be = (KtBinaryExpression) statement;
            if (be.getLeft() instanceof KtNameReferenceExpression &&
                    be.getRight() instanceof KtNameReferenceExpression) {
                return false; // x = y
            }
        }

        return true;
    }

    /**
     * Checks if a call expression is a trivial delegation (simple pass-through).
     *
     * @param call the call expression
     * @return true if it's a trivial delegation
     */
    private boolean isTrivialDelegationCall(@NotNull KtCallExpression call) {
        // No arguments and no lambda - likely trivial
        if (call.getValueArguments().isEmpty() && call.getLambdaArguments().isEmpty()) {
            return true;
        }
        return false;
    }

    /**
     * Checks if a method contains only a single statement delegation.
     *
     * @param f the function to check
     * @return true if it's a single-statement delegation
     */
    private boolean isSingleStatementDelegation(@NotNull KtNamedFunction f) {
        KtBlockExpression body = f.getBodyBlockExpression();
        if (body == null) {
            return false;
        }

        KtExpression[] statements = body.getStatements().toArray(new KtExpression[0]);
        return statements.length == 1;
    }

    /**
     * Checks if statements follow a validation + delegation pattern.
     *
     * <p>Example: validate(input); return repository.save(input)</p>
     *
     * @param statements the statements to check
     * @return true if it matches the pattern
     */
    private boolean isValidationDelegationPattern(@NotNull KtExpression[] statements) {
        if (statements.length != 2) {
            return false;
        }

        // First statement is a simple call (validation)
        if (!(statements[0] instanceof KtCallExpression)) {
            return false;
        }

        // Second statement is a return with simple delegation
        if (statements[1] instanceof KtReturnExpression) {
            KtReturnExpression ret = (KtReturnExpression) statements[1];
            KtExpression returnValue = ret.getReturnedExpression();
            return returnValue instanceof KtCallExpression;
        }

        return false;
    }

    /**
     * Checks if a dot-qualified expression has chained method calls.
     *
     * @param expr the expression to check
     * @return true if there are chained calls (a.b().c())
     */
    private boolean hasChainedCalls(@NotNull KtDotQualifiedExpression expr) {
        KtExpression selector = expr.getSelectorExpression();
        return selector instanceof KtCallExpression && expr.getReceiverExpression() instanceof KtDotQualifiedExpression;
    }

    /**
     * Checks if an expression uses lambda arguments (indicating functional complexity).
     *
     * @param expr the expression to check
     * @return true if lambda arguments are present
     */
    private boolean hasLambdaArguments(@NotNull KtDotQualifiedExpression expr) {
        KtExpression selector = expr.getSelectorExpression();
        if (selector instanceof KtCallExpression) {
            KtCallExpression call = (KtCallExpression) selector;
            return !call.getLambdaArguments().isEmpty();
        }
        return false;
    }
}
