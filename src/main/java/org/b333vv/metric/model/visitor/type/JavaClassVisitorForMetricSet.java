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
import org.b333vv.metric.model.code.JavaClass;
import org.b333vv.metric.model.metric.Metric;

import java.util.List;

public class JavaClassVisitorForMetricSet extends JavaClassVisitor {

    protected List<Metric> metrics;

    public void visitJavaClass(JavaClass javaClass) {
        PsiClass psiClass = javaClass.getPsiClass();
        visitClass(psiClass);
        metrics.forEach(javaClass::addMetric);
    }
}
