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

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiCodeBlock;
import com.intellij.psi.PsiStatement;
import com.intellij.psi.PsiReturnStatement;
import com.intellij.psi.PsiExpressionStatement;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.PsiAssignmentExpression;
import com.intellij.psi.PsiReferenceExpression;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.util.PropertyUtil;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.util.ClassUtils;
import org.b333vv.metric.model.metric.value.Value;

import static org.b333vv.metric.model.metric.MetricType.WOC;

public class WeightOfAClassVisitor extends JavaClassVisitor {

    @Override
    public void visitClass(PsiClass psiClass) {
        metric = Metric.of(WOC, Value.UNDEFINED);
        if (ClassUtils.isConcrete(psiClass)) {
            // Denominator: all declared methods (any visibility, static or instance), excluding constructors
            PsiMethod[] declaredMethods = psiClass.getMethods();
            long totalMethods = java.util.Arrays.stream(declaredMethods)
                    .filter(m -> !m.isConstructor())
                    .count();

            // Numerator: functional methods only
            long functionalMethods = java.util.Arrays.stream(declaredMethods)
                    .filter(m -> !m.isConstructor())
                    .filter(this::isFunctional)
                    .count();

            if (totalMethods == 0L) {
                metric = Metric.of(WOC, 0.00);
            } else {
                metric = Metric.of(WOC, Value.of((double) functionalMethods)
                        .divide(Value.of((double) totalMethods)));
            }
        }
    }

    private boolean isFunctional(PsiMethod m) {
        // Exclude accessors and boilerplate from functional set
        if (PropertyUtil.isSimpleGetter(m) || PropertyUtil.isSimpleSetter(m)) return false;
        if (CohesionUtils.getBoilerplateMethods().contains(m.getName())) return false;
        // Exclude trivial/empty/one-liner delegations
        if (isTrivial(m)) return false;
        return true;
    }

    private boolean isTrivial(PsiMethod m) {
        // Abstract/interface methods have no body -> treat as non-functional
        final PsiCodeBlock body = m.getBody();
        if (body == null) return true;
        PsiStatement[] statements = body.getStatements();
        if (statements.length == 0) return true; // empty body
        if (statements.length > 1) return false; // more than one statement -> treat as non-trivial

        PsiStatement s = statements[0];
        if (s instanceof PsiReturnStatement) {
            PsiExpression rv = ((PsiReturnStatement) s).getReturnValue();
            if (rv instanceof PsiReferenceExpression) return true; // return x;
            if (rv instanceof PsiMethodCallExpression) return true; // return foo();
            return false;
        }
        if (s instanceof PsiExpressionStatement) {
            PsiExpression expr = ((PsiExpressionStatement) s).getExpression();
            if (expr instanceof PsiMethodCallExpression) return true; // foo();
            if (expr instanceof PsiAssignmentExpression) {
                PsiAssignmentExpression ae = (PsiAssignmentExpression) expr;
                if (ae.getLExpression() instanceof PsiReferenceExpression && ae.getRExpression() instanceof PsiReferenceExpression) {
                    return true; // x = y;
                }
            }
        }
        return false;
    }
}