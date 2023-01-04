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

package org.b333vv.metric.model.util;

import com.intellij.psi.*;

public class ExpressionUtils {

    private ExpressionUtils() {
        // Utility class
    }
    
    public static boolean isCompileTimeCalculation(PsiExpression expression) {
        if (expression instanceof PsiLiteralExpression) {
            return true;
        }
        if (expression instanceof PsiBinaryExpression) {
            final PsiBinaryExpression binaryExpression =
                    (PsiBinaryExpression) expression;
            final PsiExpression lhs = binaryExpression.getLOperand();
            final PsiExpression rhs = binaryExpression.getROperand();
            return isCompileTimeCalculation(lhs) &&
                    isCompileTimeCalculation(rhs);
        }
        if (expression instanceof PsiPrefixExpression) {
            final PsiPrefixExpression prefixExpression =
                    (PsiPrefixExpression) expression;
            final PsiExpression operand = prefixExpression.getOperand();
            return isCompileTimeCalculation(operand);
        }
        if (expression instanceof PsiReferenceExpression) {
            final PsiReferenceExpression referenceExpression =
                    (PsiReferenceExpression) expression;
            final PsiElement qualifier = referenceExpression.getQualifier();
            if (qualifier instanceof PsiThisExpression) {
                return false;
            }
            final PsiElement element = referenceExpression.resolve();
            if (element instanceof PsiField) {
                final PsiField field = (PsiField) element;
                final PsiExpression initializer = field.getInitializer();
                return field.hasModifierProperty(PsiModifier.FINAL) &&
                        isCompileTimeCalculation(initializer);
            }
            if (element instanceof PsiVariable) {
                final PsiVariable variable = (PsiVariable) element;
                final PsiExpression initializer = variable.getInitializer();
                return variable.hasModifierProperty(PsiModifier.FINAL) &&
                        isCompileTimeCalculation(initializer);
            }
        }
        if (expression instanceof PsiParenthesizedExpression) {
            final PsiParenthesizedExpression parenthesizedExpression =
                    (PsiParenthesizedExpression) expression;
            final PsiExpression unparenthesizedExpression =
                    parenthesizedExpression.getExpression();
            return isCompileTimeCalculation(unparenthesizedExpression);
        }
        if (expression instanceof PsiConditionalExpression) {
            final PsiConditionalExpression conditionalExpression =
                    (PsiConditionalExpression) expression;
            final PsiExpression condition =
                    conditionalExpression.getCondition();
            final PsiExpression thenExpression =
                    conditionalExpression.getThenExpression();
            final PsiExpression elseExpression =
                    conditionalExpression.getElseExpression();
            return isCompileTimeCalculation(condition) &&
                    isCompileTimeCalculation(thenExpression) &&
                    isCompileTimeCalculation(elseExpression);
        }
        if (expression instanceof PsiTypeCastExpression) {
            final PsiTypeCastExpression typeCastExpression =
                    (PsiTypeCastExpression) expression;
            final PsiExpression operand = typeCastExpression.getOperand();
            return isCompileTimeCalculation(operand);
        }
        return false;
    }
}
