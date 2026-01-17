package org.b333vv.metric.model.visitor.kotlin.type;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMember;
import com.intellij.psi.util.PsiTreeUtil;
import org.b333vv.metric.model.metric.Metric;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.psi.*;

import static org.b333vv.metric.model.metric.MetricType.MPC;

/**
 * Calculates Message Passing Coupling (MPC) metric for Kotlin classes.
 * <p>
 * MPC measures the number of external method calls (messages sent) from a class
 * to other classes.
 * This metric quantifies the coupling between a class and other classes through
 * method invocations.
 * Higher MPC values indicate stronger coupling and potentially more maintenance
 * effort.
 * </p>
 *
 * <h3>What is counted in MPC:</h3>
 * <ul>
 * <li><b>Regular method calls:</b> Direct invocations of methods on external
 * objects (e.g., {@code obj.method()})</li>
 * <li><b>Property accessor calls:</b> Calls to getters and setters of external
 * properties</li>
 * <li><b>Extension function calls:</b> Calls to extension functions defined on
 * external types</li>
 * <li><b>Infix function calls:</b> Infix notation invocations (e.g.,
 * {@code a to b})</li>
 * <li><b>Operator function calls:</b> Operator overloading invocations (e.g.,
 * {@code a + b}, {@code a[i]})</li>
 * <li><b>Invoke operator calls:</b> Direct invocation on callable objects
 * (e.g., {@code func()})</li>
 * <li><b>Safe call expressions:</b> Calls using safe navigation operator (e.g.,
 * {@code obj?.method()})</li>
 * <li><b>Scope function calls:</b> Standard library scope functions with
 * lambdas (let, run, apply, also, with, use)</li>
 * <li><b>Lambda invocations:</b> Calls within lambda expressions passed to
 * external functions</li>
 * <li><b>Callable references:</b> Method references used as function parameters
 * (e.g., {@code ::method})</li>
 * <li><b>Companion object calls:</b> Calls to methods in companion objects of
 * external classes</li>
 * <li><b>Top-level function calls:</b> Calls to functions defined at package
 * level in external packages</li>
 * <li><b>Constructor calls:</b> Object instantiation of external classes (e.g.,
 * {@code ExternalClass()})</li>
 * <li><b>Super calls:</b> Calls to parent class methods using {@code super}
 * keyword</li>
 * <li><b>Nested and inner class calls:</b> Calls to methods in nested/inner
 * classes of external types</li>
 * </ul>
 *
 * <h3>What is excluded from MPC:</h3>
 * <ul>
 * <li><b>Internal method calls:</b> Calls to methods within the same class
 * (including this.method())</li>
 * <li><b>Standard library calls:</b> Calls to Java standard library (java.*,
 * javax.*) and Kotlin standard library (kotlin.*)</li>
 * <li><b>Private function calls:</b> Calls to private functions within the same
 * class</li>
 * <li><b>Local function calls:</b> Calls to functions defined locally within
 * the same method</li>
 * <li><b>Type references:</b> Simple type usage without method invocation</li>
 * <li><b>Field access:</b> Direct field access without method invocation
 * (unless property getter is called)</li>
 * </ul>
 *
 * <h3>Counting rules:</h3>
 * <ul>
 * <li>Each method call is counted separately, even if the same method is called
 * multiple times</li>
 * <li>Chained calls are counted individually (e.g., {@code obj.m1().m2()}
 * counts as 2 calls)</li>
 * <li>Calls in all class members are included: functions, properties, init
 * blocks, constructors</li>
 * <li>Calls in nested lambdas and anonymous functions are included</li>
 * <li>Unresolved calls (where type resolution fails) are excluded for
 * accuracy</li>
 * </ul>
 *
 * <h3>Result:</h3>
 * MPC = total count of external method invocations
 *
 * <h3>Interpretation:</h3>
 * <ul>
 * <li><b>Low MPC (0-10):</b> Weak coupling, good encapsulation</li>
 * <li><b>Medium MPC (11-30):</b> Moderate coupling, acceptable for most
 * classes</li>
 * <li><b>High MPC (31+):</b> Strong coupling, may indicate god class or poor
 * separation of concerns</li>
 * </ul>
 *
 * <h3>Limitations:</h3>
 * <ul>
 * <li>Requires successful PSI type resolution - unresolved references are not
 * counted</li>
 * <li>Reflection-based calls and dynamic invocations are not detected</li>
 * <li>Some complex Kotlin constructs may not be fully analyzed</li>
 * </ul>
 *
 * @see org.b333vv.metric.model.metric.MetricType#MPC
 */
