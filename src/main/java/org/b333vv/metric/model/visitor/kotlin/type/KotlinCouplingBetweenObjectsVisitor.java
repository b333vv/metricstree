package org.b333vv.metric.model.visitor.kotlin.type;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import org.b333vv.metric.model.metric.Metric;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.psi.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.b333vv.metric.model.metric.MetricType.CBO;

/**
 * Visitor for calculating Coupling Between Objects (CBO) metric for Kotlin classes.
 * <p>
 * The CBO metric counts the number of unique external types that a class is coupled to.
 * A class is coupled to another class if it uses that class in any way (inheritance, composition,
 * method parameters, return types, local variables, etc.).
 * </p>
 *
 * <h3>What is included in CBO calculation:</h3>
 * <ul>
 *   <li><b>Supertypes:</b> All direct parent classes and implemented interfaces from superTypeListEntries</li>
 *   <li><b>Primary constructor parameters:</b> Types of all property parameters (val/var) in primary constructor</li>
 *   <li><b>Secondary constructors:</b> Parameter types in all secondary constructors</li>
 *   <li><b>Properties:</b> Types of all properties (val/var) declared in class body</li>
 *   <li><b>Property delegates:</b> Types used in property delegation (by keyword)</li>
 *   <li><b>Functions:</b> Receiver types, parameter types, and return types of all functions</li>
 *   <li><b>Function bodies:</b> Types of local variables and expressions within function implementations</li>
 *   <li><b>Lambda parameters:</b> Types used in lambda expressions and function literals</li>
 *   <li><b>Type aliases:</b> Resolved types when type aliases are used</li>
 *   <li><b>Annotations:</b> Types of annotations applied to class, properties, functions, and parameters</li>
 *   <li><b>Class delegates:</b> Types used in class delegation (interfaces delegated to another object)</li>
 *   <li><b>Init blocks:</b> Types referenced in class initialization blocks</li>
 *   <li><b>Companion objects:</b> Types used within companion object declarations</li>
 *   <li><b>Nested classes:</b> For nested classes, CBO is calculated separately and does not affect outer class</li>
 *   <li><b>Generic type arguments:</b> All type arguments used in generic type declarations (e.g., List&lt;String&gt; counts String)</li>
 * </ul>
 *
 * <h3>What is excluded from CBO calculation:</h3>
 * <ul>
 *   <li><b>Standard library types:</b> Classes from kotlin.*, java.*, and javax.* packages</li>
 *   <li><b>Self-references:</b> References to the class itself</li>
 *   <li><b>Primitive types:</b> Built-in Kotlin primitive types (Int, String, Boolean, etc.)</li>
 *   <li><b>Type parameters:</b> Generic type parameter names (T, E, K, V) without bounds</li>
 * </ul>
 *
 * <h3>De-duplication:</h3>
 * Each unique external type is counted only once, regardless of how many times it appears in the class.
 *
 * <h3>Result:</h3>
 * CBO = number of distinct external types referenced by the class
 *
 * <h3>Limitations:</h3>
 * <ul>
 *   <li>Type resolution depends on PSI and may fail for unresolved references</li>
 *   <li>Some complex type constructs may not be fully analyzed</li>
 *   <li>Dynamic types and reflection-based references are not tracked</li>
 * </ul>
 *
 * @see org.b333vv.metric.model.metric.MetricType#CBO
 */
public class KotlinCouplingBetweenObjectsVisitor extends KotlinClassVisitor {

