/*
 * Kotlin Locality Of Attribute Accesses (LAA) - Phase 2.3.3
 */
package org.b333vv.metric.model.visitor.kotlin.method;

import org.b333vv.metric.model.metric.Metric;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMember;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.psi.*;

import java.util.HashSet;
import java.util.Set;

import static org.b333vv.metric.model.metric.MetricType.LAA;

/**
 * Calculates the Locality of Attribute Accesses (LAA) metric for Kotlin methods
 * and constructors.
 * 
 * <p>
 * LAA measures the ratio of accesses to the class's own properties (attributes)
 * versus all property accesses
 * in a method. This metric helps identify methods that heavily depend on
 * external data, which may indicate
 * poor encapsulation or high coupling.
 * 
 * <p>
 * <b>Formula:</b> LAA = (Number of own property accesses) / (Total number of
 * property accesses)
 * 
 * <p>
 * The metric value ranges from 0.0 to 1.0:
 * <ul>
 * <li>1.0 - all property accesses are to own properties (high locality)</li>
 * <li>0.0 - all property accesses are to foreign properties (low locality, high
 * coupling)</li>
 * <li>0.0 - also returned when there are no property accesses at all</li>
 * </ul>
 * 
 * <h3>What counts as an "own property access":</h3>
 * <ul>
 * <li>Direct access to properties declared in the same class:
 * {@code propertyName}</li>
 * <li>Explicit access via {@code this}: {@code this.propertyName}</li>
 * <li>Access to properties from primary constructor:
 * {@code class Person(val name: String)}</li>
 * <li>Access to inherited properties from superclasses</li>
 * <li>Access to companion object properties of the same class</li>
 * <li>Access to backing fields using {@code field} keyword inside property
 * accessors</li>
 * </ul>
 * 
 * <h3>What counts as a "foreign property access":</h3>
 * <ul>
 * <li>Qualified property access on other objects: {@code obj.property}</li>
 * <li>Safe qualified access: {@code obj?.property}</li>
 * <li>Properties from other classes, even if accessed through parameters</li>
 * <li>Static properties from other classes</li>
 * </ul>
 * 
 * <h3>What is NOT counted:</h3>
 * <ul>
 * <li>Function calls: {@code obj.method()} - only property accesses count</li>
 * <li>Local variables and parameters - only class properties/fields</li>
 * <li>Package-level functions</li>
 * </ul>
 * 
 * <h3>Examples:</h3>
 * 
 * <pre>
 * class Person(val name: String) {
 *     private var age: Int = 0
 *     
 *     // LAA = 1.0 (2 own / 2 total)
 *     fun getInfo(): String {
 *         return "$name is $age years old"  // name and age are own properties
 *     }
 *     
 *     // LAA = 0.5 (1 own / 2 total)
 *     fun compareTo(other: Person): Boolean {
 *         return this.age > other.age  // this.age is own, other.age is foreign
 *     }
 *     
 *     // LAA = 0.0 (0 own / 1 total)
 *     fun printOtherName(other: Person) {
 *         println(other.name)  // accessing foreign property
 *     }
 *     
 *     // LAA = 0.0 (no property accesses at all)
 *     fun calculate(x: Int, y: Int): Int {
 *         return x + y  // only parameters, no property accesses
 *     }
 * }
 * </pre>
 * 
 * <h3>Interpretation:</h3>
 * <ul>
 * <li><b>High LAA (close to 1.0):</b> Method primarily works with its own data
 * - good encapsulation</li>
 * <li><b>Medium LAA (around 0.5):</b> Method balances own and foreign data
 * access</li>
 * <li><b>Low LAA (close to 0.0):</b> Method heavily depends on external data -
 * potential code smell,
 * consider refactoring or moving the method closer to the data it uses</li>
 * </ul>
 * 
 * @see org.b333vv.metric.model.visitor.kotlin.method.KotlinMethodVisitor
 * @see org.b333vv.metric.model.metric.MetricType#LAA
 */
