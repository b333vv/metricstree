/*
 * Kotlin Number Of Attributes (NOA) - initial implementation
 */
package org.b333vv.metric.model.visitor.kotlin.type;

import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.psi.*;

import static org.b333vv.metric.model.metric.MetricType.NOA;

/**
 * Counts attributes (properties) for a Kotlin class.
 */
public class KotlinNumberOfAttributesVisitor extends KotlinClassVisitor {

    @Override
    public void visitClass(@NotNull KtClass klass) {
        int count = 0;

        // Primary constructor properties (val/var parameters)
        KtPrimaryConstructor primary = klass.getPrimaryConstructor();
        if (primary != null) {
            for (KtParameter p : primary.getValueParameters()) {
                if (p.hasValOrVar()) {
                    count += 1;
                }
            }
        }

        // Class body properties (exclude nested objects)
        KtClassBody body = klass.getBody();
        if (body != null) {
            boolean isInterface = klass.isInterface();
            for (KtDeclaration decl : body.getDeclarations()) {
                if (decl instanceof KtProperty) {
                    if (hasBackingField((KtProperty) decl, isInterface)) {
                        count += 1;
                    }
                }
            }
        }

        metric = Metric.of(NOA, count);
    }

    private boolean hasBackingField(KtProperty property, boolean isInterface) {
        if (isInterface)
            return false; // Interfaces cannot have state
        if (property.hasModifier(org.jetbrains.kotlin.lexer.KtTokens.ABSTRACT_KEYWORD))
            return false;

        if (property.hasDelegate())
            return true;
        if (property.hasInitializer())
            return true;
        if (property.hasModifier(org.jetbrains.kotlin.lexer.KtTokens.LATEINIT_KEYWORD))
            return true;

        // Check custom accessors for 'field' usage
        KtPropertyAccessor getter = property.getGetter();
        KtPropertyAccessor setter = property.getSetter();

        if (getter != null && referencesField(getter))
            return true;
        if (setter != null && referencesField(setter))
            return true;

        return false;
    }

    private boolean referencesField(KtPropertyAccessor accessor) {
        if (!accessor.hasBody())
            return true; // Default implementation implicitly uses field

        java.util.concurrent.atomic.AtomicBoolean found = new java.util.concurrent.atomic.AtomicBoolean(false);
        accessor.accept(new KtTreeVisitorVoid() {
            @Override
            public void visitSimpleNameExpression(@NotNull KtSimpleNameExpression expression) {
                if ("field".equals(expression.getReferencedName())) {
                    found.set(true);
                }
                super.visitSimpleNameExpression(expression);
            }
        });
        return found.get();
    }
}