    @Override
    public void visitClass(@NotNull KtClass klass) {
        Set<String> types = new HashSet<>();
        String selfName = klass.getFqName() != null ? klass.getFqName().asString() : klass.getName();

        // 1. Supertypes (inheritance and interfaces)
        List<KtSuperTypeListEntry> superTypes = klass.getSuperTypeListEntries();
        for (KtSuperTypeListEntry st : superTypes) {
            addTypesFromTypeRef(types, st.getTypeReference(), selfName);
            // Include delegate expressions in class delegation
            KtDelegatedSuperTypeEntry delegated = st instanceof KtDelegatedSuperTypeEntry ? (KtDelegatedSuperTypeEntry) st : null;
            if (delegated != null && delegated.getDelegateExpression() != null) {
                scanExpressionForTypes(types, delegated.getDelegateExpression(), selfName);
            }
        }

        // 2. Annotations on the class itself
        collectAnnotationTypes(types, klass.getAnnotationEntries(), selfName);

        // 3. Primary constructor
        KtPrimaryConstructor primary = klass.getPrimaryConstructor();
        if (primary != null) {
            // Annotations on constructor
            collectAnnotationTypes(types, primary.getAnnotationEntries(), selfName);
            
            for (KtParameter p : primary.getValueParameters()) {
                // Parameter type
                addTypesFromTypeRef(types, p.getTypeReference(), selfName);
                // Annotations on parameter
                collectAnnotationTypes(types, p.getAnnotationEntries(), selfName);
                // Default value expression
                if (p.getDefaultValue() != null) {
                    scanExpressionForTypes(types, p.getDefaultValue(), selfName);
                }
            }
        }

        // 4. Class body elements
        KtClassBody body = klass.getBody();
        if (body != null) {
            for (KtDeclaration decl : body.getDeclarations()) {
                if (decl instanceof KtProperty) {
                    processProperty(types, (KtProperty) decl, selfName);
                } else if (decl instanceof KtNamedFunction) {
                    processFunction(types, (KtNamedFunction) decl, selfName);
                } else if (decl instanceof KtSecondaryConstructor) {
                    processSecondaryConstructor(types, (KtSecondaryConstructor) decl, selfName);
                } else if (decl instanceof KtClassInitializer) {
                    // Init blocks
                    KtClassInitializer init = (KtClassInitializer) decl;
                    if (init.getBody() != null) {
                        scanExpressionForTypes(types, init.getBody(), selfName);
                    }
                } else if (decl instanceof KtObjectDeclaration) {
                    // Companion objects and named objects
                    processNestedObject(types, (KtObjectDeclaration) decl, selfName);
                } else if (decl instanceof KtClass) {
                    // Nested classes are handled separately - don't include their internal types
                    // in outer class CBO
                }
            }
        }

        metric = Metric.of(CBO, types.size());
    }

    /**
     * Processes a property declaration, including its type, delegate, initializer, and annotations.
     */
    private void processProperty(Set<String> types, KtProperty property, String selfName) {
        // Property type
        addTypesFromTypeRef(types, property.getTypeReference(), selfName);
        
        // Annotations
        collectAnnotationTypes(types, property.getAnnotationEntries(), selfName);
        
        // Delegate expression (by keyword)
        if (property.getDelegate() != null && property.getDelegateExpression() != null) {
            scanExpressionForTypes(types, property.getDelegateExpression(), selfName);
        }
        
        // Initializer
        if (property.getInitializer() != null) {
            scanExpressionForTypes(types, property.getInitializer(), selfName);
        }
        
        // Getter
        if (property.getGetter() != null && property.getGetter().getBodyExpression() != null) {
            scanExpressionForTypes(types, property.getGetter().getBodyExpression(), selfName);
            addTypesFromTypeRef(types, property.getGetter().getReturnTypeReference(), selfName);
        }
        
        // Setter
        if (property.getSetter() != null) {
            if (property.getSetter().getBodyExpression() != null) {
                scanExpressionForTypes(types, property.getSetter().getBodyExpression(), selfName);
            }
            KtParameter setterParam = property.getSetter().getParameter();
            if (setterParam != null) {
                addTypesFromTypeRef(types, setterParam.getTypeReference(), selfName);
            }
        }
    }

    /**
     * Processes a function declaration, including receiver, parameters, return type, and body.
     */
    private void processFunction(Set<String> types, KtNamedFunction function, String selfName) {
        // Annotations
        collectAnnotationTypes(types, function.getAnnotationEntries(), selfName);
        
        // Receiver type (extension function)
        addTypesFromTypeRef(types, function.getReceiverTypeReference(), selfName);
        
        // Parameters
        for (KtParameter p : function.getValueParameters()) {
            addTypesFromTypeRef(types, p.getTypeReference(), selfName);
            collectAnnotationTypes(types, p.getAnnotationEntries(), selfName);
            if (p.getDefaultValue() != null) {
                scanExpressionForTypes(types, p.getDefaultValue(), selfName);
            }
        }
        
        // Return type
        addTypesFromTypeRef(types, function.getTypeReference(), selfName);
        
        // Function body
        if (function.getBodyExpression() != null) {
            scanExpressionForTypes(types, function.getBodyExpression(), selfName);
        }
        
        if (function.getBodyBlockExpression() != null) {
            scanExpressionForTypes(types, function.getBodyBlockExpression(), selfName);
        }
    }

