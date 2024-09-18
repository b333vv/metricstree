/*
 * Copyright 2020 b333vv
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.b333vv.metric.model.visitor.method;

import com.intellij.psi.*;
import com.intellij.psi.tree.IElementType;
import org.b333vv.metric.model.util.CognitiveComplexityBag;
import org.b333vv.metric.model.util.CogntiveComplexityElementType;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

import static org.b333vv.metric.model.util.CogntiveComplexityElementType.*;

public class MethodCognitiveComplexityVisitor extends JavaMethodVisitor {

    private final CognitiveComplexityBag bag = new CognitiveComplexityBag();

    @Override
    public void visitIfStatement(@NotNull PsiIfStatement statement) {
        super.visitIfStatement(statement);
        if (isElseIf(statement)) {
            return;
        }
        bag.increaseComplexityAndNesting(IF);
        postProcess(statement);
    }

    private boolean isElseIf(PsiIfStatement ifStatement) {
        return isElse(prevNotWhitespace(ifStatement));
    }

    private boolean isElse(PsiElement element) {
        return element != null && element instanceof PsiKeyword && element.getText().equals(PsiKeyword.ELSE);
    }

    private PsiElement prevNotWhitespace(PsiIfStatement ifStatement) {
        PsiElement prev = ifStatement;
        while (prev.getPrevSibling() != null) {
            prev = prev.getPrevSibling();
            if (!(prev instanceof PsiWhiteSpace)) {
                return prev;
            }
        }
        return null;
    }

    public void visitMethodCallExpression(@NotNull PsiMethodCallExpression methodCallExpression) {
        super.visitMethodCallExpression(methodCallExpression);
        if (isRecursion(methodCallExpression)) {
            bag.increaseComplexity(ELSE);
        }
        postProcess(methodCallExpression);
    }

    private boolean isRecursion(PsiMethodCallExpression methodCallExpression) {
        PsiMethod parentMethod = findCurrentMethod(methodCallExpression);
        if (parentMethod == null) {
            return false;
        }
        PsiReferenceExpression methodExpression = methodCallExpression.getMethodExpression();
        if (!methodExpression.getText().equals(parentMethod.getNameIdentifier().getText())) {
            return false;
        }
        return methodCallExpression.getArgumentList().getExpressionCount() == parentMethod.getParameterList().getParametersCount();
    }

    private PsiMethod findCurrentMethod(PsiElement element) {
        while (element != null && !(element instanceof PsiMethod)) {
            element = element.getParent();
        }
        return (PsiMethod) element;
    }

    @Override
    public void visitKeyword(@NotNull PsiKeyword keyword) {
        super.visitKeyword(keyword);
        if (Objects.equals(keyword.getText(), PsiKeyword.ELSE) && keyword.getParent() instanceof PsiIfStatement) {
            bag.increaseComplexity(ELSE);
        }
        postProcess(keyword);
    }

    @Override
    public void visitLambdaExpression(@NotNull PsiLambdaExpression expression) {
        super.visitLambdaExpression(expression);
        bag.increaseNesting();
        postProcess(expression);
    }

    @Override
    public void visitContinueStatement(@NotNull PsiContinueStatement statement) {
        super.visitContinueStatement(statement);
        if (statement.getLabelIdentifier() != null) {
            bag.increaseComplexityAndNesting(CONTINUE);
        }
        postProcess(statement);
    }

    @Override
    public void visitBreakStatement(@NotNull PsiBreakStatement statement) {
        super.visitBreakStatement(statement);
        if (statement.getLabelIdentifier() != null) {
            bag.increaseComplexityAndNesting(BREAK);
        }
        postProcess(statement);
    }

    @Override
    public void visitForStatement(@NotNull PsiForStatement statement) {
        super.visitForStatement(statement);
        bag.increaseComplexityAndNesting(FOR);
        postProcess(statement);
    }

    @Override
    public void visitForeachStatement(@NotNull PsiForeachStatement statement) {
        super.visitForeachStatement(statement);
        bag.increaseComplexityAndNesting(FOR);
        postProcess(statement);
    }

    @Override
    public void visitDoWhileStatement(@NotNull PsiDoWhileStatement statement) {
        super.visitDoWhileStatement(statement);
        bag.increaseComplexityAndNesting(WHILE);
        postProcess(statement);
    }

    @Override
    public void visitConditionalExpression(@NotNull PsiConditionalExpression expression) {
        super.visitConditionalExpression(expression);
        bag.increaseComplexityAndNesting(IF);
        calculateBinaryComplexity(expression);
        postProcess(expression);
    }

    private void calculateBinaryComplexity(PsiExpression expression) {
        IElementType prevOperand = null;
        for (PsiElement element : expression.getChildren()) {
            if (element instanceof PsiJavaToken item) {
                IElementType elementType = item.getTokenType();
                if (List.of(JavaTokenType.ANDAND, JavaTokenType.OROR).contains(elementType)) {
                    if (!elementType.equals(prevOperand)) {
                        bag.increaseComplexity(toPointType(elementType));
                    }
                    prevOperand = elementType;
                }
            } else if (element instanceof PsiParenthesizedExpression) {
                calculateBinaryComplexity((PsiParenthesizedExpression) element);
                prevOperand = null;
            } else if (element instanceof PsiPrefixExpression) {
                calculateBinaryComplexity((PsiPrefixExpression) element);
                prevOperand = null;
            } else if (element instanceof PsiPolyadicExpression) {
                calculateBinaryComplexity((PsiPolyadicExpression) element);
            }
        }
    }

    private CogntiveComplexityElementType toPointType(IElementType elementType) {
        if (elementType == JavaTokenType.OROR) {
            return OR;
        } else if (elementType == JavaTokenType.ANDAND) {
            return AND;
        } else {
            return UNKNOWN;
        }
    }

    @Override
    public void visitSwitchStatement(@NotNull PsiSwitchStatement statement) {
        super.visitSwitchStatement(statement);
        bag.increaseComplexityAndNesting(SWITCH);
        postProcess(statement);
    }

    @Override
    public void visitWhileStatement(@NotNull PsiWhileStatement statement) {
        super.visitWhileStatement(statement);
        bag.increaseComplexityAndNesting(WHILE);
        postProcess(statement);
    }

    @Override
    public void visitCatchSection(PsiCatchSection section) {
        super.visitCatchSection(section);
        bag.increaseComplexityAndNesting(CATCH);
        postProcess(section);
    }

    @Override
    public void visitPolyadicExpression(PsiPolyadicExpression expression) {
        super.visitPolyadicExpression(expression);
        if (!(expression.getParent() instanceof PsiExpression)) {
            calculateBinaryComplexity(expression);
        }
        postProcess(expression);
    }

    public void postProcess(PsiElement element) {
        if (element instanceof PsiWhileStatement ||
                element instanceof PsiDoWhileStatement ||
                element instanceof PsiConditionalExpression ||
                element instanceof PsiForStatement ||
                element instanceof PsiForeachStatement ||
                element instanceof PsiCatchSection ||
                element instanceof PsiSwitchStatement ||
                element instanceof PsiLambdaExpression) {
            bag.decreaseNesting();
        } else if (element instanceof PsiIfStatement && !isElseIf((PsiIfStatement) element)) {
            bag.decreaseNesting();
        }
    }

    public long getMethodCognitiveComplexity() {
        return bag.getComplexity();
    }
}
