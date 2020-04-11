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

package org.b333vv.metric.model.builder;

import com.intellij.psi.PsiJavaFile;
import org.apache.commons.io.FilenameUtils;
import org.b333vv.metric.model.code.JavaCode;
import org.b333vv.metric.model.code.JavaPackage;
import org.b333vv.metric.model.code.JavaProject;
import org.jetbrains.annotations.NotNull;

public class ClassModelBuilder extends ModelBuilder {

    public JavaProject buildJavaCode(@NotNull PsiJavaFile psiJavaFile) {
        JavaProject javaProject = new JavaProject(FilenameUtils.getBaseName(psiJavaFile.getName()));
        JavaPackage javaPackage = new JavaPackage(psiJavaFile.getPackageName(), null);
        javaProject.addPackage(javaPackage);
        createJavaClass(javaPackage, psiJavaFile);
        return javaProject;
    }
}