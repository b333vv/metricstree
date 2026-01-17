/*
 * Kotlin Number Of Accessed Variables (NOAV) - Phase 2.3.3
 */
package org.b333vv.metric.model.visitor.kotlin.method;

import org.b333vv.metric.model.metric.Metric;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.psi.*;

import java.util.HashSet;
import java.util.Set;

import static org.b333vv.metric.model.metric.MetricType.NOAV;

/**
 * Calculates the Number of Accessed Variables (NOAV) metric for Kotlin methods.
 * 
 * <p>
 * NOAV measures the total number of distinct variables accessed within a method
 * body,
 * which serves as an indicator of method complexity and data coupling. Higher
 * NOAV values
 * suggest that a method interacts with many different data sources, potentially
 * indicating
 * poor cohesion or excessive complexity.
 * 
 * <h3>What is counted as an accessed variable:</h3>
 * <ul>
 * <li><b>Method parameters:</b> {@code fun process(x: Int, y: String)} - both x
 * and y are counted</li>
 * <li><b>Local variables:</b> Variables declared within the function body with
 * {@code val} or {@code var}</li>
 * <li><b>Class properties:</b> Properties declared in the class (both own class
 * and inherited)</li>
 * <li><b>Primary constructor properties:</b>
 * {@code class Person(val name: String)} - name is counted</li>
 * <li><b>Loop variables:</b> Iterator variables in {@code for} loops</li>
 * <li><b>Destructuring declarations:</b> {@code val (a, b) = pair} - both a and
 * b are counted</li>
 * <li><b>Lambda parameters:</b> {@code list.map { it + 1 }} - 'it' is
 * counted</li>
 * <li><b>Receiver variables:</b> {@code this}, {@code it}, extension function
 * receivers</li>
 * <li><b>When subject variable:</b> {@code when (val x = expr) { ... }} - x is
 * counted</li>
 * <li><b>Delegated properties:</b> {@code val lazy by lazy { ... }} - accessing
 * 'lazy' is counted</li>
 * <li><b>Backing fields:</b> {@code field} keyword in property
 * getters/setters</li>
 * <li><b>Qualified property access:</b> {@code obj.property} - property is
 * counted if obj is a known variable</li>
 * <li><b>Companion object properties:</b> Properties from companion objects of
 * the containing class</li>
 * </ul>
 * 
 * <h3>What is NOT counted:</h3>
 * <ul>
 * <li>Function calls: {@code myFunction()} - function names are excluded</li>
 * <li>Class names and type references: {@code String}, {@code MyClass}</li>
 * <li>Operators: {@code +}, {@code -}, {@code ==}, etc.</li>
 * <li>Keywords: {@code return}, {@code if}, {@code when}, etc.</li>
 * <li>Constants that are not variables (literals like {@code 42},
 * {@code "text"})</li>
 * <li>Enum constants (unless accessed as properties)</li>
 * </ul>
 * 
 * <h3>Examples:</h3>
 * 
 * <pre>
 * class Calculator {
 *     private var result: Int = 0
 *     
 *     // NOAV = 3 (x, y, result)
 *     fun add(x: Int, y: Int) {
 *         result = x + y
 *     }
 *     
 *     // NOAV = 5 (list, sum, it from forEach, item, result)
 *     fun sumList(list: List&lt;Int&gt;) {
 *         var sum = 0
 *         list.forEach { item -&gt;
 *             sum += item
 *         }
 *         result = sum
 *     }
 *     
 *     // NOAV = 4 (pair, a, b from destructuring, result)
 *     fun processPair(pair: Pair&lt;Int, Int&gt;) {
 *         val (a, b) = pair
 *         result = a + b
 *     }
 *     
 *     // NOAV = 3 (numbers, i from for loop, result)
 *     fun sumArray(numbers: IntArray) {
 *         for (i in numbers) {
 *             result += i
 *         }
 *     }
 * }
 * </pre>
 * 
 * <h3>Interpretation:</h3>
 * <ul>
 * <li><b>Low NOAV (1-5):</b> Simple method with few variable interactions -
 * generally good</li>
 * <li><b>Medium NOAV (6-10):</b> Moderate complexity - acceptable for most
 * methods</li>
 * <li><b>High NOAV (11+):</b> Complex method with many variable interactions -
 * consider refactoring
 * into smaller methods or reducing the number of variables</li>
 * </ul>
 * 
 * <h3>Metric significance:</h3>
 * <p>
 * NOAV helps identify methods that:
 * <ul>
 * <li>Have high data coupling (accessing many external variables)</li>
 * <li>Are doing too much (violating Single Responsibility Principle)</li>
 * <li>May be difficult to understand and maintain</li>
 * <li>Could benefit from decomposition into smaller methods</li>
 * </ul>
 * 
 * @see org.b333vv.metric.model.visitor.kotlin.method.KotlinMethodVisitor
 * @see org.b333vv.metric.model.metric.MetricType#NOAV
 */
