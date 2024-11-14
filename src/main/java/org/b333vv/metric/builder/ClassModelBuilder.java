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

package org.b333vv.metric.builder;

import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaRecursiveElementVisitor;
import com.intellij.psi.PsiJavaFile;
import org.b333vv.metric.model.code.JavaClass;
import org.b333vv.metric.model.code.JavaFile;
import org.b333vv.metric.util.MetricsService;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

public class ClassModelBuilder extends ModelBuilder {


    public JavaFile buildJavaFile(@NotNull PsiJavaFile psiJavaFile) {
        return createJavaFile(psiJavaFile);
    }

    @Deprecated
    @Override
    protected Stream<JavaRecursiveElementVisitor> classVisitors() {
        return MetricsService.classVisitorsForClassMetricsTree();
    }

    @Deprecated
    @Override
    protected Stream<JavaRecursiveElementVisitor> methodVisitors() {

        return MetricsService.methodsVisitorsForClassMetricsTree();
    }

    @Override
    protected void addToAllClasses(JavaClass javaClass) {}
}