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

import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class ProjectElement extends CodeElement {
//    private final Project project;
    private final Map<String, PackageElement> allPackages;
    private final Set<ClassElement> allClasses;

    public ProjectElement(@NotNull String name/*, @NotNull Project project*/) {
        super(name);
//        this.project = project;
        allPackages = new ConcurrentHashMap<>();
        allClasses = new ConcurrentHashMap<ClassElement, Boolean>().keySet(true);
    }

    public Stream<PackageElement> packages() {
        return children.stream()
                .filter(c -> c instanceof PackageElement)
                .map(c -> (PackageElement) c)
                .sorted(Comparator.comparing(CodeElement::getName));
    }

    public void addPackage(@NotNull PackageElement javaPackage) {
        addChild(javaPackage);
    }

    public void addToAllClasses(@NotNull ClassElement javaClass) {
        allClasses.add(javaClass);
    }

    @Override
    public String toString() {
        return "Project(" + this.getName() + ")";
    }

    public void putToAllPackages(@NotNull String name, @NotNull PackageElement javaPackage) {
        allPackages.put(name, javaPackage);
    }

    public void removeFromAllPackages(@NotNull String name) {
        allPackages.remove(name);
    }

    public PackageElement getFromAllPackages(@NotNull String name) {
        return allPackages.get(name);
    }

    public boolean allPackagesIsEmpty() {
        return allPackages.isEmpty();
    }

    public Stream<PackageElement> allPackages() { return allPackages.values().stream(); }

    public Stream<ClassElement> allClasses() { return allClasses.stream(); }
}
