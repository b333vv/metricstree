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
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import org.b333vv.metric.model.code.ClassElement;
import org.b333vv.metric.model.code.FileElement;
import org.b333vv.metric.model.visitor.method.JavaMethodVisitor;
import org.b333vv.metric.model.visitor.type.JavaClassVisitor;
import org.b333vv.metric.ui.settings.composition.MetricsTreeSettingsStub;
import org.b333vv.metric.util.SettingsService;
import org.jetbrains.annotations.NotNull;
// Do NOT import kotlin PSI here to avoid runtime classloading issues when Kotlin plugin is absent

import java.util.stream.Stream;

public class ClassModelBuilder extends ModelBuilder {

    protected final Project project;
    public ClassModelBuilder(Project project) {
        this.project = project;
    }
    public FileElement buildJavaFile(@NotNull PsiJavaFile psiJavaFile) {
        return createJavaFile(psiJavaFile);
    }

    public FileElement buildFile(@NotNull PsiFile psiFile) {
        if (psiFile instanceof PsiJavaFile) {
            return buildJavaFile((PsiJavaFile) psiFile);
        }
        // Reflectively handle Kotlin file via KotlinModelBuilder to avoid touching ModelBuilder's methods
        try {
            if ("Kotlin".equals(psiFile.getFileType().getName()) || "KOTLIN".equals(psiFile.getFileType().getName())) {
                project.getMessageBus().syncPublisher(org.b333vv.metric.event.MetricsEventListener.TOPIC)
                        .printInfo("[ClassModelBuilder] Kotlin file detected: " + psiFile.getName());
                Class<?> kmbClass = Class.forName("org.b333vv.metric.builder.KotlinModelBuilder");
                java.lang.reflect.Constructor<?> ctor = kmbClass.getConstructor(Project.class);
                Object kmb = ctor.newInstance(project);

                Class<?> paramType = psiFile.getClass();
                java.lang.reflect.Method target = null;
                for (java.lang.reflect.Method method : kmbClass.getDeclaredMethods()) {
                    if (method.getName().equals("createKotlinFile") && method.getParameterCount() == 1) {
                        Class<?> p = method.getParameterTypes()[0];
                        if (p.isAssignableFrom(paramType)) {
                            target = method;
                            break;
                        }
                    }
                }
                if (target != null) {
                    project.getMessageBus().syncPublisher(org.b333vv.metric.event.MetricsEventListener.TOPIC)
                            .printInfo("[ClassModelBuilder] Invoking KotlinModelBuilder.createKotlinFile via reflection");
                    target.setAccessible(true);
                    Object res = target.invoke(kmb, psiFile);
                    if (res == null) {
                        project.getMessageBus().syncPublisher(org.b333vv.metric.event.MetricsEventListener.TOPIC)
                                .printInfo("[ClassModelBuilder] createKotlinFile returned null for: " + psiFile.getName());
                    }
                    return (FileElement) res;
                }
                project.getMessageBus().syncPublisher(org.b333vv.metric.event.MetricsEventListener.TOPIC)
                        .printInfo("[ClassModelBuilder] createKotlinFile method not found on KotlinModelBuilder");
            }
        } catch (ClassNotFoundException e) {
            // Kotlin builder not available -> Kotlin plugin/classes missing; safely ignore
            project.getMessageBus().syncPublisher(org.b333vv.metric.event.MetricsEventListener.TOPIC)
                    .printInfo("[ClassModelBuilder] KotlinModelBuilder not found in classloader");
        } catch (NoClassDefFoundError | LinkageError e) {
            // Kotlin PSI or transitive deps missing in classloader; safely ignore for non-Java files
            project.getMessageBus().syncPublisher(org.b333vv.metric.event.MetricsEventListener.TOPIC)
                    .printInfo("[ClassModelBuilder] Linkage error during Kotlin reflection: " + e.getClass().getSimpleName());
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to build Kotlin file via reflection", e);
        }
        return null;
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