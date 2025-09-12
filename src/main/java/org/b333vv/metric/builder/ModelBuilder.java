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
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiMethod;
import org.b333vv.metric.model.code.ClassElement;
import org.b333vv.metric.model.code.FileElement;
import org.b333vv.metric.model.code.MethodElement;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.value.Value;
import org.b333vv.metric.model.visitor.method.HalsteadMethodVisitor;
import org.b333vv.metric.model.visitor.method.JavaMethodVisitor;
import org.b333vv.metric.model.visitor.type.HalsteadClassVisitor;
import org.b333vv.metric.model.visitor.type.JavaClassVisitor;
import org.b333vv.metric.ui.settings.composition.MetricsTreeSettingsStub;
import org.jetbrains.annotations.NotNull;
import org.b333vv.metric.util.SettingsService;

import java.util.List;
import java.util.stream.Stream;

import static org.b333vv.metric.model.metric.MetricType.*;

public abstract class ModelBuilder {

    protected List<JavaClassVisitor> javaClassVisitorList = null;
    protected List<JavaMethodVisitor> javaMethodVisitorList = null;

    protected List<JavaClassVisitor> getClassVisitorList(Project project) {
        if (javaClassVisitorList == null) {
            javaClassVisitorList = project.getService(SettingsService.class).getClassMetricsTreeSettings().getMetricsList().stream()
                    .filter(MetricsTreeSettingsStub::isNeedToConsider)
                    .map(m -> m.getType().visitor())
                    .filter(m -> m instanceof JavaClassVisitor)
                    .map(m -> (JavaClassVisitor) m)
                    .toList();
        }
        return javaClassVisitorList;
    }

    protected List<JavaMethodVisitor> getMethodVisitorList(Project project) {
        if (javaMethodVisitorList == null) {
            javaMethodVisitorList = project.getService(SettingsService.class).getClassMetricsTreeSettings().getMetricsList().stream()
                    .filter(MetricsTreeSettingsStub::isNeedToConsider)
                    .map(m -> m.getType().visitor())
                    .filter(m -> m instanceof JavaMethodVisitor)
                    .map(m -> (JavaMethodVisitor) m)
                    .toList();
        }
        return javaMethodVisitorList;
    }

    protected FileElement createJavaFile(@NotNull PsiJavaFile psiJavaFile) {
        FileElement javaFile = new FileElement(psiJavaFile.getName());
        Project project = psiJavaFile.getProject();
        for (PsiClass psiClass : psiJavaFile.getClasses()) {
            ClassElement javaClass = new ClassElement(psiClass);

            getClassVisitorList(project).forEach(javaClass::accept);

            HalsteadClassVisitor halsteadClassVisitor = new HalsteadClassVisitor();
            javaClass.accept(halsteadClassVisitor);

            javaFile.addClass(javaClass);
            buildConstructors(javaClass);
            buildMethods(javaClass);
            buildInnerClasses(psiClass, javaClass);

            addMaintainabilityIndexForClass(javaClass);
            addLinesOfCodeIndexForClass(javaClass);

            addCognitiveComplexityForClass(javaClass);


            addToAllClasses(javaClass);
        }
        return javaFile;
    }

    protected void buildConstructors(ClassElement javaClass) {
        Project project = javaClass.getPsiClass().getProject();
        for (PsiMethod aConstructor : javaClass.getPsiClass().getConstructors()) {
            MethodElement javaMethod = new MethodElement(aConstructor, javaClass);
            javaClass.addMethod(javaMethod);

            getMethodVisitorList(project).forEach(javaMethod::accept);

            HalsteadMethodVisitor halsteadMethodVisitor = new HalsteadMethodVisitor();
            javaMethod.accept(halsteadMethodVisitor);

            addMaintainabilityIndexForMethod(javaMethod);
        }
    }

    protected void buildMethods(ClassElement javaClass) {
        Project project = javaClass.getPsiClass().getProject();
        for (PsiMethod aMethod : javaClass.getPsiClass().getMethods()) {
            MethodElement javaMethod = new MethodElement(aMethod, javaClass);
            javaClass.addMethod(javaMethod);

            getMethodVisitorList(project).forEach(javaMethod::accept);

            HalsteadMethodVisitor halsteadMethodVisitor = new HalsteadMethodVisitor();
            javaMethod.accept(halsteadMethodVisitor);

            addMaintainabilityIndexForMethod(javaMethod);
        }
    }

