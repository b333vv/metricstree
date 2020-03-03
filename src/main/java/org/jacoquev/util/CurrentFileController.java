package org.jacoquev.util;

import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import org.jacoquev.ui.toolWindow.ClassMetricsPanel;
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
        if (selectedFile != null && selectedFile.getFileType().getName().equals("JAVA")) {
            PsiFile psiFile = PsiManager.getInstance(project).findFile(selectedFile);
            PsiJavaFile psiJavaFile = (PsiJavaFile) psiFile;
            panel.update(psiJavaFile);
        }
    }

    private class EditorChangeListener implements FileEditorManagerListener {
        @Override
        public void selectionChanged(@NotNull FileEditorManagerEvent event) {
            update();
        }
    }
}
