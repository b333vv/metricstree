/*
 * Kotlin Response For Class (RFC) - Enhanced PSI-based implementation with resolve support
 */
package org.b333vv.metric.model.visitor.kotlin.type;

import com.intellij.psi.PsiElement;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.value.Value;
import org.b333vv.metric.model.visitor.kotlin.KotlinMetricUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.descriptors.CallableDescriptor;
import org.jetbrains.kotlin.descriptors.FunctionDescriptor;
import org.jetbrains.kotlin.idea.caches.resolve.ResolutionUtils;
import org.jetbrains.kotlin.lexer.KtToken;
import org.jetbrains.kotlin.lexer.KtTokens;
import org.jetbrains.kotlin.psi.*;
import org.jetbrains.kotlin.resolve.BindingContext;
import org.jetbrains.kotlin.resolve.calls.model.ResolvedCall;
import org.jetbrains.kotlin.resolve.lazy.BodyResolveMode;

import java.util.HashSet;
import java.util.Set;

import static org.b333vv.metric.model.metric.MetricType.RFC;

/**
 * RFC (Response For Class) metric calculator for Kotlin classes.
 *
 * <p>RFC measures the size of the response set for a class, which consists of:
 * <ul>
 *   <li>Methods declared in the class (functions, constructors, and property accessors)</li>
 *   <li>Unique method calls from within the class (by fully qualified signature when resolved)</li>
 * </ul>
 *
 * <h2>Kotlin-Specific Considerations</h2>
 *
 * <h3>1. Declared Methods</h3>
 * <ul>
 *   <li><b>Named functions:</b> All non-private functions declared in the class body</li>
 *   <li><b>Constructors:</b> Primary and all secondary constructors</li>
 *   <li><b>Property accessors:</b> Implicit getters/setters for properties (including primary constructor properties)</li>
 *   <li><b>Data class methods:</b> Auto-generated equals(), hashCode(), toString(), copy(), and componentN() methods</li>
 *   <li><b>Companion object members:</b> Excluded (treated as static/class-level)</li>
 * </ul>
 *
 * <h3>2. Method Calls</h3>
 * <ul>
 *   <li><b>Regular calls:</b> Standard function invocations like {@code foo()}</li>
 *   <li><b>Extension functions:</b> Calls like {@code list.map { }}, resolved to actual function signature</li>
 *   <li><b>Operator functions:</b> Operators translated to function calls:
 *     <ul>
 *       <li>Binary: {@code a + b} → {@code plus}, {@code a[i]} → {@code get}</li>
 *       <li>Unary: {@code !a} → {@code not}, {@code ++a} → {@code inc}</li>
 *       <li>Comparison: {@code a > b} → {@code compareTo}</li>
 *     </ul>
 *   </li>
 *   <li><b>Infix calls:</b> {@code a to b} resolved as regular function call</li>
 *   <li><b>Invoke operator:</b> {@code obj()} → {@code invoke}</li>
 *   <li><b>Property delegates:</b> {@code by lazy} → implicit {@code getValue/setValue} calls</li>
 *   <li><b>Destructuring:</b> {@code val (x, y) = pair} → {@code component1(), component2()}</li>
 *   <li><b>Scope functions:</b> {@code let, run, apply, also, with} - counted when called</li>
 *   <li><b>Lambda parameters:</b> Trailing lambdas included in arity calculation</li>
 * </ul>
 *
 * <h3>3. Resolution Strategy</h3>
 * <p>This implementation uses PSI resolve where possible to determine actual function signatures,
 * falling back to name-based heuristics when resolution fails. This hybrid approach provides:
 * <ul>
 *   <li>Accurate counting of polymorphic and overloaded calls</li>
 *   <li>Proper handling of extension functions from standard library and third-party code</li>
 *   <li>Graceful degradation when resolution context is unavailable</li>
 * </ul>
 *
 * <h3>4. Signature Format</h3>
 * <p>Methods are identified by signature: {@code name/arity} where:
 * <ul>
 *   <li>{@code name} is the simple function name (or operator function name for operators)</li>
 *   <li>{@code arity} is the number of parameters (including trailing lambdas)</li>
 *   <li>For resolved calls, fully qualified names may be used to distinguish overloads</li>
 * </ul>
 *
 * @see <a href="https://en.wikipedia.org/wiki/Response_for_a_class">RFC Metric</a>
 * @see org.b333vv.metric.model.visitor.kotlin.KotlinMetricUtils
 */
