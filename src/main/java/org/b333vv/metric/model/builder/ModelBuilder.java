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

package org.b333vv.metric.model.builder;

import com.intellij.psi.JavaRecursiveElementVisitor;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiMethod;
import org.apache.commons.io.FilenameUtils;
import org.b333vv.metric.model.code.JavaClass;
import org.b333vv.metric.model.code.JavaFile;
import org.b333vv.metric.model.code.JavaMethod;
import org.b333vv.metric.model.code.JavaPackage;
import org.b333vv.metric.util.MetricsService;
import org.b333vv.metric.util.MetricsUtils;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

public abstract class ModelBuilder {

    protected void createJavaClass(@NotNull JavaPackage javaPackage, @NotNull PsiJavaFile psiJavaFile) {
        PsiClass[] psiClasses = psiJavaFile.getClasses();
        if (psiClasses.length > 1) {
            JavaFile javaFile = new JavaFile(psiJavaFile.getName());
            javaPackage.addFile(javaFile);
            for (PsiClass psiClass : psiClasses) {
                JavaClass javaClass = new JavaClass(psiClass);
                getJavaClassVisitors().forEach(javaClass::accept);
                javaFile.addClass(javaClass);
                buildConstructors(javaClass);
                buildMethods(javaClass);
                buildInnerClasses(psiClass, javaClass);
                addToAllClasses(javaClass);
            }
        } else {
            JavaClass javaClass = new JavaClass(psiClasses[0]);
            getJavaClassVisitors().forEach(javaClass::accept);
            javaPackage.addClass(javaClass);
            buildConstructors(javaClass);
            buildMethods(javaClass);
            buildInnerClasses(psiClasses[0], javaClass);
            addToAllClasses(javaClass);
        }
    }

    protected void buildConstructors(JavaClass javaClass) {
        for (PsiMethod aConstructor : javaClass.getPsiClass().getConstructors()) {
            JavaMethod javaMethod = new JavaMethod(aConstructor);
            javaClass.addMethod(javaMethod);
            getJavaMethodVisitors().forEach(javaMethod::accept);
        }
    }

    protected void buildMethods(JavaClass javaClass) {
        for (PsiMethod aMethod : javaClass.getPsiClass().getMethods()) {
            JavaMethod javaMethod = new JavaMethod(aMethod);
            javaClass.addMethod(javaMethod);
            getJavaMethodVisitors().forEach(javaMethod::accept);
        }
    }

    protected void buildInnerClasses(PsiClass aClass, JavaClass parentClass) {
        for (PsiClass psiClass : aClass.getInnerClasses()) {
            JavaClass javaClass = new JavaClass(psiClass);
            parentClass.addClass(javaClass);
            getJavaClassVisitors().forEach(javaClass::accept);
            buildConstructors(javaClass);
            buildMethods(javaClass);
            addToAllClasses(javaClass);
            buildInnerClasses(psiClass, javaClass);
        }
    }

    protected void addToAllClasses(JavaClass javaClass) {}

    protected Stream<JavaRecursiveElementVisitor> getJavaClassVisitors() {
        return MetricsService.getJavaClassVisitorsForClassMetricsTree();
    }
    protected Stream<JavaRecursiveElementVisitor> getJavaMethodVisitors() {
        return MetricsService.getJavaMethodVisitorsForClassMetricsTree();
    }
}