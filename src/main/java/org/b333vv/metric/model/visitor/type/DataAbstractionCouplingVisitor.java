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
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiType;
import com.intellij.psi.util.PsiUtil;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.util.ClassUtils;
import org.b333vv.metric.model.metric.value.Value;

import java.util.HashSet;
import java.util.Set;

public class DataAbstractionCouplingVisitor extends JavaClassVisitor {
    @Override
    public void visitClass(PsiClass psiClass) {
        super.visitClass(psiClass);
        metric = Metric.of("DAC", "Data Abstraction Coupling",
                "/html/DataAbstractionCoupling.html", Value.UNDEFINED);
        final Set<PsiClass> psiClasses = new HashSet<>();
        final PsiField[] psiClassFields = psiClass.getFields();
        for (final PsiField psiField : psiClassFields) {
            final PsiType psiType = psiField.getType().getDeepComponentType();
            final PsiClass resolvedClassInType = PsiUtil.resolveClassInType(psiType);
            if (resolvedClassInType != null) {
                psiClasses.add(resolvedClassInType);
            }
        }
        if (ClassUtils.isConcrete(psiClass)) {
            metric = Metric.of("DAC", "Data Abstraction Coupling",
                    "/html/DataAbstractionCoupling.html", psiClasses.size());
        }
    }
}