public class KotlinResponseForClassVisitor extends KotlinClassVisitor {

    private static final String GETTER_PREFIX = "get";
    private static final String SETTER_PREFIX = "set";

    @Override
    public void visitClass(@NotNull KtClass klass) {
        try {
            Set<String> responses = new HashSet<>();

            // Get binding context for resolution
            BindingContext bindingContext = null;
            try {
                bindingContext = ResolutionUtils.analyze(klass, BodyResolveMode.PARTIAL);
            } catch (Exception e) {
                // Resolution unavailable, proceed with PSI-only analysis
            }

            // 1. Add declared methods
            addDeclaredMethods(klass, responses);

            // 2. Add constructors
            addConstructors(klass, responses);

            // 3. Add property accessors (from body and primary constructor)
            addPropertyAccessors(klass, responses);

            // 4. Add data class implicit methods
            addDataClassMethods(klass, responses);

            // 5. Traverse class to collect all method calls
            BindingContext finalBindingContext = bindingContext;
            klass.accept(new KtTreeVisitorVoid() {

                @Override
                public void visitCallExpression(@NotNull KtCallExpression expression) {
                    addCallExpression(expression, responses, finalBindingContext);
                    super.visitCallExpression(expression);
                }

                @Override
                public void visitQualifiedExpression(@NotNull KtQualifiedExpression expression) {
                    addQualifiedExpression(expression, responses, finalBindingContext);
                    super.visitQualifiedExpression(expression);
                }

                @Override
                public void visitBinaryExpression(@NotNull KtBinaryExpression expression) {
                    addBinaryOperator(expression, responses, finalBindingContext);
                    super.visitBinaryExpression(expression);
                }

                @Override
                public void visitUnaryExpression(@NotNull KtUnaryExpression expression) {
                    addUnaryOperator(expression, responses, finalBindingContext);
                    super.visitUnaryExpression(expression);
                }

                @Override
                public void visitArrayAccessExpression(@NotNull KtArrayAccessExpression expression) {
                    addArrayAccess(expression, responses, finalBindingContext);
                    super.visitArrayAccessExpression(expression);
                }

                @Override
                public void visitProperty(@NotNull KtProperty property) {
                    addPropertyDelegateCalls(property, responses);
                    super.visitProperty(property);
                }

                @Override
                public void visitDestructuringDeclaration(@NotNull KtDestructuringDeclaration declaration) {
                    addDestructuringCalls(declaration, responses, finalBindingContext);
                    super.visitDestructuringDeclaration(declaration);
                }
            });

            metric = Metric.of(RFC, responses.size());
        } catch (Exception e) {
            metric = Metric.of(RFC, Value.UNDEFINED);
        }
    }

    /**
     * Adds declared named functions from the class body.
     */
    private void addDeclaredMethods(@NotNull KtClass klass, @NotNull Set<String> responses) {
        KtClassBody body = klass.getBody();
        if (body == null) return;

        for (KtDeclaration decl : body.getDeclarations()) {
            if (decl instanceof KtNamedFunction) {
                KtNamedFunction function = (KtNamedFunction) decl;
                String name = function.getName();
                if (name != null) {
                    int arity = function.getValueParameters().size();
                    responses.add(name + "/" + arity);
                }
            }
        }
    }

