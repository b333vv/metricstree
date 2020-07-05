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

package org.b333vv.metric.exec.manager;

import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.ModificationTracker;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import org.b333vv.metric.model.code.JavaProject;
import org.b333vv.metric.ui.tree.builder.ProjectMetricTreeBuilder;
import org.b333vv.metric.util.MetricsUtils;

import javax.swing.tree.DefaultTreeModel;
import java.util.function.Function;

public class BuildProjectTreeTask implements Function<JavaProject, DefaultTreeModel> {
//    public DefaultTreeModel getProjectTree(JavaProject javaProject) {
//        final CachedValuesManager manager = CachedValuesManager.getManager(MetricsUtils.getCurrentProject());
//        final Object[] dependencies = {PsiModificationTracker.MODIFICATION_COUNT, ModificationTracker.EVER_CHANGED,
//                ProjectRootManager.getInstance(MetricsUtils.getCurrentProject())};
//            instance().projectTree = manager.createCachedValue(new CachedValueProvider<DefaultTreeModel>() {
//                public Result<DefaultTreeModel> compute() {
//                    ProjectMetricTreeBuilder projectMetricTreeBuilder = new ProjectMetricTreeBuilder(javaProject);
//                    DefaultTreeModel metricsTreeModel = projectMetricTreeBuilder.createMetricTreeModel();
//                    return Result.create(metricsTreeModel, dependencies);
//                }
//            }, false);
////        project.getUserData()
//        return projectTree.getValue();
//    }

    @Override
    public DefaultTreeModel apply(JavaProject javaProject) {
        return null;
    }
}
