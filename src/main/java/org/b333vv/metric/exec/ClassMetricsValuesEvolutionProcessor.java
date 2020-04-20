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
import org.b333vv.metric.model.code.JavaFile;
import org.b333vv.metric.ui.tree.builder.ClassMetricsValuesEvolutionTreeBuilder;
import org.b333vv.metric.util.MetricsUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.DefaultTreeModel;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClassMetricsValuesEvolutionProcessor {

    private final PsiJavaFile psiJavaFile;
    private final Runnable getFileFromGitCalculateMetricsAndPutThemToMap;
    private final Runnable buildTree;
    private final Runnable cancel;
    private final BackgroundTaskQueue queue;
    private final Map<TimedVcsCommit, JavaClass> classMetricsEvolution = new ConcurrentHashMap<>();
    private final ClassModelBuilder classModelBuilder;

    private DefaultTreeModel metricsTreeModel;

    public ClassMetricsValuesEvolutionProcessor(@NotNull PsiJavaFile psiJavaFile) {

        this.psiJavaFile = psiJavaFile;
        classModelBuilder = new ClassModelBuilder();
        queue = new BackgroundTaskQueue(psiJavaFile.getProject(), "Get Metrics Values Evolution");

        MetricsUtils.getConsole().info("Building metrics values evolution tree for " + psiJavaFile.getName() + " started");

        getFileFromGitCalculateMetricsAndPutThemToMap = () ->
        {
            if (!GitUtil.isUnderGit(psiJavaFile.getVirtualFile())) {
                return;
            }
            MetricsUtils.setClassMetricsValuesEvolutionCalculationPerforming(true);

            GitRepositoryManager gitRepositoryManager = GitUtil.getRepositoryManager(psiJavaFile.getProject());
            VirtualFile root = gitRepositoryManager.getRepositoryForFile(psiJavaFile.getVirtualFile()).getRoot();
            List<? extends TimedVcsCommit> commits;

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
                        JavaFile javaFile = classModelBuilder.buildJavaFile(gitPsiJavaFile);
                        javaFile.getClasses()
                                .forEach(c -> classMetricsEvolution.put(commit, c));
                    });
                }
            } catch (VcsException e) {
                MetricsUtils.getConsole().error(e.getMessage());
            }
        };

        buildTree = () -> {
            ClassModelBuilder classModelBuilder = new ClassModelBuilder();
            JavaFile javaFile = classModelBuilder.buildJavaFile(psiJavaFile);
            ClassMetricsValuesEvolutionTreeBuilder classMetricsValuesEvolutionTreeBuilder =
                    new ClassMetricsValuesEvolutionTreeBuilder(javaFile, Collections.unmodifiableMap(classMetricsEvolution));
            metricsTreeModel = classMetricsValuesEvolutionTreeBuilder.createMetricsValuesEvolutionTreeModel();
            if (metricsTreeModel != null) {
                psiJavaFile.getProject().getMessageBus().syncPublisher(MetricsEventListener.TOPIC)
                        .classMetricsValuesEvolutionCalculated(metricsTreeModel);
                MetricsUtils.getConsole().info("Building metrics values evolution tree for " + psiJavaFile.getName() + " finished");
            }
            MetricsUtils.setClassMetricsValuesEvolutionCalculationPerforming(false);
        };

        cancel = () -> {
            queue.clear();
            MetricsUtils.getConsole().info("Building metrics values evolution tree for " + psiJavaFile.getName() + " canceled");
            MetricsUtils.setClassMetricsValuesEvolutionCalculationPerforming(false);
        };
    }

    public void buildClassMetricsValuesEvolutionMap() {
        MetricsBackgroundableTask classMetricsTask = new MetricsBackgroundableTask(psiJavaFile.getProject(),
                "Get Metrics History for " + psiJavaFile.getName() + "...", true,
                getFileFromGitCalculateMetricsAndPutThemToMap, buildTree,
                cancel, null);
        queue.run(classMetricsTask);
    }
}