    protected void buildInnerClasses(PsiClass aClass, ClassElement parentClass) {
        Project project = aClass.getProject();
        for (PsiClass psiClass : aClass.getInnerClasses()) {
            ClassElement javaClass = new ClassElement(psiClass);
            parentClass.addClass(javaClass);

            getClassVisitorList(project).forEach(javaClass::accept);

            HalsteadClassVisitor halsteadClassVisitor = new HalsteadClassVisitor();
            javaClass.accept(halsteadClassVisitor);

            buildConstructors(javaClass);
            buildMethods(javaClass);
            addToAllClasses(javaClass);

            addMaintainabilityIndexForClass(javaClass);
            addLinesOfCodeIndexForClass(javaClass);

            addCognitiveComplexityForClass(javaClass);

            buildInnerClasses(psiClass, javaClass);
        }
    }

    void addMaintainabilityIndexForClass(ClassElement javaClass) {
        long cyclomaticComplexity = javaClass.methods().flatMap(MethodElement::metrics)
                .filter(metric -> metric.getType() == CC)
                .map(Metric::getValue)
                .reduce(Value::plus)
                .orElse(Value.ZERO)
                .longValue();
        long linesOfCode = javaClass.methods().flatMap(MethodElement::metrics)
                .filter(metric -> metric.getType() == LOC)
                .map(Metric::getValue)
                .reduce(Value::plus)
                .orElse(Value.ZERO)
                .longValue();
        double halsteadVolume = javaClass.metrics()
                .filter(metric -> metric.getType() == CHVL)
                .map(Metric::getValue)
                .reduce(Value::plus)
                .orElse(Value.ZERO)
                .longValue();

        double maintainabilityIndex = 0.0;
        if (cyclomaticComplexity > 0L && linesOfCode > 0L) {
            maintainabilityIndex = Math.max(0, (171 - 5.2 * Math.log(halsteadVolume)
                    - 0.23 * Math.log(cyclomaticComplexity) - 16.2 * Math.log(linesOfCode)) * 100 / 171);
        }

        javaClass.addMetric(Metric.of(MetricType.CMI, maintainabilityIndex));
    }

    void addLinesOfCodeIndexForClass(ClassElement javaClass) {
        long linesOfCode = javaClass.methods()
                .map(javaMethod ->  javaMethod.metric(LOC).getPsiValue())
                .reduce(Value::plus)
                .orElse(Value.ZERO)
                .longValue();

        javaClass.addMetric(Metric.of(MetricType.CLOC, linesOfCode));
    }

    void addCognitiveComplexityForClass(ClassElement javaClass) {
        long cognitiveComplexity = javaClass.methods()
                .map(javaMethod ->  javaMethod.metric(CCM).getPsiValue())
                .reduce(Value::plus)
                .orElse(Value.ZERO)
                .longValue();

        javaClass.addMetric(Metric.of(MetricType.CCC, cognitiveComplexity));
    }

    private void addMaintainabilityIndexForMethod(MethodElement javaMethod) {
        long cyclomaticComplexity = javaMethod.metrics()
                .filter(metric -> metric.getType() == CC)
                .map(Metric::getValue)
                .reduce(Value::plus)
                .orElse(Value.ZERO)
                .longValue();
        long linesOfCode = javaMethod.metrics()
                .filter(metric -> metric.getType() == LOC)
                .map(Metric::getValue)
                .reduce(Value::plus)
                .orElse(Value.ZERO)
                .longValue();
        double halsteadVolume = javaMethod.metrics()
                .filter(metric -> metric.getType() == HVL)
                .map(Metric::getValue)
                .reduce(Value::plus)
                .orElse(Value.ZERO)
                .longValue();

        double maintainabilityIndex = 0.0;
        if (cyclomaticComplexity > 0L && linesOfCode > 0L) {
            maintainabilityIndex = Math.max(0, (171 - 5.2 * Math.log(halsteadVolume)
                    - 0.23 * Math.log(cyclomaticComplexity) - 16.2 * Math.log(linesOfCode)) * 100 / 171);
        }

        javaMethod.addMetric(Metric.of(MetricType.MMI, maintainabilityIndex));
    }

    abstract protected void addToAllClasses(ClassElement javaClass);

    abstract protected Stream<JavaRecursiveElementVisitor> classVisitors();

    abstract protected Stream<JavaRecursiveElementVisitor> methodVisitors();
}