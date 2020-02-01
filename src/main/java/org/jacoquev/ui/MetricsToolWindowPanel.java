
package org.jacoquev.ui;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.RangeMarker;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.ui.Splitter;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiMethod;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBTabbedPane;
import org.jacoquev.model.code.JavaClass;
import org.jacoquev.model.code.JavaMethod;
import org.jacoquev.model.code.JavaProject;
import org.jacoquev.model.code.ModelBuilder;
import org.jacoquev.model.metric.Metric;
import org.jacoquev.ui.info.BottomPanel;
import org.jacoquev.ui.info.MetricsDescriptionPanel;
import org.jacoquev.ui.info.MetricsTable;
import org.jacoquev.ui.log.MetricsConsole;
import org.jacoquev.ui.tree.*;
import org.jacoquev.util.MetricsUtils;
import org.jacoquev.util.RunnerFactory;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;

public class MetricsToolWindowPanel extends SimpleToolWindowPanel {
    private static final String SPLIT_PROPORTION_PROPERTY = "SPLIT_PROPORTION";
    private static final Logger LOG = Logger.getInstance(MetricsToolWindowPanel.class);
    private final CurrentFileController scope;
    private final Project project;
    private final MetricsTree metricsTree;
    private BottomPanel bottomPanel;
    private MetricsDescriptionPanel metricsDescriptionPanel;
    private JBTabbedPane detailsTab;
    private JBPanel rightPanel;
    private MetricsTable metricsTable;
    private MetricTreeBuilder metricTreeBuilder;
    private MetricsConsole console;
    private JScrollPane scrollableTablePanel;
    private VirtualFile virtualFile;

    public MetricsToolWindowPanel(CurrentFileController scope, Project project) {
        super(false, true);

        this.project = project;
        this.scope = scope;

        MetricsUtils.setMetricsToolWindowPanel(this);

        final ActionManager actionManager = ActionManager.getInstance();
        ActionToolbar actionToolbar = actionManager.createActionToolbar("Metrics Toolbar",
                (DefaultActionGroup) actionManager.getAction("Metrics.ProcessesToolbar"), false);
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
        scope.setPanel(this);
    }

    @Nullable
    @Override
    public Object getData(@NonNls String dataId) {
        if (CommonDataKeys.VIRTUAL_FILE.is(dataId)) {
            return MetricsUtils.getSelectedFile(project);
        }
        return null;
    }

    public void update(@NotNull VirtualFile file) {
        virtualFile = file;
        MetricsUtils.getDumbService().runWhenSmart(() -> calculateMetrics(file));
    }

    public void refresh() {
        if (virtualFile != null) {
            MetricsUtils.getDumbService().runWhenSmart(() -> calculateMetrics(virtualFile));
        }
    }

    private void calculateMetrics(@NotNull VirtualFile virtualFile) {
        ModelBuilder modelBuilder = new ModelBuilder(project);
        JavaProject javaProject = modelBuilder.buildJavaProject(virtualFile);
        RunnerFactory.getProcessor().run(javaProject);
        metricTreeBuilder = new MetricTreeBuilder(javaProject);
        buildTreeModel();
        console.info("Evaluating metrics values for " + virtualFile.getPresentableName());
    }

    public void buildTreeModel() {
        DefaultTreeModel metricsTreeModel = metricTreeBuilder.createMetricTreeModel();
        metricsTree.setModel(metricsTreeModel);
        metricsTable.init(metricTreeBuilder.getJavaProject());
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
        } else if (node instanceof ClassNode) {
            bottomPanel.setData(((ClassNode) node).getJavaClass());
            metricsDescriptionPanel.clear();
            JavaClass javaClass = ((ClassNode) node).getJavaClass();
            metricsTable.set(javaClass);
            rightPanel.remove(0);
            rightPanel.add(scrollableTablePanel);
            rightPanel.revalidate();
            rightPanel.repaint();
        } else if (node instanceof MethodNode) {
            bottomPanel.setData(((MethodNode) node).getJavaMethod());
            metricsDescriptionPanel.clear();
            JavaMethod javaMethod = ((MethodNode) node).getJavaMethod();
            metricsTable.set(javaMethod);
            rightPanel.remove(0);
            rightPanel.add(scrollableTablePanel);
            rightPanel.revalidate();
            rightPanel.repaint();
        } else {
            bottomPanel.clear();
            metricsDescriptionPanel.clear();
            metricsTable.clear();
        }
    }

    public MetricsConsole getConsole() {
        return console;
    }
}
