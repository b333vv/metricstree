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

public class JavaProject extends JavaCode {
    private final Map<String, JavaPackage> allPackages;
    private final Set<JavaClass> allClasses;

    public JavaProject(@NotNull String name) {
        super(name);
        allPackages = new ConcurrentHashMap<>();
        allClasses = new ConcurrentHashMap<JavaClass, Boolean>().keySet(true);;
    }

    public Stream<JavaPackage> packages() {
        return children.stream()
                .filter(c -> c instanceof JavaPackage)
                .map(c -> (JavaPackage) c)
                .sorted(Comparator.comparing(JavaCode::getName));
    }

    public void addPackage(@NotNull JavaPackage javaPackage) {
        addChild(javaPackage);
    }

    public void addToAllClasses(@NotNull JavaClass javaClass) {
        allClasses.add(javaClass);
    }

    @Override
    public String toString() {
        return "Project(" + this.getName() + ")";
    }

    public void putToAllPackages(@NotNull String name, @NotNull JavaPackage javaPackage) {
        allPackages.put(name, javaPackage);
    }

    public void removeFromAllPackages(@NotNull String name) {
        allPackages.remove(name);
    }

    public JavaPackage getFromAllPackages(@NotNull String name) {
        return allPackages.get(name);
    }

    public boolean allPackagesIsEmpty() {
        return allPackages.isEmpty();
    }

    public Stream<JavaPackage> allPackages() { return allPackages.values().stream(); }

    public Stream<JavaClass> allClasses() { return allClasses.stream(); }
}
