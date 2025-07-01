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

package org.b333vv.metric.builder;

import com.intellij.analysis.AnalysisScope;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiCompiledElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import org.b333vv.metric.builder.DependenciesBuilder;

public class DependenciesCalculator {

    private final AnalysisScope scope;
    private final DependenciesBuilder dependenciesBuilder;

    private ProgressIndicator indicator;
    private int filesCount;
    private int progress = 0;

    public DependenciesCalculator(AnalysisScope scope, DependenciesBuilder dependenciesBuilder) {
        this.scope = scope;
        this.dependenciesBuilder = dependenciesBuilder;
    }

    public DependenciesBuilder calculateDependencies() {
        indicator = ProgressManager.getInstance().getProgressIndicator();
        filesCount = scope.getFileCount();
        indicator.setText("Calculating dependencies");
        scope.accept(new PsiJavaFileVisitor());
        return dependenciesBuilder;
    }

    private class PsiJavaFileVisitor extends PsiElementVisitor {
        @Override
        public void visitFile(PsiFile psiFile) {
            super.visitFile(psiFile);
            indicator.checkCanceled();
            if (psiFile instanceof PsiCompiledElement) {
                return;
            }
            final FileType fileType = psiFile.getFileType();
            if (!fileType.getName().equals("JAVA") || fileType.isBinary()) {
                return;
            }
            final VirtualFile virtualFile = psiFile.getVirtualFile();
            final ProjectRootManager rootManager = ProjectRootManager.getInstance(psiFile.getProject());
            final ProjectFileIndex fileIndex = rootManager.getFileIndex();
            if (fileIndex.isExcluded(virtualFile) || !fileIndex.isInContent(virtualFile)) {
                return;
            }
            final String fileName = psiFile.getName();
            indicator.setText("Calculating dependencies: processing file " + fileName + "...");
            progress++;
            PsiJavaFile psiJavaFile = (PsiJavaFile) psiFile;
            dependenciesBuilder.build(psiJavaFile);
            indicator.setIndeterminate(false);
            indicator.setFraction((double) progress / (double) filesCount);
        }
    }
}