public class KotlinLocalityOfAttributeAccessesVisitor extends KotlinMethodVisitor {

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
        metric = Metric.of(LAA, 0.0);
    }

    @Override
    public void visitAnonymousInitializer(@NotNull KtAnonymousInitializer initializer) {
        compute(initializer, initializer.getBody());
    }

    /**
     * Computes the LAA metric for the given method context and body.
     * 
     * @param context the method or constructor element
     * @param body    the body expression to analyze
     */
    private void compute(@NotNull KtElement context, KtExpression body) {
        Set<String> ownPropertyNames = collectOwnPropertyNames(context);
        KtClassOrObject owner = findOwnerClass(context);

        if (body == null) {
            metric = Metric.of(LAA, 0.0);
            return;
        }

        final int[] own = { 0 };
        final int[] total = { 0 };

        body.accept(new KtTreeVisitorVoid() {
            @Override
            public void visitDotQualifiedExpression(@NotNull KtDotQualifiedExpression expression) {
                processQualifiedExpression(expression.getReceiverExpression(),
                        expression.getSelectorExpression(), owner, ownPropertyNames, own, total);
                super.visitDotQualifiedExpression(expression);
            }

            @Override
            public void visitSafeQualifiedExpression(@NotNull KtSafeQualifiedExpression expression) {
                processQualifiedExpression(expression.getReceiverExpression(),
                        expression.getSelectorExpression(), owner, ownPropertyNames, own, total);
                super.visitSafeQualifiedExpression(expression);
            }

            @Override
            public void visitSimpleNameExpression(@NotNull KtSimpleNameExpression expression) {
                // Skip if this is a selector of a qualified expression (already handled above)
                if (isPartOfQualifiedExpression(expression)) {
                    super.visitSimpleNameExpression(expression);
                    return;
                }

                // Check if this is a reference to the 'field' keyword (backing field)
                String name = expression.getReferencedName();
                if ("field".equals(name)) {
                    // Backing field access is always an own property access
                    own[0] += 1;
                    total[0] += 1;
                    super.visitSimpleNameExpression(expression);
                    return;
                }

                // Check if this is an unqualified reference to an own property
                if (ownPropertyNames.contains(name)) {
                    if (isOwnPropertyReference(expression, owner)) {
                        own[0] += 1;
                        total[0] += 1;
                    }
                }
                super.visitSimpleNameExpression(expression);
            }
        });

        if (total[0] == 0) {
            metric = Metric.of(LAA, 0.0);
        } else {
            metric = Metric.of(LAA, ((double) own[0]) / ((double) total[0]));
        }
    }

    /**
     * Processes a qualified expression (both dot and safe call operators).
     * 
     * @param receiver         the receiver expression (left side of the
     *                         dot/safe-call)
     * @param selector         the selector expression (right side of the
     *                         dot/safe-call)
     * @param owner            the owner class
     * @param ownPropertyNames set of own property names
     * @param own              counter for own property accesses
     * @param total            counter for total property accesses
     */
    private void processQualifiedExpression(KtExpression receiver, KtExpression selector,
            KtClassOrObject owner, Set<String> ownPropertyNames,
            int[] own, int[] total) {
        // Only count property accesses, not method calls
        if (!(selector instanceof KtSimpleNameExpression)) {
            return;
        }

        total[0] += 1;

        // Check if receiver is 'this' or 'super' - these are own property accesses
        if (receiver instanceof KtThisExpression || receiver instanceof KtSuperExpression) {
            own[0] += 1;
            return;
        }

        // Try to determine if the selector refers to an own property
        if (isOwnPropertyReference((KtSimpleNameExpression) selector, owner)) {
            own[0] += 1;
        }
    }

    /**
     * Checks if a simple name expression is part of a qualified expression.
     * 
     * @param expression the expression to check
     * @return true if it's a selector in a qualified expression
     */
    private boolean isPartOfQualifiedExpression(@NotNull KtSimpleNameExpression expression) {
        PsiElement parent = expression.getParent();
        if (parent instanceof KtDotQualifiedExpression) {
            return ((KtDotQualifiedExpression) parent).getSelectorExpression() == expression;
        }
        if (parent instanceof KtSafeQualifiedExpression) {
            return ((KtSafeQualifiedExpression) parent).getSelectorExpression() == expression;
        }
        return false;
    }

    /**
     * Determines if a simple name expression refers to an own property.
     * 
     * @param expression the expression to check
     * @param owner      the owner class
     * @return true if the expression refers to an own property
     */
    private boolean isOwnPropertyReference(@NotNull KtSimpleNameExpression expression, KtClassOrObject owner) {
        if (owner == null) {
            return false;
        }

        for (var ref : expression.getReferences()) {
            PsiElement resolved = ref.resolve();

            // Handle Java interop - PsiField from compiled Java classes
            if (resolved instanceof PsiField) {
                PsiField field = (PsiField) resolved;
                if (field.getContainingClass() != null) {
                    String fieldClassName = field.getContainingClass().getQualifiedName();
                    if (matchesOwnerOrSuperclass(owner, fieldClassName)) {
                        return true;
                    }
                }
            }

            // Handle Kotlin properties
            if (resolved instanceof KtProperty) {
                KtClassOrObject declOwner = findOwnerClass((KtProperty) resolved);
                if (declOwner != null && isSameOrSuperclass(owner, declOwner)) {
                    return true;
                }
            }

            // Handle primary constructor parameters with val/var
            if (resolved instanceof KtParameter) {
                KtParameter param = (KtParameter) resolved;
                if (param.hasValOrVar()) {
                    PsiElement paramParent = param.getParent();
                    if (paramParent instanceof KtParameterList) {
                        PsiElement ctorParent = paramParent.getParent();
                        if (ctorParent instanceof KtPrimaryConstructor) {
                            KtClassOrObject paramClass = ((KtPrimaryConstructor) ctorParent)
                                    .getContainingClassOrObject();
                            if (paramClass != null && isSameOrSuperclass(owner, paramClass)) {
                                return true;
                            }
                        }
                    }
                }
            }
        }

        return false;
    }

    /**
     * Checks if two classes are the same or if declOwner is a superclass of owner.
     * 
     * @param owner     the owner class
     * @param declOwner the declaring class
     * @return true if they are the same or declOwner is a superclass
     */
    private boolean isSameOrSuperclass(KtClassOrObject owner, KtClassOrObject declOwner) {
        if (owner.getFqName() == null || declOwner.getFqName() == null) {
            return false;
        }

        String ownerFqn = owner.getFqName().asString();
        String declFqn = declOwner.getFqName().asString();

        // Direct match
        if (ownerFqn.equals(declFqn)) {
            return true;
        }

        // Check superclasses
        if (owner instanceof KtClass) {
            for (var superTypeEntry : ((KtClass) owner).getSuperTypeListEntries()) {
                KtTypeReference typeRef = superTypeEntry.getTypeReference();
                if (typeRef != null) {
                    String superTypeName = typeRef.getText();
                    // Simple name comparison (could be improved with full resolution)
                    if (declFqn.endsWith("." + superTypeName) || declFqn.equals(superTypeName)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Checks if a qualified class name matches the owner class or its superclasses.
     * 
     * @param owner         the owner class
     * @param qualifiedName the qualified class name to check
     * @return true if they match
     */
    private boolean matchesOwnerOrSuperclass(KtClassOrObject owner, String qualifiedName) {
        if (qualifiedName == null || owner.getFqName() == null) {
            return false;
        }
        return qualifiedName.equals(owner.getFqName().asString());
    }

    /**
     * Collects the names of all properties belonging to the owner class.
     * This includes properties declared in the class body and val/var parameters
     * from the primary constructor.
     * 
     * @param element the element to start from
     * @return set of own property names
     */
    private Set<String> collectOwnPropertyNames(@NotNull KtElement element) {
        Set<String> names = new HashSet<>();
        KtClassOrObject owner = findOwnerClass(element);

        if (owner != null) {
            // Collect properties from class body
            for (KtDeclaration decl : owner.getDeclarations()) {
                if (decl instanceof KtProperty) {
                    String name = ((KtProperty) decl).getName();
                    if (name != null) {
                        names.add(name);
                    }
                }
            }

            // Collect properties from primary constructor
            if (owner instanceof KtClass) {
                KtPrimaryConstructor ctor = ((KtClass) owner).getPrimaryConstructor();
                if (ctor != null) {
                    for (KtParameter param : ctor.getValueParameters()) {
                        if (param.hasValOrVar()) {
                            String name = param.getName();
                            if (name != null) {
                                names.add(name);
                            }
                        }
                    }
                }
            }

            // Add 'field' keyword for backing field access
            names.add("field");
        }

        return names;
    }

    /**
     * Finds the containing class or object for the given element.
     * Walks up the PSI tree until it finds a KtClassOrObject.
     * 
     * @param element the element to start from
     * @return the containing class or null if not found
     */
    private KtClassOrObject findOwnerClass(@NotNull KtElement element) {
        PsiElement current = element;

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