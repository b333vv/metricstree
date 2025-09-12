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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.psi.KtClassOrObject;

import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Stream;

public class ClassElement extends CodeElement {
    private final PsiClass psiClass;
    private final KtClassOrObject ktClass;

    public ClassElement(@NotNull PsiClass psiClass) {
        super(Objects.requireNonNull(psiClass.getName()));
        this.psiClass = psiClass;
        this.ktClass = null;
    }

    public ClassElement(@NotNull KtClassOrObject ktClass) {
        super(Objects.requireNonNull(ktClass.getName()));
        this.psiClass = null;
        this.ktClass = ktClass;
    }

    public void addClass(@NotNull ClassElement javaClass) {
        addChild(javaClass);
    }

    public Stream<MethodElement> methods() {
        return children.stream()
                .filter(c -> c instanceof MethodElement)
                .map(c -> (MethodElement) c)
                .sorted(Comparator.comparing(CodeElement::getName));
    }

    public Stream<ClassElement> innerClasses() {
        return children.stream()
                .filter(c -> c instanceof ClassElement)
                .map(c -> (ClassElement) c)
                .sorted(Comparator.comparing(CodeElement::getName));
    }

    public void addMethod(@NotNull MethodElement javaMethod) {
        addChild(javaMethod);
    }

    @Override
    public String toString() {
        return this.getName();
    }

    public @Nullable PsiClass getPsiClass() { return psiClass; }

    public @Nullable KtClassOrObject getKtClassOrObject() { return ktClass; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClassElement)) return false;
        if (!super.equals(o)) return false;
        ClassElement javaClass = (ClassElement) o;
        return Objects.equals(getPsiClass(), javaClass.getPsiClass())
                && Objects.equals(getKtClassOrObject(), javaClass.getKtClassOrObject());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getPsiClass(), getKtClassOrObject());
    }

    @Override
    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof JavaClassVisitor) {
            ((JavaClassVisitor) visitor).visitJavaClass(this);
        }
    }
}
