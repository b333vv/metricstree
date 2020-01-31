package org.jacoquev.model.metric.meter.type;

import com.google.common.collect.ImmutableSet;
import com.intellij.psi.*;
import com.intellij.psi.tree.IElementType;
import org.jacoquev.model.code.JavaClass;
import org.jacoquev.model.metric.Meter;
import org.jacoquev.model.metric.Metric;
import org.jacoquev.util.MetricsUtils;

import java.util.Arrays;
import java.util.Set;

public class WeightedMethodCount implements Meter<JavaClass> {
    @Override
    public Set<Metric> meter(JavaClass javaClass) {
        PsiClass psiClass = javaClass.getPsiClass();
        long weightedMethodCount = MetricsUtils.callInReadAction(() -> {
            long result = 0;
            if (!(psiClass.isInterface() ||
                    psiClass.isEnum() ||
                    psiClass instanceof PsiAnonymousClass ||
                    psiClass instanceof PsiTypeParameter ||
                    psiClass.getParent() instanceof PsiDeclarationStatement)) {
                result = getWeightedMethodCount(psiClass);
            }
            return result;
        });
        return ImmutableSet.of(
                Metric.of("WMC", "Weighted Method Count",
                        "/html/WeightedMethodCount.html", weightedMethodCount)
        );
    }

    private long getWeightedMethodCount(PsiClass psiClass) {
        return Arrays.stream(psiClass.getMethods())
                .mapToLong(this::calculateMethodComplexity)
                .sum();
    }

    public long calculateMethodComplexity(PsiMethod psiMethod) {
        if (psiMethod == null) {
            return 1;
        }
        MethodComplexityVisitor visitor = new MethodComplexityVisitor();
        psiMethod.accept(visitor);
        return visitor.getMethodComplexity();
    }

    private static class MethodComplexityVisitor extends JavaRecursiveElementWalkingVisitor {

        private long methodComplexity = 1;

        @Override
        public void visitForStatement(PsiForStatement statement) {
            super.visitForStatement(statement);
            methodComplexity++;
        }

        @Override
        public void visitForeachStatement(PsiForeachStatement statement) {
            super.visitForeachStatement(statement);
            methodComplexity++;
        }

        @Override
        public void visitIfStatement(PsiIfStatement statement) {
            super.visitIfStatement(statement);
            methodComplexity++;
        }

        @Override
        public void visitDoWhileStatement(PsiDoWhileStatement statement) {
            super.visitDoWhileStatement(statement);
            methodComplexity++;
        }

        @Override
        public void visitConditionalExpression(PsiConditionalExpression expression) {
            super.visitConditionalExpression(expression);
            methodComplexity++;
        }

        @Override
        public void visitSwitchStatement(PsiSwitchStatement statement) {
            super.visitSwitchStatement(statement);
            final PsiCodeBlock body = statement.getBody();
            if (body == null) {
                return;
            }
            final PsiStatement[] statements = body.getStatements();
            boolean pendingLabel = false;
            boolean accepted = true;
            for (final PsiStatement child : statements) {
                if (child instanceof PsiSwitchLabelStatement) {
                    if (!pendingLabel && accepted) {
                        methodComplexity++;
                    }
                    accepted = true;
                    pendingLabel = true;
                } else {
                    accepted = true;
                    pendingLabel = false;
                }
            }
        }

        @Override
        public void visitWhileStatement(PsiWhileStatement statement) {
            super.visitWhileStatement(statement);
            methodComplexity++;
        }

        @Override
        public void visitCatchSection(PsiCatchSection section) {
            super.visitCatchSection(section);
            methodComplexity++;
        }

        @Override
        public void visitPolyadicExpression(PsiPolyadicExpression expression) {
            super.visitPolyadicExpression(expression);
            final IElementType token = expression.getOperationTokenType();
            if (token.equals(JavaTokenType.ANDAND) || token.equals(JavaTokenType.OROR)) {
                methodComplexity += expression.getOperands().length - 1;
            }
        }

        public long getMethodComplexity() {
            return methodComplexity;
        }
    }
}
