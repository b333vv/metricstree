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

import com.intellij.psi.JavaRecursiveElementVisitor;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiMethod;
import org.b333vv.metric.model.code.JavaClass;
import org.b333vv.metric.model.code.JavaCode;
import org.b333vv.metric.model.code.JavaFile;
import org.b333vv.metric.model.code.JavaMethod;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.value.Value;
import org.b333vv.metric.model.visitor.method.HalsteadMethodVisitor;
import org.b333vv.metric.model.visitor.type.HalsteadClassVisitor;
import org.b333vv.metric.util.MetricsUtils;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

import static org.b333vv.metric.model.metric.MetricType.*;

public abstract class ModelBuilder {

    protected JavaFile createJavaFile(@NotNull PsiJavaFile psiJavaFile) {
        JavaFile javaFile = new JavaFile(psiJavaFile.getName());
        for (PsiClass psiClass : psiJavaFile.getClasses()) {
            JavaClass javaClass = new JavaClass(psiClass);
            classVisitors().forEach(javaClass::accept);

            HalsteadClassVisitor halsteadClassVisitor = new HalsteadClassVisitor();
            javaClass.accept(halsteadClassVisitor);

            javaFile.addClass(javaClass);
            buildConstructors(javaClass);
            buildMethods(javaClass);
            buildInnerClasses(psiClass, javaClass);

            addMaintainabilityIndexForClass(javaClass);
            addLinesOfCodeIndexForClass(javaClass);

            addToAllClasses(javaClass);
        }
        return javaFile;
    }

    protected void buildConstructors(JavaClass javaClass) {
        for (PsiMethod aConstructor : javaClass.getPsiClass().getConstructors()) {
            JavaMethod javaMethod = new JavaMethod(aConstructor, javaClass);
            javaClass.addMethod(javaMethod);
            methodVisitors().forEach(javaMethod::accept);

            HalsteadMethodVisitor halsteadMethodVisitor = new HalsteadMethodVisitor();
            javaMethod.accept(halsteadMethodVisitor);

            addMaintainabilityIndexForMethod(javaMethod);
        }
    }

    protected void buildMethods(JavaClass javaClass) {
        for (PsiMethod aMethod : javaClass.getPsiClass().getMethods()) {
            JavaMethod javaMethod = new JavaMethod(aMethod, javaClass);
            javaClass.addMethod(javaMethod);
            methodVisitors().forEach(javaMethod::accept);

            HalsteadMethodVisitor halsteadMethodVisitor = new HalsteadMethodVisitor();
            javaMethod.accept(halsteadMethodVisitor);

            addMaintainabilityIndexForMethod(javaMethod);
        }
    }

    protected void buildInnerClasses(PsiClass aClass, JavaClass parentClass) {
        for (PsiClass psiClass : aClass.getInnerClasses()) {
            JavaClass javaClass = new JavaClass(psiClass);
            parentClass.addClass(javaClass);
            classVisitors().forEach(javaClass::accept);

            HalsteadClassVisitor halsteadClassVisitor = new HalsteadClassVisitor();
            javaClass.accept(halsteadClassVisitor);

            buildConstructors(javaClass);
            buildMethods(javaClass);
            addToAllClasses(javaClass);

            addMaintainabilityIndexForClass(javaClass);
            addLinesOfCodeIndexForClass(javaClass);

            buildInnerClasses(psiClass, javaClass);
        }
    }

    void addMaintainabilityIndexForClass(JavaClass javaClass) {
        long cyclomaticComplexity = javaClass.methods().flatMap(JavaMethod::metrics)
                .filter(metric -> metric.getType() == CC)
                .map(Metric::getValue)
                .reduce(Value::plus)
                .orElse(Value.ZERO)
                .longValue();
        long linesOfCode = javaClass.methods().flatMap(JavaMethod::metrics)
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

    void addLinesOfCodeIndexForClass(JavaClass javaClass) {
        long linesOfCode = javaClass.methods()
                .map(javaMethod ->  javaMethod.metric(LOC).getValue())
                .reduce(Value::plus)
                .orElse(Value.ZERO)
                .longValue();

        javaClass.addMetric(Metric.of(MetricType.CLOC, linesOfCode));
    }

    private void addMaintainabilityIndexForMethod(JavaMethod javaMethod) {
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

    abstract protected void addToAllClasses(JavaClass javaClass);

    abstract protected Stream<JavaRecursiveElementVisitor> classVisitors();

    abstract protected Stream<JavaRecursiveElementVisitor> methodVisitors();
}