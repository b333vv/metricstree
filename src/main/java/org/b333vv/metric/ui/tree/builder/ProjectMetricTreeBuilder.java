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

package org.b333vv.metric.ui.tree.builder;

import org.b333vv.metric.model.code.JavaClass;
import org.b333vv.metric.model.code.JavaCode;
import org.b333vv.metric.model.code.JavaPackage;
import org.b333vv.metric.model.code.JavaProject;
import org.b333vv.metric.ui.tree.MetricsTreeFilter;
import org.b333vv.metric.ui.tree.node.*;
import org.b333vv.metric.util.MetricsUtils;
import org.jetbrains.annotations.Nullable;

import javax.swing.tree.DefaultTreeModel;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ProjectMetricTreeBuilder extends MetricTreeBuilder {

    public ProjectMetricTreeBuilder(JavaProject javaProject) {
        super(javaProject);
    }

    @Nullable
    public DefaultTreeModel createMetricTreeModel() {
            JavaProject javaProject = (JavaProject) javaCode;
            ProjectNode projectNode = new ProjectNode(javaProject);
            model = new DefaultTreeModel(projectNode);
            model.setRoot(projectNode);
            if (getMetricsTreeFilter().isProjectMetricsVisible()
                    && getMetricsTreeFilter().isMoodMetricsSetVisible()) {
                javaProject.getMetrics()
                        .filter(this::mustBeShown)
                        .forEach(m -> {
                            MetricNode metricNode = new ProjectMetricNode(m);
                            projectNode.add(metricNode);
                        });
            }
            if (getMetricsTreeFilter().isPackageMetricsVisible()
                    || getMetricsTreeFilter().isClassMetricsVisible()
                    || getMetricsTreeFilter().isMethodMetricsVisible()) {
                javaProject.getPackages()
                        .map(PackageNode::new).forEach(packageNode -> {
                    projectNode.add(packageNode);
                    addPackages(packageNode);
                });
            }
            return model;
    }

    private void addPackages(PackageNode parentNode) {
        List<JavaPackage> sortedPackages = parentNode.getJavaPackage().getPackages()
                .sorted(Comparator.comparing(JavaCode::getName)).collect(Collectors.toList());
        for (JavaPackage javaPackage : sortedPackages) {
            PackageNode packageNode = new PackageNode(javaPackage);
            parentNode.add(packageNode);
            addPackages(packageNode);
            if (getMetricsTreeFilter().isClassMetricsVisible()
                    || getMetricsTreeFilter().isMethodMetricsVisible()) {
                packageNode.getJavaPackage().getFiles()
                        .forEach(f -> {
                            if (f.getClasses().count() > 1) {
                                FileNode fileNode = new FileNode(f);
                                packageNode.add(fileNode);
                                f.getClasses()
                                        .forEach(c -> {
                                            ClassNode classNode = new ClassNode(c);
                                            fileNode.add(classNode);
                                            addSubClasses(classNode);
                                            addTypeMetricsAndMethodNodes(classNode);
                                        });
                            } else if (f.getClasses().findFirst().isPresent()) {
                                JavaClass javaClass = f.getClasses().findFirst().get();
                                ClassNode classNode = new ClassNode(javaClass);
                                packageNode.add(classNode);
                                addSubClasses(classNode);
                                addTypeMetricsAndMethodNodes(classNode);
                            }
                        });
            }
            if (getMetricsTreeFilter().isPackageMetricsVisible()
                    && getMetricsTreeFilter().isRobertMartinMetricsSetVisible()
                    && !javaPackage.getFiles().collect(Collectors.toSet()).isEmpty()) {
                javaPackage.getMetrics()
                        .filter(this::mustBeShown)
                        .forEach(m -> {
                            MetricNode metricNode = new PackageMetricNode(m);
                            packageNode.add(metricNode);
                        });
            }
        }
    }

    @Override
    protected MetricsTreeFilter getMetricsTreeFilter() {
        return MetricsUtils.getProjectMetricsTreeFilter();
    }
}
