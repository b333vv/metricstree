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

package org.b333vv.metric.exec;

import com.intellij.analysis.AnalysisScope;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiCompiledElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import org.b333vv.metric.model.builder.ClassModelBuilder;
import org.b333vv.metric.model.builder.ProjectModelBuilder;
import org.b333vv.metric.model.code.JavaFile;
import org.b333vv.metric.model.code.JavaPackage;
import org.b333vv.metric.model.code.JavaProject;
import org.b333vv.metric.util.MetricsUtils;

import java.util.*;
import java.util.concurrent.*;

import static org.b333vv.metric.exec.LaunderThrowable.launderThrowable;

public class MetricsCalculationExecutor {
    private final ExecutorService javaFileExecutor;
    private final ClassModelBuilder classModelBuilder;
    private final ProjectModelBuilder projectModelBuilder;
    private final Map<PsiJavaFile, Future<JavaFile>> javaFileMap = new ConcurrentHashMap<>();

    public MetricsCalculationExecutor(JavaProject javaProject) {
        this.javaFileExecutor = Executors.newCachedThreadPool();
        projectModelBuilder = new ProjectModelBuilder(javaProject);
        classModelBuilder = new ClassModelBuilder();
    }

    public void execute(Set<PsiJavaFile> psiJavaFiles) {
        CompletionService<JavaFile> javaFileCompletionService = new ExecutorCompletionService<>(javaFileExecutor);
        for (final PsiJavaFile psiJavaFile : psiJavaFiles) {
            javaFileMap.put(psiJavaFile, javaFileCompletionService
                    .submit(() -> classModelBuilder.buildJavaFile(psiJavaFile)));
        }
        try {
            for (Map.Entry<PsiJavaFile, Future<JavaFile>> fileEntry : javaFileMap.entrySet()) {
                projectModelBuilder.findOrCreateJavaPackage(fileEntry.getKey())
                        .addFile(fileEntry.getValue().get());
            }
        } catch (InterruptedException e) {
            MetricsUtils.getConsole().error(e.getMessage());
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            MetricsUtils.getConsole().error(e.getMessage());
            throw launderThrowable(e.getCause());
        }
    }
}
