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

public class PackageElement extends CodeElement {
    @Nullable
    private final PsiPackage psiPackage;

    public PackageElement(String name, @Nullable PsiPackage psiPackage) {
        super(name);
        this.psiPackage = psiPackage;
    }

    @Nullable
    public PsiPackage getPsiPackage() {
        return psiPackage;
    }

    public Stream<ClassElement> classes() {
        return files()
                .flatMap(FileElement::classes)
                .sorted(Comparator.comparing(CodeElement::getName));
    }

    public Stream<FileElement> files() {
        return children.stream()
                .filter(c -> c instanceof FileElement)
                .map(c -> (FileElement) c)
                .sorted(Comparator.comparing(CodeElement::getName));
    }

    public Stream<PackageElement> subPackages() {
        return children.stream()
                .filter(c -> c instanceof PackageElement)
                .map(c -> (PackageElement) c)
                .sorted(Comparator.comparing(CodeElement::getName));
    }

    public void addClass(ClassElement javaClass) {
        addChild(javaClass);
    }

    public void addPackage(PackageElement javaPackage) {
        addChild(javaPackage);
    }

    public void addFile(FileElement javaFile) {
        addChild(javaFile);
    }

    @Override
    public String toString() {
        return "Package(" + this.getName() + ")";
    }
}
