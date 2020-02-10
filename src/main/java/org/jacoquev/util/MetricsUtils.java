package org.jacoquev.util;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.components.ComponentManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.DumbServiceImpl;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.SourceFolder;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.concurrency.AppExecutorUtil;
import org.jacoquev.ui.MetricsToolWindowPanel;
import org.jacoquev.ui.tree.MetricsTreeFilter;
import org.jacoquev.ui.ProjectMetricsPanel;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

public class MetricsUtils {

    private static final Logger LOG = Logger.getInstance(MetricsUtils.class);
    private static Project project;
    private static MetricsTreeFilter classMetricsTreeFilter = new MetricsTreeFilter();
    private static MetricsTreeFilter projectMetricsTreeFilter = new MetricsTreeFilter();
    private static MetricsToolWindowPanel metricsToolWindowPanel;
    private static ProjectMetricsPanel projectMetricsPanel;
    private static boolean autoscroll = true;

    private MetricsUtils() {
        // Utility class
    }

    public static <T> T get(ComponentManager container, Class<T> clazz) {
        T t = container.getComponent(clazz);
        if (t == null) {
            LOG.error("Could not find class in container: " + clazz.getName());
            throw new IllegalArgumentException("Class not found: " + clazz.getName());
        }

        return t;
    }

    public static <T> T get(Class<T> clazz) {
        return get(ApplicationManager.getApplication(), clazz);
    }

    @CheckForNull
    public static VirtualFile getSelectedFile(Project project) {
        if (project.isDisposed()) {
            return null;
        }
        FileEditorManager editorManager = FileEditorManager.getInstance(project);

        Editor editor = editorManager.getSelectedTextEditor();
        if (editor != null) {
            Document doc = editor.getDocument();
            FileDocumentManager docManager = FileDocumentManager.getInstance();
            return docManager.getFile(doc);
        }

        return null;
    }

    @Nullable
    public static SourceFolder getSourceFolder(@CheckForNull VirtualFile source, Module module) {
        if (source == null) {
            return null;
        }
        for (ContentEntry entry : ModuleRootManager.getInstance(module).getContentEntries()) {
            for (SourceFolder folder : entry.getSourceFolders()) {
                if (source.equals(folder.getFile())) {
                    return folder;
                }
            }
        }
        return null;
    }

    public static Project getProject() {
        return project;
    }

    public static void setProject(Project value) {
        project = value;
    }

    public static DumbService getDumbService() {
        return DumbServiceImpl.getInstance(project);
    }

    public static MetricsTreeFilter getClassMetricsTreeFilter() {
        return classMetricsTreeFilter;
    }

    public static MetricsTreeFilter getProjectMetricsTreeFilter() {
        return projectMetricsTreeFilter;
    }

    public static MetricsToolWindowPanel getMetricsToolWindowPanel() {
        return metricsToolWindowPanel;
    }

    public static void setMetricsToolWindowPanel(MetricsToolWindowPanel value) {
        metricsToolWindowPanel = value;
    }

    public static <T> T callInReadAction(Callable<T> task) {
        T result = null;
        try {
            result = ReadAction.nonBlocking(task)
                    .inSmartMode(getProject())
                    .submit(AppExecutorUtil.getAppExecutorService())
                    .get();
        } catch (ExecutionException | InterruptedException e) {
            metricsToolWindowPanel.getConsole().error(e.getMessage());
        }
        return result;
    }

    public static void runInReadAction(Runnable task) {
        try {
            ReadAction.nonBlocking(task)
                    .inSmartMode(getProject())
                    .submit(AppExecutorUtil.getAppExecutorService());
        } catch (Exception e) {
            metricsToolWindowPanel.getConsole().error(e.getMessage());
        }
    }

    public static void refreshMetricsTree() {
        metricsToolWindowPanel.refresh();
    }

    public static void setProjectMetricsPanel(ProjectMetricsPanel value) {
        projectMetricsPanel = value;
    }

    public static ProjectMetricsPanel getProjectMetricsPanel() {
        return projectMetricsPanel;
    }

    public static void calculateProjectMetrics() {
        projectMetricsPanel.calculate();
    }

    public static boolean isAutoscroll() {
        return autoscroll;
    }

    public static void setAutoscroll(boolean autoscroll) {
        MetricsUtils.autoscroll = autoscroll;
    }

    @Nullable
    private static VirtualFile getVirtualFile(PsiElement psiElement) {
        if (psiElement == null) {
            return null;
        }
        final PsiFile containingFile = psiElement.getContainingFile();
        if (containingFile == null) {
            return null;
        }
        return containingFile.getVirtualFile();
    }

    public static boolean isElementInSelectedFile(Project project,
                                                  PsiElement psiElement) {
        final VirtualFile elementFile = getVirtualFile(psiElement);
        if (elementFile == null) {
            return false;
        }
        final FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
        final VirtualFile[] currentEditedFiles = fileEditorManager.getSelectedFiles();

        for (final VirtualFile file : currentEditedFiles) {
            if (elementFile.equals(file)) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    public static Editor getEditorIfSelected(Project project,
                                             PsiElement psiElement) {
        final VirtualFile elementFile = getVirtualFile(psiElement);
        if (elementFile == null) {
            return null;
        }

        final FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
        final FileEditor fileEditor = fileEditorManager.getSelectedEditor(elementFile);

        Editor editor = null;

        if (fileEditor != null && fileEditor instanceof TextEditor) {
            editor = ((TextEditor) fileEditor).getEditor();
        }

        return editor;
    }
}