public class KotlinNumberOfAccessedVariablesVisitor extends KotlinMethodVisitor {

    @Override
    public void visitNamedFunction(@NotNull KtNamedFunction function) {
        compute(function, function.getBodyExpression());
    }

    @Override
    public void visitPrimaryConstructor(@NotNull KtPrimaryConstructor constructor) {
        compute(constructor, null);
    }

    @Override
    public void visitSecondaryConstructor(@NotNull KtSecondaryConstructor constructor) {
        compute(constructor, constructor.getBodyExpression());
    }

    @Override
    public void visitAnonymousInitializer(@NotNull KtAnonymousInitializer initializer) {
        compute(initializer, initializer.getBody());
    }

    private void compute(@NotNull KtElement context, KtExpression body) {
        metric = Metric.of(NOAV, 0);
        if (body == null && !(context instanceof KtPrimaryConstructor))
            return;

        final Set<String> allowedNames = collectAllowedNames(context);
        final Set<String> accessedVariables = new HashSet<>();

        if (body != null) {
            body.accept(new KtTreeVisitorVoid() {
                @Override
                public void visitSimpleNameExpression(@NotNull KtSimpleNameExpression expression) {
                    // Skip operator references like '+', '+=' which are represented as simple names
                    // in PSI
                    if (expression instanceof KtOperationReferenceExpression) {
                        super.visitSimpleNameExpression(expression);
                        return;
                    }

                    // Skip function callees - we only want variable accesses
                    if (!isFunctionCallee(expression)) {
                        String name = expression.getReferencedName();
                        if (name != null && !name.isEmpty() && allowedNames.contains(name)) {
                            accessedVariables.add(name);
                        }
                    }
                    super.visitSimpleNameExpression(expression);
                }

                @Override
                public void visitDestructuringDeclaration(@NotNull KtDestructuringDeclaration declaration) {
                    // Track variables from destructuring: val (a, b) = pair
                    for (KtDestructuringDeclarationEntry entry : declaration.getEntries()) {
                        String name = entry.getName();
                        if (name != null) {
                            allowedNames.add(name);
                        }
                    }
                    super.visitDestructuringDeclaration(declaration);
                }

                @Override
                public void visitForExpression(@NotNull KtForExpression expression) {
                    // Track loop variable: for (item in list)
                    KtParameter loopParam = expression.getLoopParameter();
                    if (loopParam != null) {
                        String name = loopParam.getName();
                        if (name != null) {
                            allowedNames.add(name);
                        }

                        // Handle destructuring in for loops: for ((key, value) in map)
                        KtDestructuringDeclaration destructuring = loopParam.getDestructuringDeclaration();
                        if (destructuring != null) {
                            for (KtDestructuringDeclarationEntry entry : destructuring.getEntries()) {
                                String entryName = entry.getName();
                                if (entryName != null) {
                                    allowedNames.add(entryName);
                                }
                            }
                        }
                    }
                    super.visitForExpression(expression);
                }

                @Override
                public void visitLambdaExpression(@NotNull KtLambdaExpression expression) {
                    // Track lambda parameters (including 'it')
                    for (KtParameter param : expression.getValueParameters()) {
                        String name = param.getName();
                        if (name != null) {
                            allowedNames.add(name);
                        }
                    }
                    super.visitLambdaExpression(expression);
                }

                @Override
                public void visitWhenExpression(@NotNull KtWhenExpression expression) {
                    // Track when subject variable: when (val x = getX()) { ... }
                    KtProperty subjectVariable = expression.getSubjectVariable();
                    if (subjectVariable != null) {
                        String name = subjectVariable.getName();
                        if (name != null) {
                            allowedNames.add(name);
                        }
                    }
                    super.visitWhenExpression(expression);
                }

                @Override
                public void visitDotQualifiedExpression(@NotNull KtDotQualifiedExpression expression) {
                    // Track qualified property access: obj.property
                    // Only count if the receiver is a known variable
                    KtExpression receiver = expression.getReceiverExpression();
                    if (receiver instanceof KtSimpleNameExpression) {
                        String receiverName = ((KtSimpleNameExpression) receiver).getReferencedName();
                        if (receiverName != null && allowedNames.contains(receiverName)) {
                            accessedVariables.add(receiverName);
                        }
                    }

                    // Track the property itself if it's a selector
                    KtExpression selector = expression.getSelectorExpression();
                    if (selector instanceof KtSimpleNameExpression && !isFunctionCallee(selector)) {
                        String selectorName = ((KtSimpleNameExpression) selector).getReferencedName();
                        if (selectorName != null && allowedNames.contains(selectorName)) {
                            accessedVariables.add(selectorName);
                        }
                    }
                    super.visitDotQualifiedExpression(expression);
                }

                @Override
                public void visitThisExpression(@NotNull KtThisExpression expression) {
                    // Count 'this' as an accessed variable
                    accessedVariables.add("this");
                    super.visitThisExpression(expression);
                }

                /**
                 * Determines if a simple name expression is used as a function callee.
                 * 
                 * @param expr the expression to check
                 * @return true if the expression is a function being called
                 */
                private boolean isFunctionCallee(@NotNull KtExpression expr) {
                    if (!(expr instanceof KtSimpleNameExpression)) {
                        return false;
                    }

                    // Direct call: myFunction()
                    if (expr.getParent() instanceof KtCallExpression) {
                        KtExpression callee = ((KtCallExpression) expr.getParent()).getCalleeExpression();
                        return callee == expr;
                    }

                    // Qualified call: obj.myFunction()
                    if (expr.getParent() instanceof KtDotQualifiedExpression) {
                        KtDotQualifiedExpression dq = (KtDotQualifiedExpression) expr.getParent();
                        if (dq.getSelectorExpression() == expr && dq.getParent() instanceof KtCallExpression) {
                            return true;
                        }
                    }

                    return false;
                }
            });
        }

        // Add 'this' to allowed names for tracking
        allowedNames.add("this");

        metric = Metric.of(NOAV, accessedVariables.size());
    }

