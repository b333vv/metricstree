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

import com.intellij.psi.PsiJvmMember;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiMethodCallExpression;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.value.Value;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.b333vv.metric.model.metric.MetricType.CDISP;

public class CouplingDispersionVisitor extends JavaMethodVisitor {
    private Set<PsiMethod> usedMethods = new HashSet<>();

    @Override
    public void visitMethod(PsiMethod method) {
        usedMethods.clear();
        metric = Metric.of(CDISP, Value.UNDEFINED);
        super.visitMethod(method);
        int usedMethodsNumber = usedMethods.size();
        // Create a defensive copy to avoid ConcurrentModificationException
        Set<PsiMethod> usedMethodsCopy = new HashSet<>(usedMethods);
        long classesNumber = usedMethodsCopy.stream()
                .map(PsiJvmMember::getContainingClass)
                .filter(c -> c != null && !c.equals(method.getContainingClass()))
                .collect(Collectors.toSet())
                .size();
        if (usedMethodsNumber == 0) {
            metric = Metric.of(CDISP, 0.0);
        } else {
            metric = Metric.of(CDISP, Value.of((double) classesNumber)
                    .divide(Value.of((double) usedMethodsNumber)));
        }
    }

    @Override
    public void visitMethodCallExpression(PsiMethodCallExpression psiMethodCallExpression) {
        super.visitMethodCallExpression(psiMethodCallExpression);
        final PsiMethod method = psiMethodCallExpression.resolveMethod();
        if (method == null) {
            return;
        }
        usedMethods.add(method);
    }
}