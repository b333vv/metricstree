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
import org.b333vv.metric.task.MetricTaskCache;
import org.b333vv.metric.builder.DependenciesBuilder;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.util.ClassUtils;
import org.b333vv.metric.model.metric.value.Value;
import org.b333vv.metric.util.MetricsUtils;

import java.util.HashSet;
import java.util.Set;

import static org.b333vv.metric.model.metric.MetricType.CBO;

public class CouplingBetweenObjectsVisitor extends JavaClassVisitor {
    @Override
    public void visitClass(PsiClass psiClass) {
        super.visitClass(psiClass);
        MetricsUtils.setCurrentProject(psiClass.getProject());
        metric = Metric.of(CBO, Value.UNDEFINED);
        if (ClassUtils.isConcrete(psiClass)) {
            DependenciesBuilder dependenciesBuilder = MetricTaskCache.instance()
                    .getUserData(MetricTaskCache.DEPENDENCIES);
            if (dependenciesBuilder != null) {
                Set<PsiClass> dependencies = dependenciesBuilder.getClassesDependencies(psiClass);
                Set<PsiClass> dependents = dependenciesBuilder.getClassesDependents(psiClass);
                Set<PsiClass> union = new HashSet<>(dependencies);
                union.addAll(dependents);
                metric = Metric.of(CBO, union.size());
            }
        }
    }
}