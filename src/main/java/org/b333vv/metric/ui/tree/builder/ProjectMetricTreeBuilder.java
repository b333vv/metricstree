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
import com.intellij.openapi.project.Project;
import org.b333vv.metric.model.code.*;
import org.b333vv.metric.model.code.CodeElement;
import org.b333vv.metric.model.code.ProjectElement;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.MetricLevel;
import org.b333vv.metric.model.metric.MetricSet;
import org.b333vv.metric.service.UIStateService;
import org.b333vv.metric.ui.tree.MetricsTreeFilter;
import org.b333vv.metric.ui.tree.node.*;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static icons.MetricsIcons.CLASS_METRIC;
import static icons.MetricsIcons.PROJECT_METRIC;

public class ProjectMetricTreeBuilder extends MetricTreeBuilder {
    public ProjectMetricTreeBuilder(CodeElement codeElement, Project project) {
        super(codeElement, project);
    }

//    public ProjectMetricTreeBuilder(projectElement projectElement) {
//        super(projectElement);
//    }

    @Nullable
    public DefaultTreeModel createMetricTreeModel() {
            ProjectElement projectElement = (ProjectElement) codeElement;
            ProjectNode projectNode = new ProjectNode(projectElement, "Project Metrics", AllIcons.Nodes.Project);
            model = new DefaultTreeModel(projectNode);
            model.setRoot(projectNode);
            if (getMetricsTreeFilter().isProjectMetricsVisible()) {
                if (getMetricsTreeFilter().isMetricsGroupedByMetricSets()) {
                    for (MetricSet metricSet : MetricSet.values()) {
                        if (!getMetricsTreeFilter().isMoodMetricsSetVisible() && metricSet == MetricSet.MOOD) {
                            continue;
                        }
                        if (metricSet.level() == MetricLevel.PROJECT || metricSet.level() == MetricLevel.PROJECT_PACKAGE) {
                            MetricsSetNode metricsSetNode = new MetricsSetNode(metricSet, PROJECT_METRIC);
                            projectNode.add(metricsSetNode);
                            addMetrics(projectElement.metrics()
                                            .filter(m -> m.getType().set() == metricSet),
                                    metricsSetNode,
                                    PROJECT_METRIC);
                        }
                    }
                } else {
                    if (getMetricsTreeFilter().isMoodMetricsSetVisible()) {
                        addMetrics(projectElement.metrics(), projectNode, PROJECT_METRIC);
                    }
                    else {
                        addMetrics(projectElement.metrics()
                                .filter(m -> m.getType().set() == MetricSet.STATISTIC), projectNode, PROJECT_METRIC);
                    }
                }
            }
 
            if (getMetricsTreeFilter().isPackageMetricsVisible()
                    || getMetricsTreeFilter().isClassMetricsVisible()
                    || getMetricsTreeFilter().isMethodMetricsVisible()) {
                projectElement.packages()
                        .map(PackageNode::new)
                        .forEach(packageNode -> {
                            projectNode.add(packageNode);
                            addPackages(packageNode);
                        });
            }
            return model;
    }

    private void addMetrics(Stream<Metric> metrics, AbstractNode node, Icon icon) {
                metrics
                    .filter(this::mustBeShown)
                    .map(m -> new MetricNode(m, icon, project))
                    .forEach(node::add);
    }

    private void addPackages(PackageNode parentNode) {
        List<PackageElement> sortedPackages = parentNode.getJavaPackage().subPackages().collect(Collectors.toList());
        for (PackageElement javaPackage : sortedPackages) {
            PackageNode packageNode = new PackageNode(javaPackage);
            parentNode.add(packageNode);
            addPackages(packageNode);
        }
        addJavaFiles(parentNode);
        addPackageMetrics(parentNode);
    }

