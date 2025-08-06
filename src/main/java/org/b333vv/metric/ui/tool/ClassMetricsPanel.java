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

package org.b333vv.metric.ui.tool;

import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiCompiledElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import org.b333vv.metric.event.MetricsEventListener;
import org.b333vv.metric.builder.ClassModelBuilder;
import org.b333vv.metric.model.code.JavaFile;
import org.b333vv.metric.service.UIStateService;
import org.b333vv.metric.ui.settings.composition.ClassMetricsTreeSettings;
import org.b333vv.metric.util.SettingsService;
import org.b333vv.metric.ui.tree.builder.ClassMetricTreeBuilder;
import org.b333vv.metric.util.EditorUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultTreeModel;

public class ClassMetricsPanel extends MetricsTreePanel {
    private static final String SPLIT_PROPORTION_PROPERTY = "CLASS_PANEL_SPLIT_PROPORTION";

    private ClassMetricsPanel(Project project) {
        super(project, "Metrics.ClassMetricsToolbar", SPLIT_PROPORTION_PROPERTY);
        MetricsEventListener metricsEventListener = new ClassMetricsEventListener();
        project.getMessageBus().connect().subscribe(MetricsEventListener.TOPIC, metricsEventListener);

        EditorChangeListener editorChangeListener = new EditorChangeListener();
        project.getMessageBus().connect().subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, editorChangeListener);
    }

    public static ClassMetricsPanel newInstance(Project project) {
        return new ClassMetricsPanel(project);
    }

    @Override
    public void update(@NotNull PsiJavaFile file) {
        project.getService(UIStateService.class).setClassMetricsValuesEvolutionAdded(false);
        project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).cancelMetricsValuesEvolutionCalculation();
        // psiJavaFile = file; // Удаляем кеширование PSI-элемента
        if (project.getService(SettingsService.class).getClassMetricsTreeSettings().isShowClassMetricsTree()) {
            DumbService.getInstance(project).runWhenSmart(() -> calculateMetrics(file));
        }
    }

    public void refresh() {
        // Вместо использования кеша, получаем PsiJavaFile по VirtualFile
        VirtualFile selectedFile = EditorUtils.getSelectedFile(project);
        if (selectedFile != null) {
            PsiFile psiFile = PsiManager.getInstance(project).findFile(selectedFile);
            if (psiFile instanceof PsiJavaFile) {
                update((PsiJavaFile) psiFile);
            }
        }
    }

    private void calculateMetrics(@NotNull PsiJavaFile psiJavaFile) {
        UIStateService uiStateService = project.getService(UIStateService.class);
        uiStateService.setClassMetricsTreeExists(false);
        project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC)
                .printInfo("Built metrics tree for " + psiJavaFile.getName());
        JavaFile jf = CachedValuesManager.getCachedValue(psiJavaFile, () -> {
            JavaFile javaFile = new ClassModelBuilder(psiJavaFile.getProject()).buildJavaFile(psiJavaFile);
            return CachedValueProvider.Result.create(javaFile, psiJavaFile);
        });

        metricTreeBuilder = new ClassMetricTreeBuilder(jf, psiJavaFile.getProject());
        buildTreeModel();
        uiStateService.setClassMetricsTreeExists(true);
    }

    private class ClassMetricsEventListener implements MetricsEventListener {
        @Override
        public void buildClassMetricsTree() {
            project.getService(UIStateService.class).setClassMetricsValuesEvolutionAdded(false);
            buildTreeModel();
        }

        @Override
        public void showClassMetricsTree(boolean showClassMetricsTree) {
            if (!showClassMetricsTree) {
                clear();
            } else {
                SwingUtilities.invokeLater(() -> {
                    createUIComponents(SPLIT_PROPORTION_PROPERTY);
                    refresh();
                });
            }
        }

        @Override
        public void refreshClassMetricsTree() {
            refresh();
        }

        @Override
        public void classMetricsValuesEvolutionCalculated(@NotNull DefaultTreeModel metricsTreeModel) {
            showResults(metricsTreeModel);
        }
    }

    private class EditorChangeListener implements FileEditorManagerListener {

        @Override
        public void fileClosed(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
            clear();
        }

        @Override
        public void selectionChanged(@NotNull FileEditorManagerEvent event) {
            clear();
            VirtualFile selectedFile = event.getNewFile();
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
            update((PsiJavaFile) psiFile);
        }
    }
}
