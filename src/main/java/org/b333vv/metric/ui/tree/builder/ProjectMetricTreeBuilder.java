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

import com.intellij.icons.AllIcons;
import org.b333vv.metric.model.code.JavaClass;
import org.b333vv.metric.model.code.JavaCode;
import org.b333vv.metric.model.code.JavaPackage;
import org.b333vv.metric.model.code.JavaProject;
import org.b333vv.metric.ui.tree.MetricsTreeFilter;
import org.b333vv.metric.ui.tree.node.*;
import org.b333vv.metric.util.MetricsService;
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
            ProjectNode projectNode = new ProjectNode(javaProject, "whole project metrics", AllIcons.Nodes.Project);
            model = new DefaultTreeModel(projectNode);
            model.setRoot(projectNode);
            if (getMetricsTreeFilter().isProjectMetricsVisible()
                    && getMetricsTreeFilter().isMoodMetricsSetVisible()) {
                javaProject.metrics()
                        .filter(this::mustBeShown)
                        .map(ProjectMetricNode::new)
                        .forEach(projectNode::add);
            }
            if (getMetricsTreeFilter().isPackageMetricsVisible()
                    || getMetricsTreeFilter().isClassMetricsVisible()
                    || getMetricsTreeFilter().isMethodMetricsVisible()) {
                javaProject.packages()
                        .map(PackageNode::new)
                        .forEach(packageNode -> {
                            projectNode.add(packageNode);
                            addPackages(packageNode);
                        });
            }
            return model;
    }

    private void addPackages(PackageNode parentNode) {
        List<JavaPackage> sortedPackages = parentNode.getJavaPackage().subPackages().collect(Collectors.toList());
        for (JavaPackage javaPackage : sortedPackages) {
            PackageNode packageNode = new PackageNode(javaPackage);
            parentNode.add(packageNode);
            addPackages(packageNode);
        }
        addJavaFiles(parentNode);
        addPackageMetrics(parentNode);
    }

    private void addPackageMetrics(PackageNode packageNode) {
        if (getMetricsTreeFilter().isPackageMetricsVisible()
                && getMetricsTreeFilter().isRobertMartinMetricsSetVisible()
                && !packageNode.getJavaPackage().files().collect(Collectors.toSet()).isEmpty()) {
            packageNode.getJavaPackage().metrics()
                    .filter(this::mustBeShown)
                    .map(PackageMetricNode::new)
                    .forEach(packageNode::add);
        }
    }

    private void addJavaFiles(PackageNode parentNode) {
        if (getMetricsTreeFilter().isClassMetricsVisible()
                || getMetricsTreeFilter().isMethodMetricsVisible()) {
            parentNode.getJavaPackage().files()
                    .forEach(f -> {
                        if (f.classes().count() > 1) {
                            FileNode fileNode = new FileNode(f);
                            parentNode.add(fileNode);
                            f.classes()
                                    .map(ClassNode::new)
                                    .forEach(c -> {
                                        fileNode.add(c);
                                        addSubClasses(c);
                                        addTypeMetricsAndMethodNodes(c);
                                    });
                        } else if (f.classes().findFirst().isPresent()) {
                            JavaClass javaClass = f.classes().findFirst().get();
                            ClassNode classNode = new ClassNode(javaClass);
                            parentNode.add(classNode);
                            addSubClasses(classNode);
                            addTypeMetricsAndMethodNodes(classNode);
                        }
                    });
        }
    }

    @Override
    protected MetricsTreeFilter getMetricsTreeFilter() {
        return MetricsUtils.getProjectMetricsTreeFilter();
    }
}
