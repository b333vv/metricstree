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
import org.b333vv.metric.model.code.ClassElement;
import org.b333vv.metric.model.code.FileElement;
import org.b333vv.metric.model.visitor.method.JavaMethodVisitor;
import org.b333vv.metric.model.visitor.type.JavaClassVisitor;
import org.b333vv.metric.ui.settings.composition.MetricsTreeSettingsStub;
import org.b333vv.metric.util.SettingsService;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

public class ClassModelBuilder extends ModelBuilder {

    protected final Project project;
    public ClassModelBuilder(Project project) {
        this.project = project;
    }
    public FileElement buildJavaFile(@NotNull PsiJavaFile psiJavaFile) {
        return createJavaFile(psiJavaFile);
    }

    @Deprecated
    @Override
    protected Stream<JavaRecursiveElementVisitor> classVisitors() {
        return project.getService(SettingsService.class)
                .getClassMetricsTreeSettings()
                .getMetricsList().stream()
                .filter(MetricsTreeSettingsStub::isNeedToConsider)
                .map(m -> m.getType().visitor())
                .filter(m -> m instanceof JavaClassVisitor);
    }

    @Deprecated
    @Override
    protected Stream<JavaRecursiveElementVisitor> methodVisitors() {

        return project.getService(SettingsService.class)
                .getClassMetricsTreeSettings()
                .getMetricsList().stream()
                .filter(MetricsTreeSettingsStub::isNeedToConsider)
                .map(m -> m.getType().visitor())
                .filter(m -> m instanceof JavaMethodVisitor);
    }

    @Override
    protected void addToAllClasses(ClassElement javaClass) {}
}