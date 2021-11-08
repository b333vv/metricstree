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

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ComponentManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.DumbServiceImpl;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import org.b333vv.metric.ui.log.MetricsConsole;
import org.b333vv.metric.ui.tree.MetricsTreeFilter;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import javax.swing.*;
import java.util.*;

public final class MetricsUtils {

    private final NotificationGroup NOTIFICATION_GROUP = new NotificationGroup("MetricsTree Info", NotificationDisplayType.BALLOON, true);
    private final MetricsTreeFilter classMetricsTreeFilter = new MetricsTreeFilter();
    private final MetricsTreeFilter projectMetricsTreeFilter = new MetricsTreeFilter();

    private Project currentProject;
    private boolean projectAutoScrollable = true;
    private boolean profileAutoScrollable = true;
    private boolean classMetricsTreeExists = false;
    private boolean projectMetricsTreeActive = false;
    private boolean classMetricsValuesEvolutionCalculationPerforming = false;
    private boolean classMetricsValuesEvolutionAdded = false;


    private MetricsUtils() {
        // Utility class
    }

    public static MetricsUtils instance() {
        return ServiceManager.getService(MetricsUtils.class);
    }

    public static synchronized MetricsConsole getConsole() {
        return getForProject(MetricsConsole.class);
    }

    public static void setClassMetricsValuesEvolutionCalculationPerforming(boolean classMetricsValuesEvolutionCalculationPerforming) {
        instance().classMetricsValuesEvolutionCalculationPerforming = classMetricsValuesEvolutionCalculationPerforming;
    }

    public static boolean isClassMetricsValuesEvolutionAdded() {
        return instance().classMetricsValuesEvolutionAdded;
    }

    public static void setClassMetricsValuesEvolutionAdded(boolean classMetricsValuesEvolutionAdded) {
        instance().classMetricsValuesEvolutionAdded = classMetricsValuesEvolutionAdded;
    }

    public static Project getCurrentProject() {
        return instance().currentProject;
    }

    public static void setCurrentProject(Project value) {
        instance().currentProject = value;
    }

    public static DumbService getDumbService() {
        return DumbService.getInstance(instance().currentProject);
    }

    public static MetricsTreeFilter getClassMetricsTreeFilter() {
        return instance().classMetricsTreeFilter;
    }

    public static MetricsTreeFilter getProjectMetricsTreeFilter() {
        return instance().projectMetricsTreeFilter;
    }

    public static boolean isProjectAutoScrollable() {
        return instance().projectAutoScrollable;
    }

    public static void setProjectAutoScrollable(boolean autoScrollable) {
        instance().projectAutoScrollable = autoScrollable;
    }

    public static boolean isProfileAutoScrollable() {
        return instance().profileAutoScrollable;
    }

    public static void setProfileAutoScrollable(boolean autoScrollable) {
        instance().profileAutoScrollable = autoScrollable;
    }

    public static boolean isClassMetricsTreeExists() {
        return instance().classMetricsTreeExists;
    }

    public static void setClassMetricsTreeExists(boolean classMetricsTreeExists) {
        instance().classMetricsTreeExists = classMetricsTreeExists;
    }

    public static boolean isMetricsEvolutionCalculationPerforming() {
        return instance().classMetricsValuesEvolutionCalculationPerforming;
    }

    public static <T> T get(ComponentManager container, Class<T> clazz) {
        T t = container.getComponent(clazz);
        if (t == null) {
            throw new IllegalArgumentException("Class not found: " + clazz.getName());
        }
        return t;
    }

    public static <T> T getForProject(Class<T> clazz) {
        Project project = instance().currentProject;
        if (project != null) {
            return instance().currentProject.getComponent(clazz);
        }
        else {
            return get(clazz);
        }
    }

    public static <T> T get(Class<T> clazz) {
        return get(ApplicationManager.getApplication(), clazz);
    }

    @CheckForNull
    public static VirtualFile getSelectedFile(Project project) {
        if (project.isDisposed()) {
            return null;
        }
        if (!SwingUtilities.isEventDispatchThread()) {
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

    @CheckForNull
    public static PsiJavaFile getSelectedPsiJavaFile(Project project) {
        VirtualFile selectedFile = getSelectedFile(project);
        if (selectedFile == null) {
            return null;
        }
        PsiFile psiFile = PsiManager.getInstance(project).findFile(selectedFile);
        if (psiFile == null) {
            return null;
        }
        if (psiFile instanceof PsiCompiledElement) {
            return null;
        }
        final FileType fileType = psiFile.getFileType();
        if (fileType.isBinary()) {
            return null;
        }
        if (!fileType.getName().equals("JAVA")) {
            return null;
        }
        return (PsiJavaFile) psiFile;
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
    public static Editor getEditorIfSelected(Project project, PsiElement psiElement) {
        final VirtualFile elementFile = getVirtualFile(psiElement);
        if (elementFile == null) {
            return null;
        }
        final FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
        final FileEditor fileEditor = fileEditorManager.getSelectedEditor(elementFile);
        Editor editor = null;
        if (fileEditor instanceof TextEditor) {
            editor = ((TextEditor) fileEditor).getEditor();
        }
        return editor;
    }

    public static void openInEditor(PsiElement psiElement) {
        final EditorController caretMover = new EditorController(getCurrentProject());
        if (psiElement != null) {
            Editor editor = caretMover.openInEditor(psiElement);
            if (editor != null) {
                caretMover.moveEditorCaret(psiElement);
            }
        }
    }

    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValueReversed(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new ArrayList<>(map.entrySet());
        list.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    public static boolean isProjectTreeActive() {
        return instance().projectMetricsTreeActive;
    }

    public static boolean setProjectTreeActive(boolean projectMetricsTreeActive) {
        return instance().projectMetricsTreeActive = projectMetricsTreeActive;
    }

    public void notify(String content, Project project) {
        final Notification notification = NOTIFICATION_GROUP.createNotification(content, NotificationType.INFORMATION);
        notification.notify(project);
    }
}
