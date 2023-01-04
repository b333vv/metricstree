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
import org.b333vv.metric.model.code.JavaFile;
import org.b333vv.metric.model.code.JavaMethod;
import org.b333vv.metric.model.visitor.method.HalsteadMethodVisitor;
import org.b333vv.metric.model.visitor.type.HalsteadClassVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

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
            addToAllClasses(javaClass);
        }
        return javaFile;
    }

    protected void buildConstructors(JavaClass javaClass) {
        for (PsiMethod aConstructor : javaClass.getPsiClass().getConstructors()) {
            JavaMethod javaMethod = new JavaMethod(aConstructor);
            javaClass.addMethod(javaMethod);
            methodVisitors().forEach(javaMethod::accept);

            HalsteadMethodVisitor halsteadMethodVisitor = new HalsteadMethodVisitor();
            javaMethod.accept(halsteadMethodVisitor);
        }
    }

    protected void buildMethods(JavaClass javaClass) {
        for (PsiMethod aMethod : javaClass.getPsiClass().getMethods()) {
            JavaMethod javaMethod = new JavaMethod(aMethod);
            javaClass.addMethod(javaMethod);
            methodVisitors().forEach(javaMethod::accept);

            HalsteadMethodVisitor halsteadMethodVisitor = new HalsteadMethodVisitor();
            javaMethod.accept(halsteadMethodVisitor);
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
            buildInnerClasses(psiClass, javaClass);
        }
    }

    abstract protected void addToAllClasses(JavaClass javaClass);

    abstract protected Stream<JavaRecursiveElementVisitor> classVisitors();

    abstract protected Stream<JavaRecursiveElementVisitor> methodVisitors();
}