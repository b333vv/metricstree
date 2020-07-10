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

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.ui.Splitter;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiJavaFile;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.components.JBPanel;
import org.b333vv.metric.model.code.*;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.ui.info.BottomPanel;
import org.b333vv.metric.ui.info.MetricsDescriptionPanel;
import org.b333vv.metric.ui.info.MetricsSummaryTable;
import org.b333vv.metric.ui.tree.MetricsTree;
import org.b333vv.metric.ui.tree.builder.MetricTreeBuilder;
import org.b333vv.metric.ui.tree.node.*;
import org.b333vv.metric.util.MetricsUtils;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;

public abstract class MetricsTreePanel extends SimpleToolWindowPanel {
    private static final String SPLIT_PROPORTION_PROPERTY = "SPLIT_PROPORTION";

    private MetricsTree metricsTree;
    private BottomPanel bottomPanel;
    private MetricsDescriptionPanel metricsDescriptionPanel;
    private JBPanel<?> rightPanel;
    private MetricsSummaryTable metricsSummaryTable;
    private JPanel mainPanel;
    private JScrollPane scrollableTablePanel;

    protected final Project project;

    protected MetricTreeBuilder metricTreeBuilder;
    protected PsiJavaFile psiJavaFile;

    public MetricsTreePanel(Project project, String actionId) {
        super(false, true);
        this.project = project;
        createUIComponents();
        ActionManager actionManager = ActionManager.getInstance();
        ActionToolbar actionToolbar = actionManager.createActionToolbar("Metrics Toolbar",
                (DefaultActionGroup) actionManager.getAction(actionId), false);
        actionToolbar.setOrientation(SwingConstants.VERTICAL);
        setToolbar(actionToolbar.getComponent());
    }

    final protected void createUIComponents() {
        metricsTree = new MetricsTree(null);
        metricsTree.addTreeSelectionListener(e -> treeSelectionChanged());
        bottomPanel = new BottomPanel();
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(ScrollPaneFactory.createScrollPane(metricsTree), BorderLayout.CENTER);
        mainPanel.add(bottomPanel.getPanel(), BorderLayout.SOUTH);
        createRightPanels();
        super.setContent(createSplitter(mainPanel, rightPanel));
    }

    private void createRightPanels() {
        metricsDescriptionPanel = new MetricsDescriptionPanel();
        JScrollPane scrollableMetricPanel = ScrollPaneFactory.createScrollPane(
                metricsDescriptionPanel.getPanel(),
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollableMetricPanel.getVerticalScrollBar().setUnitIncrement(10);

        metricsSummaryTable = new MetricsSummaryTable(true);
        scrollableTablePanel = ScrollPaneFactory.createScrollPane(
                metricsSummaryTable.getComponent(),
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollableTablePanel.getVerticalScrollBar().setUnitIncrement(10);
        scrollableTablePanel.getHorizontalScrollBar().setUnitIncrement(10);

        rightPanel = new JBPanel<>(new BorderLayout());
        rightPanel.add(scrollableTablePanel);
    }

    private JComponent createSplitter(JComponent c1, JComponent c2) {
        float savedProportion = PropertiesComponent.getInstance(project)
                .getFloat(MetricsTreePanel.SPLIT_PROPORTION_PROPERTY, (float) 0.65);

        final JBSplitter splitter = new JBSplitter(false);
        splitter.setFirstComponent(c1);
        splitter.setSecondComponent(c2);
        splitter.setProportion(savedProportion);
        splitter.setHonorComponentsMinimumSize(true);
        splitter.addPropertyChangeListener(Splitter.PROP_PROPORTION,
                evt -> PropertiesComponent.getInstance(project).setValue(MetricsTreePanel.SPLIT_PROPORTION_PROPERTY,
                        Float.toString(splitter.getProportion())));
        return splitter;
    }

    @Nullable
    @Override
    public Object getData(@NotNull String dataId) {
        if (CommonDataKeys.VIRTUAL_FILE.is(dataId)) {
            return MetricsUtils.getSelectedFile(project);
        }
        return null;
    }

    private void treeSelectionChanged() {
        AbstractNode node = metricsTree.getSelectedNode();
        if (node instanceof MetricNode) {
            Metric metric = ((MetricNode) node).getMetric();
            bottomPanel.setData(metric);
            metricsDescriptionPanel.setMetric(metric);
            metricsSummaryTable.clear();
            rightPanel.remove(0);
            rightPanel.add(metricsDescriptionPanel.getPanel());
            rightPanel.revalidate();
            rightPanel.repaint();
        } else if (node instanceof MetricTypeNode) {
            MetricType metricType = ((MetricTypeNode) node).getMetricType();
            bottomPanel.setData(metricType);
            metricsDescriptionPanel.setMetric(metricType);
            metricsSummaryTable.clear();
            rightPanel.remove(0);
            rightPanel.add(metricsDescriptionPanel.getPanel());
            rightPanel.revalidate();
            rightPanel.repaint();
        } else if (node instanceof ProjectNode) {
            JavaProject jProject = ((ProjectNode) node).getJavaProject();
            bottomPanel.setData(jProject);
            metricsSummaryTable.set(jProject);
            rightPanelRepaint();
        } else if (node instanceof PackageNode) {
            JavaPackage javaPackage = ((PackageNode) node).getJavaPackage();
            bottomPanel.setData(javaPackage);
            metricsSummaryTable.set(javaPackage);
            rightPanelRepaint();
        } else if (node instanceof FileNode) {
            JavaFile javaFile = ((FileNode) node).getJavaFile();
            bottomPanel.setData(javaFile);
            metricsSummaryTable.clear();
            rightPanelRepaint();
        } else if (node instanceof ClassNode) {
            JavaClass javaClass = ((ClassNode) node).getJavaClass();
            bottomPanel.setData(javaClass);
            metricsSummaryTable.set(javaClass);
            rightPanelRepaint();
            openInEditor(javaClass.getPsiClass());
        } else if (node instanceof MethodNode) {
            JavaMethod javaMethod = ((MethodNode) node).getJavaMethod();
            bottomPanel.setData(javaMethod);
            metricsSummaryTable.set(javaMethod);
            rightPanelRepaint();
            openInEditor(javaMethod.getPsiMethod());
        } else {
            bottomPanel.clear();
            metricsDescriptionPanel.clear();
            metricsSummaryTable.clear();
        }
    }

    private void rightPanelRepaint() {
        metricsDescriptionPanel.clear();
        rightPanel.remove(0);
        rightPanel.add(scrollableTablePanel);
        rightPanel.revalidate();
        rightPanel.repaint();
    }

    protected void openInEditor(PsiElement psiElement) {}

    protected void buildTreeModel() {
        DefaultTreeModel metricsTreeModel = metricTreeBuilder.createMetricTreeModel();
        showResults(metricsTreeModel);
    }

    protected void showResults(DefaultTreeModel metricsTreeModel) {
        metricsTree.setModel(metricsTreeModel);
        if (metricsTreeModel == null) {
            metricsSummaryTable.clear();
        } else {
            metricsTree.setSelectionPath(new TreePath(metricsTreeModel.getRoot()));
        }
    }

    protected void clear() {
        rightPanel.removeAll();
        mainPanel.removeAll();
        updateUI();
        createUIComponents();
    }

    abstract public void update(@NotNull PsiJavaFile file);
}
