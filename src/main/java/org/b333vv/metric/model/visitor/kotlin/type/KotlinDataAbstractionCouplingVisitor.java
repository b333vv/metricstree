package org.b333vv.metric.model.visitor.kotlin.type;

import org.b333vv.metric.model.metric.Metric;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.psi.*;

import java.util.HashSet;
import java.util.Set;

import static org.b333vv.metric.model.metric.MetricType.DAC;

/**
 * Kotlin Data Abstraction Coupling (DAC) visitor for metric calculation.
 * 
 * <p>This visitor calculates the Data Abstraction Coupling metric for Kotlin classes,
 * which measures the number of distinct Abstract Data Types (ADTs) used as attribute types.
 * Higher DAC values indicate greater coupling complexity and potentially reduced reusability.
 * 
 * <h2>Metric Scope</h2>
 * The DAC metric counts unique types from the following sources:
 * <ul>
 *   <li><b>Primary constructor properties</b> - properties declared with val/var in primary constructor</li>
 *   <li><b>Class body properties</b> - properties declared in the class body</li>
 *   <li><b>Delegated properties</b> - types of delegate expressions (e.g., by lazy, by observable)</li>
 *   <li><b>Custom getter return types</b> - explicit return types of property getters</li>
 *   <li><b>Generic type parameters</b> - recursively extracted from parameterized types
 *       (e.g., List&lt;String&gt; counts both List and String)</li>
 * </ul>
 * 
 * <h2>Exclusions</h2>
 * The following types are explicitly excluded from the metric:
 * <ul>
 *   <li>Standard library types (packages: java.*, kotlin.*, javax.*)</li>
 *   <li>The class itself (self-references)</li>
 *   <li>Primitive types and their wrappers (handled by standard library exclusion)</li>
 * </ul>
 * 
 * <h2>Examples</h2>
 * <pre>
 * class Example(
 *     val name: String,              // excluded (kotlin.String)
 *     val user: User                 // counted
 * ) {
 *     val items: List&lt;Product&gt;        // counts List and Product
 *     val config by lazy { Config() } // counts Config (delegate type)
 *     val computed: Result            // counted
 *         get() = calculateResult()
 * }
 * // DAC = 4 (User, List, Product, Config, Result)
 * </pre>
 * 
 * @see org.b333vv.metric.model.metric.MetricType#DAC
 */
public class KotlinDataAbstractionCouplingVisitor extends KotlinClassVisitor {

    @Override
    public void visitClass(@NotNull KtClass klass) {
        Set<String> types = new HashSet<>();
        String selfName = klass.getFqName() != null ? klass.getFqName().asString() : klass.getName();

        // Primary constructor properties (val/var parameters)
        KtPrimaryConstructor primary = klass.getPrimaryConstructor();
        if (primary != null) {
            for (KtParameter p : primary.getValueParameters()) {
                if (p.hasValOrVar()) {
                    addTypesFromTypeRef(types, p.getTypeReference(), selfName);
                }
            }
        }

        // Class body properties
        KtClassBody body = klass.getBody();
        if (body != null) {
            for (KtDeclaration decl : body.getDeclarations()) {
                if (decl instanceof KtProperty) {
                    KtProperty property = (KtProperty) decl;
                    
                    // Extract types from property declaration type
                    addTypesFromTypeRef(types, property.getTypeReference(), selfName);
                    
                    // Extract types from delegated property expression
                    KtPropertyDelegate delegate = property.getDelegate();
                    if (delegate != null) {
                        addTypesFromExpression(types, delegate.getExpression(), selfName);
                    }
                    
                    // Extract types from custom getter return type
                    KtPropertyAccessor getter = property.getGetter();
                    if (getter != null && getter.getReturnTypeReference() != null) {
                        addTypesFromTypeRef(types, getter.getReturnTypeReference(), selfName);
                    }
                }
            }
        }

        metric = Metric.of(DAC, types.size());
    }

