/*
 * Kotlin Response For Class (RFC) - PSI-based implementation
 */
package org.b333vv.metric.model.visitor.kotlin.type;

import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.value.Value;
import org.b333vv.metric.model.visitor.kotlin.KotlinMetricUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.psi.*;

import java.util.HashSet;
import java.util.Set;

import static org.b333vv.metric.model.metric.MetricType.RFC;

/**
 * RFC for Kotlin: size of the set consisting of
 * - methods declared in the class (functions and constructors)
 * - plus unique method calls from within the class bodies (by simple name)
 *
 * This PSI-only approach uses call names without resolve; Phase 3 can augment
 * with proper resolution.
 */
public class KotlinResponseForClassVisitor extends KotlinClassVisitor {

    @Override
    public void visitClass(@NotNull KtClass klass) {
        try {
            Set<String> responses = new HashSet<>();

            // Declared methods: functions only (match Java RFC which uses
            // psiClass.getMethods())
            KtClassBody body = klass.getBody();
            if (body != null) {
                for (KtDeclaration decl : body.getDeclarations()) {
                    if (decl instanceof KtNamedFunction) {
                        KtNamedFunction f = (KtNamedFunction) decl;
                        String name = f.getName();
                        if (name != null) {
                            int arity = f.getValueParameters().size();
                            responses.add(name + "/" + arity);
                        }
                    } else if (decl instanceof KtProperty) {
                        // Add implicit property accessors
                        KtProperty prop = (KtProperty) decl;
                        if (!KotlinMetricUtils.isInCompanionObject(prop)) {
                            String propName = prop.getName();
                            if (propName != null) {
                                // Getter: get<PropertyName>/0
                                responses.add("get" + capitalize(propName) + "/0");
                                // Setter for var: set<PropertyName>/1
                                if (prop.isVar()) {
                                    responses.add("set" + capitalize(propName) + "/1");
                                }
                            }
                        }
                    }
                }
            }

            // Add constructors
            String className = klass.getName();
            if (className != null) {
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

            // Add implicit accessors for primary constructor properties
            KtPrimaryConstructor primary = klass.getPrimaryConstructor();
            if (primary != null) {
                for (KtParameter param : primary.getValueParameters()) {
                    if (param.hasValOrVar()) {
                        String paramName = param.getName();
                        if (paramName != null) {
                            responses.add("get" + capitalize(paramName) + "/0");
                            if (param.isMutable()) {
                                responses.add("set" + capitalize(paramName) + "/1");
                            }
                        }
                    }
                }
            }

            // Add data class implicit methods
            if (klass.isData()) {
                responses.add("equals/1");
                responses.add("hashCode/0");
                responses.add("toString/0");
                // copy method has same arity as primary constructor parameters
                if (primary != null) {
                    responses.add("copy/" + primary.getValueParameters().size());
                    // componentN methods
                    for (int i = 1; i <= primary.getValueParameters().size(); i++) {
                        responses.add("component" + i + "/0");
                    }
                }
            }

            // Traverse class to collect call expressions (use simple name + arity)
            klass.accept(new KtTreeVisitorVoid() {
                @Override
                public void visitCallExpression(@NotNull KtCallExpression expression) {
                    KtExpression calleeExpr = expression.getCalleeExpression();
                    if (calleeExpr instanceof KtSimpleNameExpression) {
                        String name = ((KtSimpleNameExpression) calleeExpr).getReferencedName();
                        if (name != null && !name.isEmpty()) {
                            int arity = expression.getValueArguments().size();
                            responses.add(name + "/" + arity);
                        }
                    }
                    super.visitCallExpression(expression);
                }
            });

            metric = Metric.of(RFC, responses.size());
        } catch (Exception e) {
            metric = Metric.of(RFC, Value.UNDEFINED);
        }
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty())
            return str;
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }
}
