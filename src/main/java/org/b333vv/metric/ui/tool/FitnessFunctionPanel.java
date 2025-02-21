package org.b333vv.metric.ui.tool;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBTabbedPane;
import org.b333vv.metric.ui.info.BottomPanel;

import javax.swing.*;
import java.awt.*;

import static org.b333vv.metric.ui.tool.MetricsToolWindowFactory.TAB_PROFILES;

public class FitnessFunctionPanel extends SimpleToolWindowPanel {

    private final JBTabbedPane tabs = new JBTabbedPane();
    public FitnessFunctionPanel(Project project, ToolWindow toolWindow) {
        super(false, true);
        JBPanel<?> fitnessFunctionPanel = new JBPanel<>(new BorderLayout());
        super.setContent(fitnessFunctionPanel);
//        addProjectFitnessFunctionTab(project, toolWindow);
        addPackageFitnessFunctionTab(project, toolWindow);
        addClassFitnessFunctionTab(project, toolWindow);
        fitnessFunctionPanel.add(ScrollPaneFactory.createScrollPane(tabs), BorderLayout.CENTER);
    }

    private void addProjectFitnessFunctionTab(Project project, ToolWindow toolWindow) {
        var packageLevelFitnessFunctionPanel = new PackageLevelFitnessFunctionPanel(project);
//        var classFitnessFunctionContent = toolWindow.getContentManager().getFactory()
//                .createContent(
//                        classLevelFitnessFunctionPanel, TAB_PROFILES, false);
//        toolWindow.getContentManager().addDataProvider(classLevelFitnessFunctionPanel);
//        toolWindow.getContentManager().addContent(classFitnessFunctionContent);
        tabs.insertTab("Project Level", null, packageLevelFitnessFunctionPanel, "", 0);
    }

    private void addPackageFitnessFunctionTab(Project project, ToolWindow toolWindow) {
        var packageLevelFitnessFunctionPanel = new PackageLevelFitnessFunctionPanel(project);
//        var classFitnessFunctionContent = toolWindow.getContentManager().getFactory()
//                .createContent(
//                        classLevelFitnessFunctionPanel, TAB_PROFILES, false);
//        toolWindow.getContentManager().addDataProvider(classLevelFitnessFunctionPanel);
//        toolWindow.getContentManager().addContent(classFitnessFunctionContent);
        tabs.insertTab("Package Level", null, packageLevelFitnessFunctionPanel, "", 0);
    }

    private void addClassFitnessFunctionTab(Project project, ToolWindow toolWindow) {
        var classLevelFitnessFunctionPanel = new ClassLevelFitnessFunctionPanel(project);
//        var classFitnessFunctionContent = toolWindow.getContentManager().getFactory()
//                .createContent(
//                        classLevelFitnessFunctionPanel, TAB_PROFILES, false);
//        toolWindow.getContentManager().addDataProvider(classLevelFitnessFunctionPanel);
//        toolWindow.getContentManager().addContent(classFitnessFunctionContent);
        tabs.insertTab("Class Level", null, classLevelFitnessFunctionPanel, "", 1);
    }
}
