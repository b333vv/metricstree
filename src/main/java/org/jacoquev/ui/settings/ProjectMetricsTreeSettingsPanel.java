package org.jacoquev.ui.settings;

import com.intellij.openapi.project.Project;
import com.intellij.util.ui.JBInsets;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;
import java.util.List;

import static java.awt.GridBagConstraints.*;

public class ProjectMetricsTreeSettingsPanel implements ConfigurationPanel<ProjectMetricsTreeSettings> {
    private static final String EMPTY_LABEL = "No metrics configured";
    private final Project project;
    private JPanel panel;
    private MetricsTreeSettingsTable metricsTreeSettingsTable;
    private JPanel tablePanel;
    private JCheckBox needToConsiderProjectMetrics;
    private JCheckBox needToConsiderPackageMetrics;

    public ProjectMetricsTreeSettingsPanel(Project project, ProjectMetricsTreeSettings projectMetricsTreeSettings) {
        this.project = project;
        needToConsiderProjectMetrics =
                new JCheckBox("Project level metrics (MOOD metrics set: MHF, AHF, MIF, AIF, PF, CF) should be calculated",
                        projectMetricsTreeSettings.isNeedToConsiderProjectMetrics());
        needToConsiderPackageMetrics =
                new JCheckBox("Package level metrics (Robert C. Martin metrics set: Ce, Ca, I, A, D) should be calculated",
                        projectMetricsTreeSettings.isNeedToConsiderPackageMetrics());

        createUIComponents(projectMetricsTreeSettings);
    }

    public JPanel getComponent() {
        return panel;
    }

    @Override
    public boolean isModified(ProjectMetricsTreeSettings settings) {
        List<MetricsTreeSettingsStub> rows = settings.getMetricsList();
        return !rows.equals(metricsTreeSettingsTable.get())
                || settings.isNeedToConsiderProjectMetrics() != needToConsiderProjectMetrics.isSelected()
                || settings.isNeedToConsiderPackageMetrics() != needToConsiderPackageMetrics.isSelected();
    }

    @Override
    public void save(ProjectMetricsTreeSettings settings) {
        settings.setProjectTreeMetrics(metricsTreeSettingsTable.get());
        settings.setNeedToConsiderProjectMetrics(needToConsiderProjectMetrics.isSelected());
        settings.setNeedToConsiderPackageMetrics(needToConsiderPackageMetrics.isSelected());
    }

    @Override
    public void load(ProjectMetricsTreeSettings settings) {
        metricsTreeSettingsTable.set(settings.getMetricsList());
        needToConsiderProjectMetrics.setSelected(settings.isNeedToConsiderProjectMetrics());
        needToConsiderPackageMetrics.setSelected(settings.isNeedToConsiderPackageMetrics());
    }

    private void createUIComponents(ProjectMetricsTreeSettings projectMetricsTreeSettings) {
        metricsTreeSettingsTable = new MetricsTreeSettingsTable(EMPTY_LABEL, project, projectMetricsTreeSettings.getMetricsList());

        panel = new JPanel(new GridBagLayout());

        JBInsets insets = JBUI.insets(2, 2, 2, 2);

        tablePanel = metricsTreeSettingsTable.getComponent();

        panel.add(needToConsiderProjectMetrics, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                NORTHWEST, NONE, insets, 0, 0));
        panel.add(needToConsiderPackageMetrics, new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0,
                NORTHWEST, NONE, insets, 0, 0));
        panel.add(tablePanel, new GridBagConstraints(0, 2, 4, 2, 1.0, 1.0,
                NORTHWEST, BOTH, insets, 40, 40));
    }
}
