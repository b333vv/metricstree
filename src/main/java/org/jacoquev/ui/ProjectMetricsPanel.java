
package org.jacoquev.ui;

import com.intellij.analysis.AnalysisScope;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.ui.Splitter;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.components.JBPanel;
import org.jacoquev.model.code.*;
import org.jacoquev.model.metric.Metric;
import org.jacoquev.exec.ProjectMetricsRunner;
import org.jacoquev.ui.info.BottomPanel;
import org.jacoquev.ui.info.MetricsDescriptionPanel;
import org.jacoquev.ui.info.MetricsTable;
import org.jacoquev.ui.log.MetricsConsole;
import org.jacoquev.ui.tree.MetricsTree;
import org.jacoquev.ui.tree.builder.ProjectMetricTreeBuilder;
import org.jacoquev.ui.tree.node.*;
import org.jacoquev.util.EditorOpener;
import org.jacoquev.util.MetricsUtils;
import org.jetbrains.annotations.NonNls;

import javax.annotation.Nullable;
import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;

public class ProjectMetricsPanel extends SimpleToolWindowPanel {
    private static final String SPLIT_PROPORTION_PROPERTY = "SPLIT_PROPORTION";
    private static final Logger LOG = Logger.getInstance(ProjectMetricsPanel.class);
    private final Project project;
    private final MetricsTree metricsTree;
    private BottomPanel bottomPanel;
    private MetricsDescriptionPanel metricsDescriptionPanel;
    private JBPanel rightPanel;
    private MetricsTable metricsTable;
    private ProjectMetricTreeBuilder projectMetricTreeBuilder;
    private MetricsConsole console;
    private JScrollPane scrollableTablePanel;
    private VirtualFile virtualFile;
    private JavaProject javaProject;

    public ProjectMetricsPanel(Project project) {
        super(false, true);

        this.project = project;

        final ActionManager actionManager = ActionManager.getInstance();
        ActionToolbar actionToolbar = actionManager.createActionToolbar("Metrics Toolbar",
                (DefaultActionGroup) actionManager.getAction("Metrics.ProjectMetricsToolbar"), false);
        actionToolbar.setOrientation(SwingConstants.VERTICAL);
        setToolbar(actionToolbar.getComponent());

        metricsTree = new MetricsTree(null);
        metricsTree.addTreeSelectionListener(e -> treeSelectionChanged());
        bottomPanel = new BottomPanel();
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(ScrollPaneFactory.createScrollPane(metricsTree), BorderLayout.CENTER);
        mainPanel.add(bottomPanel.getPanel(), BorderLayout.SOUTH);

        createTabs();

        super.setContent(createSplitter(mainPanel, rightPanel, SPLIT_PROPORTION_PROPERTY, false, 0.65f));

        console = MetricsUtils.get(project, MetricsConsole.class);

        subscribeToEvents();

        MetricsUtils.setProjectMetricsPanel(this);
    }

