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

import com.intellij.psi.PsiPackage;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.stream.Stream;

public class JavaPackage extends JavaCode {
    @Nullable
    private final PsiPackage psiPackage;

    public JavaPackage(String name, @Nullable PsiPackage psiPackage) {
        super(name);
        this.psiPackage = psiPackage;
    }

    @Nullable
    public PsiPackage getPsiPackage() {
        return psiPackage;
    }

    public Stream<JavaClass> classes() {
        return children.stream()
                .filter(c -> c instanceof JavaClass)
                .map(c -> (JavaClass) c)
                .sorted(Comparator.comparing(JavaCode::getName));
    }

    public Stream<JavaFile> files() {
        return children.stream()
                .filter(c -> c instanceof JavaFile)
                .map(c -> (JavaFile) c)
                .sorted(Comparator.comparing(JavaCode::getName));
    }

    public Stream<JavaPackage> subPackages() {
        return children.stream()
                .filter(c -> c instanceof JavaPackage)
                .map(c -> (JavaPackage) c)
                .sorted(Comparator.comparing(JavaCode::getName));
    }

    public void addClass(JavaClass javaClass) {
        addChild(javaClass);
    }

    public void addPackage(JavaPackage javaPackage) {
        addChild(javaPackage);
    }

    public void addFile(JavaFile javaFile) {
        addChild(javaFile);
    }

    @Override
    public String toString() {
        return "Package(" + this.getName() + ")";
    }
}
