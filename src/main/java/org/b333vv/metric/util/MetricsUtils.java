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

//import com.intellij.notification.NotificationGroupManager;
//import com.intellij.notification.NotificationType;
import com.intellij.notification.NotificationGroupManager;
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
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import org.b333vv.metric.ui.log.MetricsConsole;
import org.b333vv.metric.ui.tree.MetricsTreeFilter;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import javax.swing.*;
import java.util.*;

public final class MetricsUtils {

    //    private final NotificationGroup NOTIFICATION_GROUP = new NotificationGroup("MetricsTree Info", NotificationDisplayType.BALLOON, true);
    private final static MetricsTreeFilter classMetricsTreeFilter = new MetricsTreeFilter();
    private final static MetricsTreeFilter projectMetricsTreeFilter = new MetricsTreeFilter();

    /**
     * @deprecated Use {@link org.b333vv.metric.service.UIStateService#isProjectAutoScrollable()} instead.
     */
    @Deprecated(forRemoval = true)
    private static boolean projectAutoScrollable = true;
    
    /**
     * @deprecated Use {@link org.b333vv.metric.service.UIStateService#isProfileAutoScrollable()} instead.
     */
    @Deprecated(forRemoval = true)
    private static boolean profileAutoScrollable = true;
    private static boolean classMetricsTreeExists = true;
    //    private static boolean classMetricsTreeExists = false;
    private static boolean projectMetricsTreeActive = false;
    private static boolean classMetricsValuesEvolutionCalculationPerforming = false;
    private static boolean classMetricsValuesEvolutionAdded = false;


    private MetricsUtils() {
        // Utility class
    }

//    public static MetricsUtils instance() {
//        return ServiceManager.getService(MetricsUtils.class);
    ////        return getCurrentProject().getService(MetricsUtils.class);
//    }

    public static void setClassMetricsValuesEvolutionCalculationPerforming(boolean value) {
        classMetricsValuesEvolutionCalculationPerforming = value;
    }

    public static boolean isClassMetricsValuesEvolutionAdded() {
        return classMetricsValuesEvolutionAdded;
    }

    public static void setClassMetricsValuesEvolutionAdded(boolean value) {
        classMetricsValuesEvolutionAdded = value;
    }

    public static DumbService getDumbService(Project project) {
        return DumbService.getInstance(project);
    }

    public static MetricsTreeFilter getClassMetricsTreeFilter() {
        return classMetricsTreeFilter;
    }

    public static MetricsTreeFilter getProjectMetricsTreeFilter() {
        return projectMetricsTreeFilter;
    }

    /**
     * @deprecated Use {@link org.b333vv.metric.service.UIStateService#isProjectAutoScrollable()} instead.
     */
    @Deprecated(forRemoval = true)
    public static boolean isProjectAutoScrollable() {
        return projectAutoScrollable;
    }

    /**
     * @deprecated Use {@link org.b333vv.metric.service.UIStateService#setProjectAutoScrollable(boolean)} instead.
     */
    @Deprecated(forRemoval = true)
    public static void setProjectAutoScrollable(boolean value) {
        projectAutoScrollable = value;
    }

    /**
     * @deprecated Use {@link org.b333vv.metric.service.UIStateService#isProfileAutoScrollable()} instead.
     */
    @Deprecated(forRemoval = true)
    public static boolean isProfileAutoScrollable() {
        return profileAutoScrollable;
    }

    /**
     * @deprecated Use {@link org.b333vv.metric.service.UIStateService#setProfileAutoScrollable(boolean)} instead.
     */
    @Deprecated(forRemoval = true)
    public static void setProfileAutoScrollable(boolean value) {
        profileAutoScrollable = value;
    }

    public static boolean isClassMetricsTreeExists() {
        return classMetricsTreeExists;
    }

    public static void setClassMetricsTreeExists(boolean value) {
        classMetricsTreeExists = value;
    }

    public static boolean isMetricsEvolutionCalculationPerforming() {
        return classMetricsValuesEvolutionCalculationPerforming;
    }

//    public static <T> T getProfiles(Class<T> clazz) {
//        return getProfiles(ApplicationManager.getApplication(), clazz);
//    }

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

    public static void openInEditor(Project project, PsiElement psiElement) {
        final EditorController caretMover = new EditorController(project);
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
        return projectMetricsTreeActive;
    }

    public static boolean setProjectTreeActive(boolean value) {
        return projectMetricsTreeActive = value;
    }

    public void notify(String content, Project project) {
//        Pre-2020.3
//        final Notification notification = NOTIFICATION_GROUP.createNotification(content, NotificationType.INFORMATION);
//        notification.notify(project);

//        2020.3 and later
//        NotificationGroupManager.getInstance().getNotificationGroup("MetricsTree Info")
//                .createNotification(content, NotificationType.INFORMATION)
//                .notify(project);
    }
}