    /**
     * Collects all variable names that are accessible within the function scope.
     * This includes parameters, local variables, class properties, and special
     * variables.
     * 
     * @param function the function to analyze
     * @return set of all accessible variable names
     */
    private Set<String> collectAllowedNames(@NotNull KtElement context) {
        Set<String> allowed = new HashSet<>();

        // Add method/constructor parameters
        if (context instanceof KtNamedFunction) {
            for (KtParameter p : ((KtNamedFunction) context).getValueParameters()) {
                addParamToAllowed(p, allowed);
            }
        } else if (context instanceof KtConstructor) {
            for (Object o : ((KtConstructor<?>) context).getValueParameters()) {
                if (o instanceof KtParameter) {
                    addParamToAllowed((KtParameter) o, allowed);
                }
            }
        }

        // Add receiver parameter for extension functions: fun String.process()
        if (context instanceof KtNamedFunction) {
            if (((KtNamedFunction) context).getReceiverTypeReference() != null) {
                allowed.add("this"); // Extension receiver is accessed via 'this'
            }
        }

        // Add local properties and variables declared in
        // function/constructor/initializer body
        KtExpression body = null;
        if (context instanceof KtNamedFunction)
            body = ((KtNamedFunction) context).getBodyExpression();
        else if (context instanceof KtConstructor)
            body = ((KtConstructor) context).getBodyExpression();
        else if (context instanceof KtAnonymousInitializer)
            body = ((KtAnonymousInitializer) context).getBody();

        if (body != null) {
            body.accept(new KtTreeVisitorVoid() {
                @Override
                public void visitProperty(@NotNull KtProperty property) {
                    String name = property.getName();
                    if (name != null) {
                        allowed.add(name);
                    }
                    super.visitProperty(property);
                }
            });
        }

        // Add class properties (own class)
        KtClassOrObject owner = findOwnerClass(context);
        if (owner != null) {
            // Properties from class body
            for (KtDeclaration decl : owner.getDeclarations()) {
                if (decl instanceof KtProperty) {
                    String name = ((KtProperty) decl).getName();
                    if (name != null) {
                        allowed.add(name);
                    }
                }
            }

            // Properties from primary constructor
            if (owner instanceof KtClass) {
                KtPrimaryConstructor ctor = ((KtClass) owner).getPrimaryConstructor();
                if (ctor != null) {
                    for (KtParameter p : ctor.getValueParameters()) {
                        if (p.hasValOrVar()) {
                            String name = p.getName();
                            if (name != null) {
                                allowed.add(name);
                            }
                        }
                    }
                }
            }

            // Properties from companion object
            KtObjectDeclaration companion = getCompanionObject(owner);
            if (companion != null) {
                for (KtDeclaration decl : companion.getDeclarations()) {
                    if (decl instanceof KtProperty) {
                        String name = ((KtProperty) decl).getName();
                        if (name != null) {
                            allowed.add(name);
                        }
                    }
                }
            }
        }

        // Add special Kotlin variables
        allowed.add("it"); // Default lambda parameter
        allowed.add("this"); // Receiver reference
        allowed.add("field"); // Backing field in property accessors

        return allowed;
    }

