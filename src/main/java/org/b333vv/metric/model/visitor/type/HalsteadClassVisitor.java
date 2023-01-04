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

package org.b333vv.metric.model.visitor.type;

import com.intellij.psi.*;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.value.Value;
import org.b333vv.metric.model.util.ClassUtils;
import org.b333vv.metric.model.util.ExpressionUtils;
import org.b333vv.metric.model.util.MethodUtils;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import static org.b333vv.metric.model.metric.MetricType.*;
import static org.b333vv.metric.model.metric.MetricType.HL;

public class HalsteadClassVisitor extends JavaClassVisitorForMetricSet {

    public HalsteadClassVisitor() {
        metrics = new LinkedList<>();
    }

    private int numberOfOperands = 0;
    private int numberOfOperators = 0;
    private final Set<String> operators = new HashSet<>();
    private final Set<String> operands = new HashSet<>();
    private boolean inCompileTimeConstant = false;

    private int getNumberOfDistinctOperands() {
        return operands.size();
    }

    private int getNumberOfDistinctOperators() {
        return operators.size();
    }

    public long length() {
        return numberOfOperands + numberOfOperators;
    }

    public long vocabulary() {
        return operands.size() + operators.size();
    }

    public double difficulty() {
        final int N2 = numberOfOperands;
        final int n1 = getNumberOfDistinctOperators();
        final int n2 = getNumberOfDistinctOperands();
        return n2 == 0 ? 0.0 : ((double) n1 / 2.0) * ((double) N2 / (double) n2);
    }

    public double volume() {
        return (double) length() * Math.log(vocabulary()) / Math.log(2.0);
    }

    public double effort() {
        return difficulty() * volume();
    }

    public double errors() {
        return Math.pow(effort(), 2.0 / 3.0) / 3000.0;
    }

    @Override
    public void visitClass(PsiClass psiClass) {
        if (ClassUtils.isConcrete(psiClass)) {
            super.visitClass(psiClass);
            metrics.add(Metric.of(CHEF, effort()));
            metrics.add(Metric.of(CHER, errors()));
            metrics.add(Metric.of(CHVL, volume()));
            metrics.add(Metric.of(CHD, difficulty()));
            metrics.add(Metric.of(CHVC, vocabulary()));
            metrics.add(Metric.of(CHL, length()));
        } else {
            metrics.add(Metric.of(CHEF, Value.UNDEFINED));
            metrics.add(Metric.of(CHER, Value.UNDEFINED));
            metrics.add(Metric.of(CHVL, Value.UNDEFINED));
            metrics.add(Metric.of(CHD, Value.UNDEFINED));
            metrics.add(Metric.of(CHVC, Value.UNDEFINED));
            metrics.add(Metric.of(CHL, Value.UNDEFINED));
        }
    }

    @Override
    public void visitReferenceExpression(PsiReferenceExpression expression) {
        super.visitReferenceExpression(expression);

        final PsiElement element = expression.resolve();
        if (element instanceof PsiVariable) {
            final String expressionText = expression.getText();
            registerOperand(expressionText);
        }
    }

    @Override
    public void visitLiteralExpression(PsiLiteralExpression expression) {
        if (inCompileTimeConstant) {
            return;
        }
        inCompileTimeConstant = ExpressionUtils.isCompileTimeCalculation(expression);
        super.visitLiteralExpression(expression);
        final String text = expression.getText();
        registerOperand(text);
        inCompileTimeConstant = false;
    }

    @Override
    public void visitBinaryExpression(PsiBinaryExpression expression) {
        if (inCompileTimeConstant) {
            return;
        }
        if (ExpressionUtils.isCompileTimeCalculation(expression)) {
            inCompileTimeConstant = true;
            final String text = expression.getText();
            registerOperand(text);
        }
        super.visitBinaryExpression(expression);
        final PsiJavaToken sign = expression.getOperationSign();
        registerSign(sign);
        inCompileTimeConstant = false;
    }

    @Override
    public void visitPrefixExpression(PsiPrefixExpression expression) {
        if (inCompileTimeConstant) {
            return;
        }
        if (ExpressionUtils.isCompileTimeCalculation(expression)) {
            inCompileTimeConstant = true;
            final String text = expression.getText();
            registerOperand(text);
        }
        super.visitPrefixExpression(expression);
        final PsiJavaToken sign = expression.getOperationSign();

        registerSign(sign);
        inCompileTimeConstant = false;
    }

    @Override
    public void visitPostfixExpression(PsiPostfixExpression expression) {
        if (inCompileTimeConstant) {
            return;
        }
        if (ExpressionUtils.isCompileTimeCalculation(expression)) {
            inCompileTimeConstant = true;
            final String text = expression.getText();
            registerOperand(text);
        }
        super.visitPostfixExpression(expression);
        final PsiJavaToken sign = expression.getOperationSign();
        registerSign(sign);
        inCompileTimeConstant = false;
    }

    @Override
    public void visitKeyword(PsiKeyword psiKeyword) {
        super.visitKeyword(psiKeyword);
        registerSign(psiKeyword);
    }

    @Override
    public void visitMethodCallExpression(PsiMethodCallExpression callExpression) {
        super.visitMethodCallExpression(callExpression);
        final PsiMethod method = callExpression.resolveMethod();
        if (method != null) {
            final String signature = MethodUtils.calculateSignature(method);
            registerOperator(signature);
        }
    }

    private void registerSign(PsiJavaToken sign) {
        final String text = sign.getText();
        registerOperator(text);
    }

    private void registerOperator(String operator) {
        numberOfOperators++;
        operators.add(operator);
    }

    private void registerOperand(String operand) {
        numberOfOperands++;
        operands.add(operand);
    }
}