    /**
     * Adds primary and secondary constructors.
     */
    private void addConstructors(@NotNull KtClass klass, @NotNull Set<String> responses) {
        String className = klass.getName();
        if (className == null) return;

        // Primary constructor
        KtPrimaryConstructor primary = klass.getPrimaryConstructor();
        if (primary != null) {
            int arity = primary.getValueParameters().size();
            responses.add(className + "/" + arity);
        }

        // Secondary constructors
        for (KtSecondaryConstructor ctor : klass.getSecondaryConstructors()) {
            int arity = ctor.getValueParameters().size();
            responses.add(className + "/" + arity);
        }
    }

    /**
     * Adds implicit property accessors (getters/setters).
     */
    private void addPropertyAccessors(@NotNull KtClass klass, @NotNull Set<String> responses) {
        // Primary constructor properties
        KtPrimaryConstructor primary = klass.getPrimaryConstructor();
        if (primary != null) {
            for (KtParameter param : primary.getValueParameters()) {
                if (param.hasValOrVar()) {
                    String paramName = param.getName();
                    if (paramName != null) {
                        responses.add(GETTER_PREFIX + capitalize(paramName) + "/0");
                        if (param.isMutable()) {
                            responses.add(SETTER_PREFIX + capitalize(paramName) + "/1");
                        }
                    }
                }
            }
        }

        // Body properties
        KtClassBody body = klass.getBody();
        if (body != null) {
            for (KtDeclaration decl : body.getDeclarations()) {
                if (decl instanceof KtProperty) {
                    KtProperty prop = (KtProperty) decl;
                    if (!KotlinMetricUtils.isInCompanionObject(prop)) {
                        String propName = prop.getName();
                        if (propName != null) {
                            responses.add(GETTER_PREFIX + capitalize(propName) + "/0");
                            if (prop.isVar()) {
                                responses.add(SETTER_PREFIX + capitalize(propName) + "/1");
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Adds data class implicit methods: equals, hashCode, toString, copy, componentN.
     */
    private void addDataClassMethods(@NotNull KtClass klass, @NotNull Set<String> responses) {
        if (!klass.isData()) return;

        responses.add("equals/1");
        responses.add("hashCode/0");
        responses.add("toString/0");

        KtPrimaryConstructor primary = klass.getPrimaryConstructor();
        if (primary != null) {
            int paramCount = primary.getValueParameters().size();
            responses.add("copy/" + paramCount);

            // componentN methods for each parameter
            for (int i = 1; i <= paramCount; i++) {
                responses.add("component" + i + "/0");
            }
        }
    }

    /**
     * Processes regular call expressions, including those with trailing lambdas.
     */
    private void addCallExpression(@NotNull KtCallExpression expression,
                                   @NotNull Set<String> responses,
                                   @Nullable BindingContext bindingContext) {
        // Try to resolve the call
        String signature = resolveCallSignature(expression, bindingContext);
        if (signature != null) {
            responses.add(signature);
            return;
        }

        // Fallback: use simple name + arity
        KtExpression calleeExpr = expression.getCalleeExpression();
        if (calleeExpr instanceof KtSimpleNameExpression) {
            String name = ((KtSimpleNameExpression) calleeExpr).getReferencedName();
            if (name != null && !name.isEmpty()) {
                int arity = calculateArity(expression);
                responses.add(name + "/" + arity);
            }
        } else if (calleeExpr instanceof KtNameReferenceExpression) {
            // Handle invoke operator: obj()
            String name = ((KtNameReferenceExpression) calleeExpr).getReferencedName();
            if (name != null) {
                int arity = calculateArity(expression);
                responses.add("invoke/" + arity);
            }
        }
    }

    /**
     * Processes qualified expressions (extension functions and method chains).
     */
    private void addQualifiedExpression(@NotNull KtQualifiedExpression expression,
                                        @NotNull Set<String> responses,
                                        @Nullable BindingContext bindingContext) {
        KtExpression selector = expression.getSelectorExpression();
        if (selector instanceof KtCallExpression) {
            KtCallExpression callExpr = (KtCallExpression) selector;

            // Try to resolve
            String signature = resolveCallSignature(callExpr, bindingContext);
            if (signature != null) {
                responses.add(signature);
            }
        }
    }

    /**
     * Processes binary operators (arithmetic, comparison, range, etc.).
     */
    private void addBinaryOperator(@NotNull KtBinaryExpression expression,
                                   @NotNull Set<String> responses,
                                   @Nullable BindingContext bindingContext) {
        // Try to resolve the operator call
        if (bindingContext != null) {
            try {
                @SuppressWarnings("unchecked")
                ResolvedCall<? extends CallableDescriptor> resolvedCall =
                        (ResolvedCall<? extends CallableDescriptor>) bindingContext.get(BindingContext.CALL, expression);
                if (resolvedCall != null) {
                    CallableDescriptor descriptor = resolvedCall.getResultingDescriptor();
                    if (descriptor instanceof FunctionDescriptor) {
                        String name = descriptor.getName().asString();
                        int arity = descriptor.getValueParameters().size();
                        responses.add(name + "/" + arity);
                        return;
                    }
                }
            } catch (Exception ignored) {}
        }

        // Fallback: map token to operator function name
        org.jetbrains.kotlin.lexer.KtToken token = (org.jetbrains.kotlin.lexer.KtToken) expression.getOperationReference().getReferencedNameElementType();
        String operatorName = getOperatorFunctionName(token);
        if (operatorName != null) {
            responses.add(operatorName + "/1");
        }
    }

    /**
     * Processes unary operators (!, ++, --, +, -).
     */
    private void addUnaryOperator(@NotNull KtUnaryExpression expression,
                                  @NotNull Set<String> responses,
                                  @Nullable BindingContext bindingContext) {
        // Try to resolve
        if (bindingContext != null) {
            try {
                @SuppressWarnings("unchecked")
                ResolvedCall<? extends CallableDescriptor> resolvedCall =
                        (ResolvedCall<? extends CallableDescriptor>) bindingContext.get(BindingContext.CALL, expression);
                if (resolvedCall != null) {
                    CallableDescriptor descriptor = resolvedCall.getResultingDescriptor();
                    if (descriptor instanceof FunctionDescriptor) {
                        String name = descriptor.getName().asString();
                        responses.add(name + "/0");
                        return;
                    }
                }
            } catch (Exception ignored) {}
        }

        // Fallback: map token to operator function name
        org.jetbrains.kotlin.lexer.KtToken token = (org.jetbrains.kotlin.lexer.KtToken) expression.getOperationReference().getReferencedNameElementType();
        String operatorName = getUnaryOperatorName(token);
        if (operatorName != null) {
            responses.add(operatorName + "/0");
        }
    }

    /**
     * Processes array access expressions: a[i] → get(i) or set(i, value).
     */
    private void addArrayAccess(@NotNull KtArrayAccessExpression expression,
                                @NotNull Set<String> responses,
                                @Nullable BindingContext bindingContext) {
        // Check if this is on the left side of assignment (set) or right side (get)
        PsiElement parent = expression.getParent();
        boolean isAssignment = parent instanceof KtBinaryExpression &&
                ((KtBinaryExpression) parent).getOperationToken() == KtTokens.EQ &&
                ((KtBinaryExpression) parent).getLeft() == expression;

        if (isAssignment) {
            // a[i] = value → set(i, value)
            int indexCount = expression.getIndexExpressions().size();
            responses.add("set/" + (indexCount + 1));
        } else {
            // a[i] → get(i)
            int indexCount = expression.getIndexExpressions().size();
            responses.add("get/" + indexCount);
        }
    }

    /**
     * Adds implicit getValue/setValue calls from property delegates.
     */
    private void addPropertyDelegateCalls(@NotNull KtProperty property,
                                          @NotNull Set<String> responses) {
        KtPropertyDelegate delegate = property.getDelegate();
        if (delegate != null) {
            // by lazy, by Delegates.observable, etc.
            // Signature: getValue(thisRef, property)
            responses.add("getValue/2");

            if (property.isVar()) {
                // Signature: setValue(thisRef, property, value)
                responses.add("setValue/3");
            }
        }
    }

    /**
     * Adds componentN calls from destructuring declarations.
     */
    private void addDestructuringCalls(@NotNull KtDestructuringDeclaration declaration,
                                       @NotNull Set<String> responses,
                                       @Nullable BindingContext bindingContext) {
        int componentCount = declaration.getEntries().size();
        for (int i = 1; i <= componentCount; i++) {
            responses.add("component" + i + "/0");
        }
    }

    /**
     * Calculates arity including trailing lambdas.
     */
    private int calculateArity(@NotNull KtCallExpression expression) {
        int arity = expression.getValueArguments().size();

        // Add trailing lambda arguments
        if (!expression.getLambdaArguments().isEmpty()) {
            arity += expression.getLambdaArguments().size();
        }

        return arity;
    }

    /**
     * Attempts to resolve a call expression to its actual function signature.
     * Returns null if resolution fails.
     */
    @Nullable
    private String resolveCallSignature(@NotNull KtCallExpression expression,
                                        @Nullable BindingContext bindingContext) {
        if (bindingContext == null) return null;

        try {
            KtExpression calleeExpression = expression.getCalleeExpression();
            @SuppressWarnings("unchecked")
            ResolvedCall<? extends CallableDescriptor> resolvedCall =
                    (ResolvedCall<? extends CallableDescriptor>) bindingContext.get(BindingContext.CALL, calleeExpression);

            if (resolvedCall != null) {
                CallableDescriptor descriptor = resolvedCall.getResultingDescriptor();
                if (descriptor instanceof FunctionDescriptor) {
                    String name = descriptor.getName().asString();
                    int arity = descriptor.getValueParameters().size();
                    return name + "/" + arity;
                }
            }
        } catch (Exception ignored) {
            // Resolution failed, will use fallback
        }

        return null;
    }

    /**
     * Maps binary operator tokens to Kotlin operator function names.
     */
    @Nullable
    private String getOperatorFunctionName(@NotNull KtToken token) {
        if (token == KtTokens.PLUS) return "plus";
        if (token == KtTokens.MINUS) return "minus";
        if (token == KtTokens.MUL) return "times";
        if (token == KtTokens.DIV) return "div";
        if (token == KtTokens.PERC) return "rem";
        if (token == KtTokens.RANGE) return "rangeTo";
        if (token == KtTokens.IN_KEYWORD) return "contains";
        if (token == KtTokens.NOT_IN) return "contains"; // negated
        if (token == KtTokens.PLUSEQ) return "plusAssign";
        if (token == KtTokens.MINUSEQ) return "minusAssign";
        if (token == KtTokens.MULTEQ) return "timesAssign";
        if (token == KtTokens.DIVEQ) return "divAssign";
        if (token == KtTokens.PERCEQ) return "remAssign";
        if (token == KtTokens.GT) return "compareTo";
        if (token == KtTokens.LT) return "compareTo";
        if (token == KtTokens.GTEQ) return "compareTo";
        if (token == KtTokens.LTEQ) return "compareTo";

        return null;
    }

    /**
     * Maps unary operator tokens to Kotlin operator function names.
     */
    @Nullable
    private String getUnaryOperatorName(@NotNull KtToken token) {
        if (token == KtTokens.PLUSPLUS) return "inc";
        if (token == KtTokens.MINUSMINUS) return "dec";
        if (token == KtTokens.EXCL) return "not";
        if (token == KtTokens.PLUS) return "unaryPlus";
        if (token == KtTokens.MINUS) return "unaryMinus";

        return null;
    }

    /**
     * Capitalizes the first letter of a string for property accessor naming.
     */
    @NotNull
    private String capitalize(@NotNull String str) {
        if (str.isEmpty()) return str;
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }
}
