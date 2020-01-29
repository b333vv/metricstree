package org.jacoquev.ui;

import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jacoquev.util.MetricsUtils;
import org.jetbrains.annotations.NotNull;

public class CurrentFileController {
    private final Project project;
    private MetricsToolWindowPanel panel;

    public CurrentFileController(Project project) {
        this.project = project;
    }

    public void setPanel(MetricsToolWindowPanel panel) {
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
            panel.update(selectedFile);
        }
    }

    private class EditorChangeListener implements FileEditorManagerListener {
        @Override
        public void selectionChanged(@NotNull FileEditorManagerEvent event) {
            update();
        }
    }
}