public class KotlinMessagePassingCouplingVisitor extends KotlinClassVisitor {

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
        final int[] calls = { 0 };
        String selfName = null;
        if (element instanceof KtClassOrObject) {
            KtClassOrObject klassOrObj = (KtClassOrObject) element;
            selfName = klassOrObj.getFqName() != null ? klassOrObj.getFqName().asString() : klassOrObj.getName();
        } else if (element instanceof KtFile) {
            selfName = ((KtFile) element).getName();
        }

        final String finalSelfName = selfName;

        element.accept(new KtTreeVisitorVoid() {
            @Override
            public void visitCallExpression(@NotNull KtCallExpression expression) {
                if (isExternalCall(expression, finalSelfName)) {
                    calls[0]++;
                }
                super.visitCallExpression(expression);
            }

            @Override
            public void visitBinaryExpression(@NotNull KtBinaryExpression expression) {
                // Handle operator calls and infix functions
                if (isOperatorOrInfixCall(expression, finalSelfName)) {
                    calls[0]++;
                }
                super.visitBinaryExpression(expression);
            }

            @Override
            public void visitUnaryExpression(@NotNull KtUnaryExpression expression) {
                // Handle unary operators (++, --, !, etc.)
                if (isExternalUnaryOperator(expression, finalSelfName)) {
                    calls[0]++;
                }
                super.visitUnaryExpression(expression);
            }

            @Override
            public void visitArrayAccessExpression(@NotNull KtArrayAccessExpression expression) {
                // Handle array access operators (get/set)
                if (isExternalArrayAccess(expression, finalSelfName)) {
                    calls[0]++;
                }
                super.visitArrayAccessExpression(expression);
            }

            @Override
            public void visitDotQualifiedExpression(@NotNull KtDotQualifiedExpression expression) {
                // Handle property access that may invoke getters
                if (isExternalPropertyAccess(expression, finalSelfName)) {
                    calls[0]++;
                }
                super.visitDotQualifiedExpression(expression);
            }

            @Override
            public void visitSafeQualifiedExpression(@NotNull KtSafeQualifiedExpression expression) {
                // Handle safe calls (?.) that invoke methods
                if (isExternalSafeCall(expression, finalSelfName)) {
                    calls[0]++;
                }
                super.visitSafeQualifiedExpression(expression);
            }

            @Override
            public void visitCallableReferenceExpression(@NotNull KtCallableReferenceExpression expression) {
                // Handle callable references (::functionName)
                if (isExternalCallableReference(expression, finalSelfName)) {
                    calls[0]++;
                }
                super.visitCallableReferenceExpression(expression);
            }
        });

