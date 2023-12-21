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

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public class JavaMethod extends JavaCode {
    private final PsiMethod psiMethod;
    private final JavaClass javaClass;

    public JavaMethod(PsiMethod psiMethod, JavaClass javaClass) {
        super(signature(psiMethod));
        this.psiMethod = psiMethod;
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

    public PsiMethod getPsiMethod() {
        return psiMethod;
    }

    public JavaClass getJavaClass() {
        return javaClass;
    }

    @Override
    public String toString() {
        return "Method(" + this.getName() + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JavaMethod)) return false;
        if (!super.equals(o)) return false;
        JavaMethod that = (JavaMethod) o;
        return Objects.equals(getPsiMethod(), that.getPsiMethod());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getPsiMethod());
    }

    @Override
    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof JavaMethodVisitor) {
            ((JavaMethodVisitor) visitor).visitJavaMethod(this);
        }
    }
}