    /**
     * Finds the companion object of a class or object.
     * 
     * @param owner the class or object to search in
     * @return the companion object, or null if not found
     */
    private KtObjectDeclaration getCompanionObject(KtClassOrObject owner) {
        if (!(owner instanceof KtClass)) {
            return null;
        }

        for (KtDeclaration decl : owner.getDeclarations()) {
            if (decl instanceof KtObjectDeclaration) {
                KtObjectDeclaration obj = (KtObjectDeclaration) decl;
                if (obj.isCompanion()) {
                    return obj;
                }
            }
        }
        return null;
    }

    private void addParamToAllowed(KtParameter p, Set<String> allowed) {
        String name = p.getName();
        if (name != null) {
            allowed.add(name);
        }

        // Handle destructuring parameters: fun process((x, y): Pair<Int, Int>)
        KtDestructuringDeclaration destructuring = p.getDestructuringDeclaration();
        if (destructuring != null) {
            for (KtDestructuringDeclarationEntry entry : destructuring.getEntries()) {
                String entryName = entry.getName();
                if (entryName != null) {
                    allowed.add(entryName);
                }
            }
        }
    }

    /**
     * Finds the containing class or object for the given element.
     * Walks up the PSI tree until it finds a KtClassOrObject.
     * 
     * @param element the element to start from
     * @return the containing class or null if not found
     */
    private KtClassOrObject findOwnerClass(@NotNull KtElement element) {
        com.intellij.psi.PsiElement current = element;

        while (current != null) {
            if (current instanceof KtClassOrObject) {
                return (KtClassOrObject) current;
            }
            current = current.getParent();

            // Stop if we exit Kotlin PSI hierarchy
            if (current != null && !(current instanceof KtElement)) {
                break;
            }
        }

        return null;
    }
}