    /**
     * Extracts and adds all type references from a Kotlin type reference, including
     * recursively extracting generic type parameters.
     * 
     * @param sink the set to collect unique type names
     * @param typeRef the type reference to analyze
     * @param selfName the fully qualified name of the current class (to exclude self-references)
     */
    private void addTypesFromTypeRef(Set<String> sink, KtTypeReference typeRef, String selfName) {
        if (typeRef == null)
            return;
        typeRef.accept(new KtTreeVisitorVoid() {
            @Override
            public void visitUserType(@NotNull KtUserType type) {
                KtSimpleNameExpression refExpr = type.getReferenceExpression();
                if (refExpr != null) {
                    try {
                        PsiElement target = refExpr.getReference() != null ? refExpr.getReference().resolve() : null;
                        String qName = null;
                        if (target instanceof PsiClass) {
                            qName = ((PsiClass) target).getQualifiedName();
                        } else if (target instanceof KtClassOrObject) {
                            qName = ((KtClassOrObject) target).getFqName() != null
                                    ? ((KtClassOrObject) target).getFqName().asString()
                                    : null;
                        }

                        if (qName != null && !isStandardClass(qName) && (selfName == null || !qName.equals(selfName))) {
                            sink.add(qName);
                        }
                    } catch (Exception e) {
                        // Ignore resolution errors
                    }
                }
                
                // Recursively process type arguments (generics)
                for (KtTypeProjection projection : type.getTypeArguments()) {
                    KtTypeReference argTypeRef = projection.getTypeReference();
                    if (argTypeRef != null) {
                        addTypesFromTypeRef(sink, argTypeRef, selfName);
                    }
                }
                
                super.visitUserType(type);
            }
        });
    }

    /**
     * Extracts types from Kotlin expressions, particularly useful for delegate expressions
     * (e.g., by lazy { SomeType() }, by observable(default) { ... }).
     * 
     * @param sink the set to collect unique type names
     * @param expr the expression to analyze
     * @param selfName the fully qualified name of the current class (to exclude self-references)
     */
    private void addTypesFromExpression(Set<String> sink, KtExpression expr, String selfName) {
        if (expr == null)
            return;
        
        expr.accept(new KtTreeVisitorVoid() {
            @Override
            public void visitCallExpression(@NotNull KtCallExpression expression) {
                // Process lambda arguments in delegates (e.g., lazy { ... })
                for (KtValueArgument arg : expression.getValueArguments()) {
                    KtExpression argExpr = arg.getArgumentExpression();
                    if (argExpr instanceof KtLambdaExpression) {
                        KtLambdaExpression lambda = (KtLambdaExpression) argExpr;
                        extractTypesFromLambdaBody(sink, lambda, selfName);
                    }
                }
                super.visitCallExpression(expression);
            }
            
            @Override
            public void visitTypeReference(@NotNull KtTypeReference typeReference) {
                addTypesFromTypeRef(sink, typeReference, selfName);
                super.visitTypeReference(typeReference);
            }
        });
    }

    /**
     * Extracts type references from lambda expression bodies, particularly object
     * instantiations and type casts.
     * 
     * @param sink the set to collect unique type names
     * @param lambda the lambda expression to analyze
     * @param selfName the fully qualified name of the current class (to exclude self-references)
     */
    private void extractTypesFromLambdaBody(Set<String> sink, KtLambdaExpression lambda, String selfName) {
        KtBlockExpression body = lambda.getBodyExpression();
        if (body == null)
            return;
        
        body.accept(new KtTreeVisitorVoid() {
            @Override
            public void visitCallExpression(@NotNull KtCallExpression expression) {
                KtExpression calleeExpr = expression.getCalleeExpression();
                if (calleeExpr instanceof KtSimpleNameExpression) {
                    try {
                        PsiElement target = ((KtSimpleNameExpression) calleeExpr).getReference() != null
                                ? ((KtSimpleNameExpression) calleeExpr).getReference().resolve()
                                : null;
                        
                        if (target instanceof KtClass) {
                            KtClass targetClass = (KtClass) target;
                            String qName = targetClass.getFqName() != null
                                    ? targetClass.getFqName().asString()
                                    : null;
                            
                            if (qName != null && !isStandardClass(qName) && (selfName == null || !qName.equals(selfName))) {
                                sink.add(qName);
                            }
                        }
                    } catch (Exception e) {
                        // Ignore resolution errors
                    }
                }
                super.visitCallExpression(expression);
            }
            
            @Override
            public void visitTypeReference(@NotNull KtTypeReference typeReference) {
                addTypesFromTypeRef(sink, typeReference, selfName);
                super.visitTypeReference(typeReference);
            }
        });
    }

    /**
     * Checks if a fully qualified class name belongs to the standard library
     * and should be excluded from the DAC metric.
     * 
     * @param qName the fully qualified class name
     * @return true if the class is from standard library (java.*, kotlin.*, javax.*), false otherwise
     */
    private boolean isStandardClass(String qName) {
        return qName.startsWith("java.") || qName.startsWith("kotlin.") || qName.startsWith("javax.");
    }
}
