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

package org.b333vv.metric.model.code;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElementVisitor;
import org.b333vv.metric.model.visitor.type.JavaClassVisitor;

import java.util.Objects;
import java.util.stream.Stream;

public class JavaClass extends JavaCode {
    private final PsiClass psiClass;

    public JavaClass(PsiClass aClass) {
        super(aClass.getName());
        this.psiClass = aClass;
    }

    public void addClass(JavaClass javaClass) {
        addChild(javaClass);
    }

    public Stream<JavaMethod> getMethods() {
        return children.stream()
                .filter(c -> c instanceof JavaMethod)
                .map(c -> (JavaMethod) c);
    }

    public Stream<JavaClass> getClasses() {
        return children.stream()
                .filter(c -> c instanceof JavaClass)
                .map(c -> (JavaClass) c);
    }

    public void addMethod(JavaMethod javaMethod) {
        addChild(javaMethod);
    }

    @Override
    public String toString() {
        return "Type(" + this.getName() + ")";
    }

    public PsiClass getPsiClass() {
        return psiClass;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JavaClass)) return false;
        if (!super.equals(o)) return false;
        JavaClass javaClass = (JavaClass) o;
        return Objects.equals(getPsiClass(), javaClass.getPsiClass());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getPsiClass());
    }

    @Override
    public void accept(PsiElementVisitor visitor) {
        if (visitor instanceof JavaClassVisitor) {
            ((JavaClassVisitor) visitor).visitJavaClass(this);
        }
    }
}