        metric = Metric.of(MPC, calls[0]);
    }

    private boolean isExternalCall(KtCallExpression expression, String selfName) {
        try {
            PsiElement callee = expression.getCalleeExpression();
            if (callee == null)
                return false;

            // Try to resolve the reference
            PsiElement resolved = null;
            if (callee instanceof KtReferenceExpression) {
                resolved = ((KtReferenceExpression) callee).getReference().resolve();
            }

            if (resolved == null) {
                return false;
            }

            // Check if this is a local function
            if (isLocalFunction(resolved, expression)) {
                return false;
            }

            // Determine container of the resolved element
            String containerName = getContainerName(resolved);

            if (containerName != null) {
                // Filter self calls
                if (selfName != null && selfName.equals(containerName)) {
                    return false;
                }
                // Filter standard library
                if (isStandardClass(containerName)) {
                    return false;
                }
                return true;
            }

        } catch (Exception e) {
            // Ignore resolution errors
        }
        return false;
    }

    /**
     * Checks if a binary expression represents an operator call or infix function.
     */
    private boolean isOperatorOrInfixCall(KtBinaryExpression expression, String selfName) {
        try {
            // Check if this is a simple assignment or comparison that doesn't involve
            // external calls
            if (expression.getOperationReference().getReferencedName().matches("=")) {
                return false;
            }

            PsiElement resolved = expression.getOperationReference().getReference() != null
                    ? expression.getOperationReference().getReference().resolve()
                    : null;

            if (resolved == null) {
                return false;
            }

            String containerName = getContainerName(resolved);
            if (containerName != null && !selfName.equals(containerName) && !isStandardClass(containerName)) {
                return true;
            }
        } catch (Exception e) {
            // Ignore resolution errors
        }
        return false;
    }

    /**
     * Checks if a unary expression represents an external operator call.
     */
    private boolean isExternalUnaryOperator(KtUnaryExpression expression, String selfName) {
        try {
            PsiElement resolved = expression.getOperationReference().getReference() != null
                    ? expression.getOperationReference().getReference().resolve()
                    : null;

            if (resolved == null) {
                return false;
            }

            String containerName = getContainerName(resolved);
            if (containerName != null && !selfName.equals(containerName) && !isStandardClass(containerName)) {
                return true;
            }
        } catch (Exception e) {
            // Ignore
        }
        return false;
    }

    /**
     * Checks if an array access represents an external get/set operator call.
     */
    private boolean isExternalArrayAccess(KtArrayAccessExpression expression, String selfName) {
        try {
            KtExpression arrayExpr = expression.getArrayExpression();
            if (arrayExpr == null) {
                return false;
            }

            // For arrays, the get/set operators might be defined on the type
            // This is a simplified check - full implementation would resolve the operator
            if (arrayExpr instanceof KtReferenceExpression) {
                PsiElement resolved = ((KtReferenceExpression) arrayExpr).getReference().resolve();
                if (resolved != null) {
                    String containerName = getContainerName(resolved);
                    if (containerName != null && !selfName.equals(containerName) && !isStandardClass(containerName)) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            // Ignore
        }
        return false;
    }

    /**
     * Checks if a qualified expression represents an external property access
     * (getter call).
     */
    private boolean isExternalPropertyAccess(KtDotQualifiedExpression expression, String selfName) {
        try {
            KtExpression selector = expression.getSelectorExpression();
            // Only count if it's a simple name reference (property), not a call expression
            // (call expressions are handled separately)
            if (selector instanceof KtNameReferenceExpression && !(selector.getParent() instanceof KtCallExpression)) {
                PsiElement resolved = ((KtNameReferenceExpression) selector).getReference().resolve();
                if (resolved instanceof KtProperty || resolved instanceof KtParameter) {
                    String containerName = getContainerName(resolved);
                    if (containerName != null && !selfName.equals(containerName) && !isStandardClass(containerName)) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            // Ignore
        }
        return false;
    }

    /**
     * Checks if a safe qualified expression represents an external call.
     */
    private boolean isExternalSafeCall(KtSafeQualifiedExpression expression, String selfName) {
        try {
            KtExpression selector = expression.getSelectorExpression();
            if (selector instanceof KtCallExpression) {
                return isExternalCall((KtCallExpression) selector, selfName);
            } else if (selector instanceof KtNameReferenceExpression) {
                // Property access with safe call
                PsiElement resolved = ((KtNameReferenceExpression) selector).getReference().resolve();
                if (resolved != null) {
                    String containerName = getContainerName(resolved);
                    if (containerName != null && !selfName.equals(containerName) && !isStandardClass(containerName)) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            // Ignore
        }
        return false;
    }

    /**
     * Checks if a callable reference represents an external function reference.
     */
    private boolean isExternalCallableReference(KtCallableReferenceExpression expression, String selfName) {
        try {
            PsiElement resolved = expression.getCallableReference().getReference() != null
                    ? expression.getCallableReference().getReference().resolve()
                    : null;

            if (resolved == null) {
                return false;
            }

            String containerName = getContainerName(resolved);
            if (containerName != null && !selfName.equals(containerName) && !isStandardClass(containerName)) {
                return true;
            }
        } catch (Exception e) {
            // Ignore
        }
        return false;
    }

    /**
     * Checks if the resolved element is a local function defined within the current
     * scope.
     */
    private boolean isLocalFunction(PsiElement resolved, PsiElement context) {
        if (!(resolved instanceof KtNamedFunction)) {
            return false;
        }

        KtNamedFunction function = (KtNamedFunction) resolved;
        // Local functions are those defined inside another function
        return PsiTreeUtil.getParentOfType(function, KtNamedFunction.class) != null;
    }

    /**
     * Gets the fully qualified name of the container class/object for a given
     * element.
     */
    private String getContainerName(PsiElement resolved) {
        if (resolved instanceof PsiMember) {
            com.intellij.psi.PsiClass containingClass = ((PsiMember) resolved).getContainingClass();
            if (containingClass != null) {
                return containingClass.getQualifiedName();
            }
        } else if (resolved instanceof KtClassOrObject) {
            KtClassOrObject c = (KtClassOrObject) resolved;
            return c.getFqName() != null ? c.getFqName().asString() : c.getName();
        } else if (resolved instanceof KtDeclaration) {
            // For Kotlin declarations, find the containing class or object
            KtClassOrObject containingClass = PsiTreeUtil.getParentOfType(resolved, KtClassOrObject.class);
            if (containingClass != null) {
                return containingClass.getFqName() != null
                        ? containingClass.getFqName().asString()
                        : containingClass.getName();
            }

            // Check if it's a top-level declaration
            KtFile containingFile = PsiTreeUtil.getParentOfType(resolved, KtFile.class);
            if (containingFile != null) {
                // Top-level function - use package name
                String packageName = containingFile.getPackageFqName().asString();
                if (!packageName.isEmpty()) {
                    return packageName;
                }
            }
        }
        return null;
    }

    /**
     * Checks if a fully qualified class name belongs to the standard library.
     */
    private boolean isStandardClass(String qName) {
        return qName.startsWith("java.") || qName.startsWith("kotlin.") || qName.startsWith("javax.");
    }
}