    private void createTabs() {
        metricsDescriptionPanel = new MetricsDescriptionPanel();
        JScrollPane scrollableMetricPanel = ScrollPaneFactory.createScrollPane(
                metricsDescriptionPanel.getPanel(),
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollableMetricPanel.getVerticalScrollBar().setUnitIncrement(10);

        metricsTable = new MetricsTable();
        scrollableTablePanel = ScrollPaneFactory.createScrollPane(
                metricsTable.getComponent(),
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollableTablePanel.getVerticalScrollBar().setUnitIncrement(10);
        scrollableTablePanel.getHorizontalScrollBar().setUnitIncrement(10);

        rightPanel = new JBPanel(new BorderLayout());
        rightPanel.add(scrollableTablePanel);
    }

    protected JComponent createSplitter(JComponent c1, JComponent c2, String proportionProperty, boolean vertical, float defaultSplit) {
        float savedProportion = PropertiesComponent.getInstance(project).getFloat(proportionProperty, defaultSplit);

        final Splitter splitter = new Splitter(vertical);
        splitter.setFirstComponent(c1);
        splitter.setSecondComponent(c2);
        splitter.setProportion(savedProportion);
        splitter.setHonorComponentsMinimumSize(true);
        splitter.addPropertyChangeListener(Splitter.PROP_PROPORTION,
                evt -> PropertiesComponent.getInstance(project).setValue(proportionProperty, Float.toString(splitter.getProportion())));

        return splitter;
    }

    private void subscribeToEvents() {
    }

    @Nullable
    @Override
    public Object getData(@NonNls String dataId) {
        if (CommonDataKeys.VIRTUAL_FILE.is(dataId)) {
            return MetricsUtils.getSelectedFile(project);
        }
        return null;
    }

    public void calculate() {
        javaProject = new JavaProject(project.getName());
        console.info("Evaluating metrics values for project: " + project.getName());
        AnalysisScope analysisScope = new AnalysisScope(project);
        analysisScope.setIncludeTestSource(false);
        ProjectMetricsRunner projectMetricsRunner = new ProjectMetricsRunner(project, analysisScope, javaProject);
        projectMetricsRunner.execute();
        projectMetricTreeBuilder = new ProjectMetricTreeBuilder(javaProject);
    }

    public void buildTreeModel() {
        DefaultTreeModel metricsTreeModel = projectMetricTreeBuilder.createProjectMetricTreeModel();
        showResults(metricsTreeModel);
    }

    public void treeSelectionChanged() {
        AbstractNode node = metricsTree.getSelectedNode();
        if (node instanceof MetricNode) {
            Metric metric = ((MetricNode) node).getMetric();
            bottomPanel.setData(metric);
            metricsDescriptionPanel.setMetric(metric);
            metricsTable.clear();
            rightPanel.remove(0);
            rightPanel.add(metricsDescriptionPanel.getPanel());
            rightPanel.revalidate();
            rightPanel.repaint();
        } else if (node instanceof ProjectNode) {
            JavaProject javaProject = ((ProjectNode) node).getJavaProject();
            bottomPanel.setData(javaProject);
            metricsTable.set(javaProject);
            rightPanelRepaint();
        } else if (node instanceof PackageNode) {
            JavaPackage javaPackage = ((PackageNode) node).getJavaPackage();
            bottomPanel.setData(javaPackage);
            metricsTable.set(javaPackage);
            rightPanelRepaint();
        } else if (node instanceof ClassNode) {
            JavaClass javaClass = ((ClassNode) node).getJavaClass();
            bottomPanel.setData(javaClass);
            metricsTable.set(javaClass);
            rightPanelRepaint();
            openInEditor(javaClass.getPsiClass());
        } else if (node instanceof MethodNode) {
            JavaMethod javaMethod = ((MethodNode) node).getJavaMethod();
            bottomPanel.setData(javaMethod);
            metricsTable.set(javaMethod);
            rightPanelRepaint();
            openInEditor(javaMethod.getPsiMethod());
        } else {
            bottomPanel.clear();
            metricsDescriptionPanel.clear();
            metricsTable.clear();
        }
    }

    private void rightPanelRepaint() {
        metricsDescriptionPanel.clear();
        rightPanel.remove(0);
        rightPanel.add(scrollableTablePanel);
        rightPanel.revalidate();
        rightPanel.repaint();
    }

    private void openInEditor(PsiElement psiElement) {
        if (MetricsUtils.isAutoscroll()) {
            final EditorOpener caretMover = new EditorOpener(project);
            if (psiElement != null) {
                Editor editor = caretMover.openInEditor(psiElement);
                if (editor != null) {
                    caretMover.moveEditorCaret(psiElement);
                }
            }
        }
    }

    public MetricsConsole getConsole() {
        return console;
    }

    public void showResults(DefaultTreeModel metricsTreeModel) {
        metricsTree.setModel(metricsTreeModel);
        if (metricsTreeModel == null) {
            metricsTable.clear();
        } else {
            metricsTable.init(javaProject);
        }
    }
}
