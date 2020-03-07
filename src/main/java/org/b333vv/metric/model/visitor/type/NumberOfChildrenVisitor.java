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
import com.intellij.psi.PsiModifier;
import com.intellij.psi.search.searches.ClassInheritorsSearch;
import org.b333vv.metric.model.metric.value.Value;
import org.b333vv.metric.model.metric.Metric;

public class NumberOfChildrenVisitor extends JavaClassVisitor {
    @Override
    public void visitClass(PsiClass psiClass) {
        super.visitClass(psiClass);
        metric = Metric.of("NOC", "Number Of Children",
                "/html/NumberOfChildren.html", Value.UNDEFINED);
        if (!(psiClass.hasModifierProperty(PsiModifier.FINAL) ||
                psiClass.isInterface() ||
                psiClass.isEnum()
        )) {
            metric = Metric.of("NOC", "Number Of Children", "/html/NumberOfChildren.html",
                    ClassInheritorsSearch.search(psiClass, false).findAll().size());
        }
    }
}