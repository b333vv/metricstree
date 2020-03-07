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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class JavaProject extends JavaCode {
    private final Map<String, JavaPackage> packagesMap;
    private final Set<JavaClass> classes;


    public JavaProject(String name) {
        super(name);
        packagesMap = new HashMap<>();
        classes = new HashSet<>();
    }

    public Stream<JavaPackage> getPackages() {
        return children.stream()
                .filter(c -> c instanceof JavaPackage)
                .map(c -> (JavaPackage) c);
    }

    public void addPackage(JavaPackage aJavaPackage) {
        addChild(aJavaPackage);
    }

    public void addClassToClassesSet(JavaClass javaClass) {
        classes.add(javaClass);
    }

    @Override
    public String toString() {
        return "Project(" + this.getName() + ")";
    }

    public Map<String, JavaPackage> getPackagesMap() {
        return packagesMap;
    }
    public Stream<JavaPackage> getAllPackages() { return packagesMap.values().stream(); }
    public Stream<JavaClass> getAllClasses() { return classes.stream(); }


}
