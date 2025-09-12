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

import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiSubstitutor;
import com.intellij.psi.PsiType;
import com.intellij.psi.util.MethodSignature;
import org.b333vv.metric.model.visitor.method.JavaMethodVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.psi.KtNamedFunction;
import org.jetbrains.kotlin.psi.KtParameter;
import org.jetbrains.kotlin.psi.KtPrimaryConstructor;
import org.jetbrains.kotlin.psi.KtSecondaryConstructor;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public class MethodElement extends CodeElement {
    private final PsiMethod psiMethod;
    private final KtNamedFunction ktFunction;
    private final KtPrimaryConstructor ktPrimaryConstructor;
    private final KtSecondaryConstructor ktSecondaryConstructor;
    private final ClassElement javaClass;

    public MethodElement(PsiMethod psiMethod, ClassElement javaClass) {
        super(signature(psiMethod));
        this.psiMethod = psiMethod;
        this.ktFunction = null;
        this.ktPrimaryConstructor = null;
        this.ktSecondaryConstructor = null;
        this.javaClass = javaClass;
    }

    public MethodElement(@NotNull KtNamedFunction function, @NotNull ClassElement javaClass) {
        super(kotlinSignature(function));
        this.psiMethod = null;
        this.ktFunction = function;
        this.ktPrimaryConstructor = null;
        this.ktSecondaryConstructor = null;
        this.javaClass = javaClass;
    }

    public MethodElement(@NotNull KtPrimaryConstructor ctor, @NotNull ClassElement javaClass) {
        super(kotlinSignature(ctor));
        this.psiMethod = null;
        this.ktFunction = null;
        this.ktPrimaryConstructor = ctor;
        this.ktSecondaryConstructor = null;
        this.javaClass = javaClass;
    }

    public MethodElement(@NotNull KtSecondaryConstructor ctor, @NotNull ClassElement javaClass) {
        super(kotlinSignature(ctor));
        this.psiMethod = null;
        this.ktFunction = null;
        this.ktPrimaryConstructor = null;
        this.ktSecondaryConstructor = ctor;
        this.javaClass = javaClass;
    }

    public static String signature(PsiMethod aMethod) {
        StringBuilder signature = new StringBuilder();
        MethodSignature methodSignature = aMethod.getSignature(PsiSubstitutor.EMPTY);
        signature.append(aMethod.getName());
        signature.append("(");
        signature.append(Arrays.stream(
                methodSignature.getParameterTypes())
                .map(PsiType::getPresentableText)
                .collect(Collectors.joining(", ")));
        signature.append(")");
        return signature.toString();
    }

    private static String kotlinSignature(@NotNull KtNamedFunction function) {
        String name = function.getName() != null ? function.getName() : "<anonymous>";
        String params = function.getValueParameters().stream()
                .map(MethodElement::ktParamText)
                .collect(Collectors.joining(", "));
        return name + "(" + params + ")";
    }

    private static String kotlinSignature(@NotNull KtPrimaryConstructor ctor) {
        String name = "<init>";
        String params = ctor.getValueParameters().stream()
                .map(MethodElement::ktParamText)
                .collect(Collectors.joining(", "));
        return name + "(" + params + ")";
    }

    private static String kotlinSignature(@NotNull KtSecondaryConstructor ctor) {
        String name = "<init>";
        String params = ctor.getValueParameters().stream()
                .map(MethodElement::ktParamText)
                .collect(Collectors.joining(", "));
        return name + "(" + params + ")";
    }

    private static String ktParamText(@NotNull KtParameter p) {
        if (p.getTypeReference() != null) {
            return p.getTypeReference().getText();
        }
        return p.getName() != null ? p.getName() : "?";
    }

    public @Nullable PsiMethod getPsiMethod() {
        return psiMethod;
    }

    public @Nullable KtNamedFunction getKtFunction() { return ktFunction; }

    public @Nullable KtPrimaryConstructor getKtPrimaryConstructor() { return ktPrimaryConstructor; }

    public @Nullable KtSecondaryConstructor getKtSecondaryConstructor() { return ktSecondaryConstructor; }

    public ClassElement getJavaClass() {
        return javaClass;
    }

    @Override
    public String toString() {
        return "Method(" + this.getName() + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MethodElement)) return false;
        if (!super.equals(o)) return false;
        MethodElement that = (MethodElement) o;
        return Objects.equals(getPsiMethod(), that.getPsiMethod())
                && Objects.equals(getKtFunction(), that.getKtFunction())
                && Objects.equals(getKtPrimaryConstructor(), that.getKtPrimaryConstructor())
                && Objects.equals(getKtSecondaryConstructor(), that.getKtSecondaryConstructor());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getPsiMethod(), getKtFunction(), getKtPrimaryConstructor(), getKtSecondaryConstructor());
    }

    @Override
    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof JavaMethodVisitor) {
            ((JavaMethodVisitor) visitor).visitJavaMethod(this);
        }
    }
}