    /**
     * Processes a secondary constructor.
     */
    private void processSecondaryConstructor(Set<String> types, KtSecondaryConstructor constructor, String selfName) {
        // Annotations
        collectAnnotationTypes(types, constructor.getAnnotationEntries(), selfName);
        
        // Parameters
        for (KtParameter p : constructor.getValueParameters()) {
            addTypesFromTypeRef(types, p.getTypeReference(), selfName);
            collectAnnotationTypes(types, p.getAnnotationEntries(), selfName);
            if (p.getDefaultValue() != null) {
                scanExpressionForTypes(types, p.getDefaultValue(), selfName);
            }
        }
        
        // Constructor body
        if (constructor.getBodyExpression() != null) {
            scanExpressionForTypes(types, constructor.getBodyExpression(), selfName);
        }
    }

    /**
     * Processes nested object declarations (companion objects and named objects).
     */
    private void processNestedObject(Set<String> types, KtObjectDeclaration obj, String selfName) {
        // For companion objects, include types they reference as part of outer class coupling
        if (obj.isCompanion()) {
            // Supertypes
            for (KtSuperTypeListEntry st : obj.getSuperTypeListEntries()) {
                addTypesFromTypeRef(types, st.getTypeReference(), selfName);
            }
            
            // Object body
            KtClassBody body = obj.getBody();
            if (body != null) {
                for (KtDeclaration decl : body.getDeclarations()) {
                    if (decl instanceof KtProperty) {
                        processProperty(types, (KtProperty) decl, selfName);
                    } else if (decl instanceof KtNamedFunction) {
                        processFunction(types, (KtNamedFunction) decl, selfName);
                    }
                }
            }
        }
        // For named objects, we could either include or exclude them - current approach excludes
        // their internal coupling from outer class, which is more conservative
    }

    /**
     * Scans an expression for type references, including local variables, casts, and nested expressions.
     */
    private void scanExpressionForTypes(Set<String> types, KtExpression expression, String selfName) {
        if (expression == null) return;
        
        expression.accept(new KtTreeVisitorVoid() {
            @Override
            public void visitTypeReference(@NotNull KtTypeReference typeReference) {
                addTypesFromTypeRef(types, typeReference, selfName);
                super.visitTypeReference(typeReference);
            }
            
            @Override
            public void visitAnnotationEntry(@NotNull KtAnnotationEntry annotationEntry) {
                addTypesFromTypeRef(types, annotationEntry.getTypeReference(), selfName);
                super.visitAnnotationEntry(annotationEntry);
            }
        });
    }

    /**
     * Collects types from annotation entries.
     */
    private void collectAnnotationTypes(Set<String> types, List<KtAnnotationEntry> annotations, String selfName) {
        for (KtAnnotationEntry annotation : annotations) {
            addTypesFromTypeRef(types, annotation.getTypeReference(), selfName);
            // Annotation arguments may contain type references
            if (annotation.getValueArgumentList() != null) {
                for (KtValueArgument arg : annotation.getValueArgumentList().getArguments()) {
                    if (arg.getArgumentExpression() != null) {
                        scanExpressionForTypes(types, arg.getArgumentExpression(), selfName);
                    }
                }
            }
        }
    }

    /**
     * Extracts and adds all user types from a type reference, including nested generic arguments.
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
                        } else if (target instanceof KtTypeAlias) {
                            // For type aliases, get the actual aliased type
                            KtTypeAlias alias = (KtTypeAlias) target;
                            addTypesFromTypeRef(sink, alias.getTypeReference(), selfName);
                            // Don't return here - continue processing
                        }

                        if (qName != null && !isStandardClass(qName) && (selfName == null || !qName.equals(selfName))) {
                            sink.add(qName);
                        }
                    } catch (Exception e) {
                        // Ignore resolution errors - unresolved types are not counted
                    }
                }
                super.visitUserType(type);
            }
        });
    }

    /**
     * Checks if a class is from standard library (Java or Kotlin standard library).
     */
    private boolean isStandardClass(String qName) {
        return qName.startsWith("java.") || qName.startsWith("kotlin.") || qName.startsWith("javax.");
    }
}