    private void addPackageMetrics(PackageNode packageNode) {
        if (getMetricsTreeFilter().isPackageMetricsVisible()
                && !packageNode.getJavaPackage().files().collect(Collectors.toSet()).isEmpty()) {
            if (getMetricsTreeFilter().isMetricsGroupedByMetricSets()) {
                for (MetricSet metricSet : MetricSet.values()) {
                    if (!getMetricsTreeFilter().isRobertMartinMetricsSetVisible() && metricSet == MetricSet.R_MARTIN) {
                        continue;
                    }
                    if (metricSet.level() == MetricLevel.PACKAGE || metricSet.level() == MetricLevel.PROJECT_PACKAGE) {
                        MetricsSetNode metricsSetNode = new MetricsSetNode(metricSet, PROJECT_METRIC);
                        packageNode.add(metricsSetNode);
                        packageNode.getJavaPackage().metrics()
                                .filter(m -> m.getType().set() == metricSet)
                                .filter(this::mustBeShown)
                                .map(m -> new ProjectMetricNode(m, project))
                                .forEach(metricsSetNode::add);
                    }
                }
            }
            else {
                if (getMetricsTreeFilter().isRobertMartinMetricsSetVisible()) {
                    addMetrics(packageNode.getJavaPackage().metrics(), packageNode, PROJECT_METRIC);
                }
                else {
                    addMetrics(packageNode.getJavaPackage().metrics()
                            .filter(m -> m.getType().set() == MetricSet.STATISTIC), packageNode, PROJECT_METRIC);
                }
            }
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
                                        addMethodNodes(c);
                                        addTypeMetrics(c);
                                    });
                        } else if (f.classes().findFirst().isPresent()) {
                            ClassElement javaClass = f.classes().findFirst().get();
                            ClassNode classNode = new ClassNode(javaClass);
                            parentNode.add(classNode);
                            addSubClasses(classNode);
                            addTypeMetrics(classNode);
                            addMethodNodes(classNode);
                        }
                    });
        }
    }


    private void addMethodNodes(ClassNode classNode) {
        if (getMetricsTreeFilter().isMethodMetricsVisible()) {
            classNode.getJavaClass().methods()
                    .map(MethodNode::new)
                    .forEach(m -> {
                        classNode.add(m);
                        if (getMetricsTreeFilter().isMethodMetricsVisible()) {
                            addMethodMetricsNodes(m);
                        }});
        }

    }

    private void addTypeMetrics(ClassNode classNode) {
        if (getMetricsTreeFilter().isClassMetricsVisible()) {
            if (getMetricsTreeFilter().isMetricsGroupedByMetricSets()) {
                for (MetricSet metricSet : MetricSet.values()) {
                    if (metricSet.level() == MetricLevel.CLASS) {
                        MetricsSetNode metricsSetNode = new MetricsSetNode(metricSet, CLASS_METRIC);
                        classNode.add(metricsSetNode);
                        classNode.getJavaClass().metrics()
                                .filter(m -> m.getType().set() == metricSet)
                                .filter(m -> mustBeShown(m) && checkClassMetricsSets(m.getType()))
                                .map(m -> new ClassMetricNode(m, project))
                                .forEach(m -> {
                                    metricsSetNode.add(m);
                                    storeMetric(classNode, m);
                                });
                    }
                }
            }
            else {
                classNode.getJavaClass().metrics()
                        .filter(m -> mustBeShown(m) && checkClassMetricsSets(m.getType()))
                        .map(m -> new ClassMetricNode(m, project))
                        .forEach(m -> {
                            classNode.add(m);
                            storeMetric(classNode, m);
                        });
            }
        }
    }

    @Override
    protected void addMethodMetricsNodes(MethodNode methodNode) {
        methodNode.getJavaMethod().metrics()
                .filter(this::mustBeShown)
                .map(m -> new MethodMetricNode(m, project))
                .forEach(m -> {
                    methodNode.add(m);
                    storeMetric(methodNode, m);
                });
    }

    @Override
    protected MetricsTreeFilter getMetricsTreeFilter() {
        return project.getService(UIStateService.class).getProjectMetricsTreeFilter();
    }
}
