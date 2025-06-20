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

import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
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
import org.b333vv.metric.event.MetricsEventListener;
import org.b333vv.metric.model.code.JavaClass;
import org.b333vv.metric.model.code.JavaFile;
import org.b333vv.metric.ui.log.MetricsConsole;
import org.b333vv.metric.ui.tree.builder.ClassMetricsValuesEvolutionTreeBuilder;
import org.b333vv.metric.util.MetricsUtils;
import org.b333vv.metric.task.MetricTaskCache;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.DefaultTreeModel;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ConcurrentHashMap;

public class ClassMetricsValuesEvolutionProcessor {

    private final PsiJavaFile psiJavaFile;
    private final FutureTask<Void> getFileFromGitCalculateMetricsAndPutThemToMap;
    private final FutureTask<Void> buildTree;
    private final Runnable cancel;
    private final Map<TimedVcsCommit, Set<JavaClass>> classMetricsEvolution = new ConcurrentHashMap<>();
    private final ClassModelBuilder classModelBuilder;
    private Project project;

    private DefaultTreeModel metricsTreeModel;

    public ClassMetricsValuesEvolutionProcessor(@NotNull PsiJavaFile psiJavaFile) {

        this.psiJavaFile = psiJavaFile;
        classModelBuilder = new ClassModelBuilder(psiJavaFile.getProject());
        project = psiJavaFile.getProject();

        MetricsEventListener metricsEventListener = new ClassMetricsEvolutionEventListener();
        project.getMessageBus()
                .connect(project).subscribe(MetricsEventListener.TOPIC, metricsEventListener);

        project.getService(MetricsConsole.class).info("Adding metrics values evolution tree for " + psiJavaFile.getName() + " started");

        Callable<Void> gitCalculations = () ->
        {
            if (!GitUtil.isUnderGit(psiJavaFile.getVirtualFile())) {
                return null;
            }
            MetricsUtils.setClassMetricsValuesEvolutionCalculationPerforming(true);

            GitRepositoryManager gitRepositoryManager = GitUtil.getRepositoryManager(psiJavaFile.getProject());
            VirtualFile root = Objects.requireNonNull(gitRepositoryManager.getRepositoryForFile(psiJavaFile.getVirtualFile())).getRoot();
            List<? extends TimedVcsCommit> commits;

            try {
                commits = GitHistoryUtils.collectTimedCommits(psiJavaFile.getProject(), root,
                        "--", VcsFileUtil.relativePath(root, psiJavaFile.getVirtualFile()));
                if (commits.isEmpty()) {
                    return null;
                }
                for (TimedVcsCommit commit : commits) {
                    ReadAction.run(() ->
                    {
                        PsiJavaFile gitPsiJavaFile = (PsiJavaFile) PsiFileFactory.getInstance(psiJavaFile.getProject()).createFileFromText(
                                psiJavaFile.getName(),
                                psiJavaFile.getFileType(),
                                new String(GitFileUtils.getFileContent(psiJavaFile.getProject(), root, commit.getId().toShortString(),
                                        VcsFileUtil.relativePath(root, psiJavaFile.getVirtualFile()))));
                        JavaFile javaFile = classModelBuilder.buildJavaFile(gitPsiJavaFile);
                        javaFile.classes()
                                .forEach(c -> classMetricsEvolution.computeIfAbsent(commit, (unused) -> new HashSet<>()).add(c));
                    });
                }
            } catch (VcsException e) {
                project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo(e.getMessage());
//                MetricsUtils.getConsole().error(e.getMessage());
            }
            return null;
        };

        getFileFromGitCalculateMetricsAndPutThemToMap = new FutureTask<>(gitCalculations);

        Callable<Void> buildingTree = () -> {
            ClassModelBuilder classModelBuilder = new ClassModelBuilder(psiJavaFile.getProject());
            JavaFile javaFile = classModelBuilder.buildJavaFile(psiJavaFile);
            ClassMetricsValuesEvolutionTreeBuilder classMetricsValuesEvolutionTreeBuilder =
                    new ClassMetricsValuesEvolutionTreeBuilder(javaFile, Collections.unmodifiableMap(classMetricsEvolution), project);
            metricsTreeModel = classMetricsValuesEvolutionTreeBuilder.createMetricsValuesEvolutionTreeModel();
            if (metricsTreeModel != null) {
                psiJavaFile.getProject().getMessageBus().syncPublisher(MetricsEventListener.TOPIC)
                        .classMetricsValuesEvolutionCalculated(metricsTreeModel);
                project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC)
                        .printInfo("Adding metrics values evolution tree for " + psiJavaFile.getName() + " finished");
//                MetricsUtils.getConsole().info("Adding metrics values evolution tree for " + psiJavaFile.getName() + " finished");
            }
            MetricsUtils.setClassMetricsValuesEvolutionCalculationPerforming(false);
            return null;
        };

        buildTree = new FutureTask<>(buildingTree);

        cancel = () -> {
            project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC)
                    .printInfo("Adding metrics values evolution tree for " + psiJavaFile.getName() + " canceled");
            MetricsUtils.setClassMetricsValuesEvolutionCalculationPerforming(false);
        };
    }

    public void buildClassMetricsValuesEvolutionMap() {
        MetricsBackgroundableTask classMetricsTask = new MetricsBackgroundableTask(psiJavaFile.getProject(),
                "Get Metrics History for " + psiJavaFile.getName() + "...", true,
                getFileFromGitCalculateMetricsAndPutThemToMap, buildTree,
                cancel, null);
        MetricTaskCache.runTask(psiJavaFile.getProject(),classMetricsTask);
    }

    private class ClassMetricsEvolutionEventListener implements MetricsEventListener {
        @Override
        public void cancelMetricsValuesEvolutionCalculation() {
            if (!MetricTaskCache.isQueueEmpty(project)) {
                getFileFromGitCalculateMetricsAndPutThemToMap.cancel(false);
                buildTree.cancel(false);
                project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC)
                        .printInfo("Adding metrics values evolution tree for " + psiJavaFile.getName() + " canceled");
                MetricsUtils.setClassMetricsValuesEvolutionCalculationPerforming(false);
            }
        }
    }
}
