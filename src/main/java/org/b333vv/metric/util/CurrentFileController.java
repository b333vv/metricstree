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

package org.b333vv.metric.util;

import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiCompiledElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import org.b333vv.metric.ui.tool.ClassMetricsPanel;
import org.jetbrains.annotations.NotNull;

public class CurrentFileController {
    private final Project project;
    private ClassMetricsPanel panel;

    public CurrentFileController(Project project) {
        this.project = project;
    }

    public void setPanel(ClassMetricsPanel panel) {
        this.panel = panel;
        initEventHandling();
        update();
    }

    private void initEventHandling() {
        EditorChangeListener editorChangeListener = new EditorChangeListener();
        project.getMessageBus()
                .connect(project)
                .subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, editorChangeListener);
    }

    private void update() {
        MetricsUtils.setProject(project);
        VirtualFile selectedFile = MetricsUtils.getSelectedFile(project);
        if (selectedFile == null) {
            return;
        }
        PsiFile psiFile = PsiManager.getInstance(project).findFile(selectedFile);
        if (psiFile == null) {
            return;
        }
        if (psiFile instanceof PsiCompiledElement) {
            return;
        }
        final FileType fileType = psiFile.getFileType();
        if (fileType.isBinary()) {
            return;
        }
        if (!fileType.getName().equals("JAVA")) {
            return;
        }
        PsiJavaFile psiJavaFile = (PsiJavaFile) psiFile;
        panel.update(psiJavaFile);
    }

    private class EditorChangeListener implements FileEditorManagerListener {
        @Override
        public void selectionChanged(@NotNull FileEditorManagerEvent event) {
            update();
        }
    }
}