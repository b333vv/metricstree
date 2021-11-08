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
import com.intellij.openapi.project.NoAccessDuringPsiEvents;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiCompiledElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import org.b333vv.metric.event.MetricsEventListener;
import org.b333vv.metric.builder.ClassModelBuilder;
import org.b333vv.metric.model.code.JavaFile;
import org.b333vv.metric.ui.tree.builder.ClassMetricTreeBuilder;
import org.b333vv.metric.util.MetricsService;
import org.b333vv.metric.util.MetricsUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.DefaultTreeModel;

public class ClassMetricsPanel extends MetricsTreePanel {
    private static final String SPLIT_PROPORTION_PROPERTY = "CLASS_PANEL_SPLIT_PROPORTION";

    private ClassMetricsPanel(Project project) {
        super(project, "Metrics.ClassMetricsToolbar", SPLIT_PROPORTION_PROPERTY);
        MetricsEventListener metricsEventListener = new ClassMetricsEventListener();
        project.getMessageBus().connect(project).subscribe(MetricsEventListener.TOPIC, metricsEventListener);

        EditorChangeListener editorChangeListener = new EditorChangeListener();
        project.getMessageBus().connect(project).subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, editorChangeListener);
    }

    public static ClassMetricsPanel newInstance(Project project) {
        return new ClassMetricsPanel(project);
    }

    @Override
    public void update(@NotNull PsiJavaFile file) {
        MetricsUtils.setClassMetricsValuesEvolutionAdded(false);
        project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).cancelMetricsValuesEvolutionCalculation();
        psiJavaFile = file;
        if (MetricsService.isShowClassMetricsTree()) {
            MetricsUtils.getDumbService().runWhenSmart(() -> calculateMetrics(file));
        }
    }

    public void refresh() {
        if (psiJavaFile != null) {
            update(psiJavaFile);
        }
    }

    private void calculateMetrics(@NotNull PsiJavaFile psiJavaFile) {
        MetricsUtils.setClassMetricsTreeExists(false);
        MetricsUtils.getConsole().info("Built metrics tree for " + psiJavaFile.getName());
        ClassModelBuilder classModelBuilder = new ClassModelBuilder();
        JavaFile javaFile = classModelBuilder.buildJavaFile(psiJavaFile);
        metricTreeBuilder = new ClassMetricTreeBuilder(javaFile);
        buildTreeModel();
        MetricsUtils.setClassMetricsTreeExists(true);
    }

    private class ClassMetricsEventListener implements MetricsEventListener {
        @Override
        public void buildClassMetricsTree() {
            MetricsUtils.setClassMetricsValuesEvolutionAdded(false);
            buildTreeModel();
        }

        @Override
        public void showClassMetricsTree(boolean showClassMetricsTree) {
            if (!showClassMetricsTree) {
                clear();
            } else {
                createUIComponents(SPLIT_PROPORTION_PROPERTY);
                refresh();
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
            psiJavaFile = null;
            clear();
        }

        @Override
        public void selectionChanged(@NotNull FileEditorManagerEvent event) {
            psiJavaFile = null;
            clear();
            VirtualFile selectedFile = event.getNewFile();
            MetricsUtils.setCurrentProject(event.getManager().getProject());
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
