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

import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.progress.BackgroundTaskQueue;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiJavaFile;
import com.intellij.vcs.log.TimedVcsCommit;
import com.intellij.vcsUtil.VcsFileUtil;
import git4idea.GitUtil;
import git4idea.history.GitHistoryUtils;
import git4idea.repo.GitRepositoryManager;
import git4idea.util.GitFileUtils;
import org.b333vv.metric.model.builder.ClassModelBuilder;
import org.b333vv.metric.model.code.JavaClass;
import org.b333vv.metric.model.code.JavaProject;
import org.b333vv.metric.ui.tree.builder.ClassMetricsValuesEvolutionTreeBuilder;
import org.b333vv.metric.util.CalculationState;
import org.b333vv.metric.util.MetricsUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.DefaultTreeModel;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassMetricsValuesEvolutionProcessor {

    private final PsiJavaFile psiJavaFile;

    private CalculationState state = CalculationState.IDLE;
    private final PropertyChangeSupport support;

    private DefaultTreeModel metricsTreeModel;

    private final Runnable getFileFromGitCalculateMetricsAndPutThemToMap;
    private final Runnable buildTree;
    private final Runnable cancel;

    private BackgroundTaskQueue queue;

    private final Map<TimedVcsCommit, JavaClass> classMetricsEvolution;

    public ClassMetricsValuesEvolutionProcessor(@NotNull PsiJavaFile psiJavaFile) {

        this.psiJavaFile = psiJavaFile;

        classMetricsEvolution = new HashMap<>();

        support = new PropertyChangeSupport(this);

        getFileFromGitCalculateMetricsAndPutThemToMap = () ->
        {
            if (!GitUtil.isUnderGit(psiJavaFile.getVirtualFile())) {
                return;
            }
            support.firePropertyChange("state", this.state, CalculationState.RUNNING);
            this.state = CalculationState.RUNNING;

            GitRepositoryManager gitRepositoryManager = GitUtil.getRepositoryManager(psiJavaFile.getProject());
            VirtualFile root = gitRepositoryManager.getRepositoryForFile(psiJavaFile.getVirtualFile()).getRoot();
            List<? extends TimedVcsCommit> commits;
            ClassModelBuilder classModelBuilder = new ClassModelBuilder();

            try {
                commits = GitHistoryUtils.collectTimedCommits(psiJavaFile.getProject(), root,
                        "--", VcsFileUtil.relativePath(root, psiJavaFile.getVirtualFile()));
                if (commits.isEmpty()) {
                    return;
                }
                for (TimedVcsCommit commit : commits) {
                    ReadAction.run(() ->
                    {
                        PsiJavaFile gitPsiJavaFile = (PsiJavaFile) PsiFileFactory.getInstance(psiJavaFile.getProject()).createFileFromText(
                                psiJavaFile.getName(),
                                psiJavaFile.getFileType(),
                                new String(GitFileUtils.getFileContent(psiJavaFile.getProject(), root, commit.getId().toShortString(),
                                        VcsFileUtil.relativePath(root, psiJavaFile.getVirtualFile()))));

                        JavaClass rootJavaClass = classModelBuilder
                                .buildJavaProject(gitPsiJavaFile)
                                .getPackages()
                                .findFirst().get()
                                .getClasses()
                                .findFirst().get();

                        classMetricsEvolution.put(commit, rootJavaClass);
                    });
                }
            } catch (VcsException ignored) {}
        };

        buildTree = () -> {
            ClassModelBuilder classModelBuilder = new ClassModelBuilder();
            JavaProject javaProject = classModelBuilder.buildJavaProject(psiJavaFile);
            ClassMetricsValuesEvolutionTreeBuilder classMetricsValuesEvolutionTreeBuilder =
                    new ClassMetricsValuesEvolutionTreeBuilder(javaProject, classMetricsEvolution);
            metricsTreeModel = classMetricsValuesEvolutionTreeBuilder.createMetricsValuesEvolutionTreeModel();
            support.firePropertyChange("state", this.state, CalculationState.DONE);
            this.state = CalculationState.IDLE;
        };

        cancel = () -> {
            queue.clear();
            support.firePropertyChange("state", this.state, CalculationState.CANCELED);
            this.state = CalculationState.IDLE;
        };
    }

    public DefaultTreeModel getMetricsTreeModel() {
        return metricsTreeModel;
    }

    public void addPropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        support.addPropertyChangeListener(propertyChangeListener);
    }

    public void removePropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        support.removePropertyChangeListener(propertyChangeListener);
    }

    public void buildClassMetricsValuesEvolutionMap() {

        queue = new BackgroundTaskQueue(psiJavaFile.getProject(), "Get Metrics History");
        MetricsBackgroundableTask classMetricsTask = new MetricsBackgroundableTask(psiJavaFile.getProject(),
                "Get Metrics History for " + psiJavaFile.getName() + "...", true,
                getFileFromGitCalculateMetricsAndPutThemToMap, buildTree,
                cancel, null);

        queue.run(classMetricsTask);
    }
}
