package org.jacoquev.ui.settings;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nls;

import javax.annotation.Nullable;
import javax.swing.*;

public class MetricsConfigurable implements Configurable, Configurable.NoMargin, Configurable.NoScroll {
    private final Project project;
    private final MetricsAllowableValueRanges metricsAllowableValueRanges;
    private final ClassMetricsTreeSettings classMetricsTreeSettings;
    private final ProjectMetricsTreeSettings projectMetricsTreeSettings;

    private SettingsPanel panel;

    public MetricsConfigurable(Project project) {
        this.project = project;
        this.metricsAllowableValueRanges = project.getComponent(MetricsAllowableValueRanges.class);
        this.classMetricsTreeSettings = project.getComponent(ClassMetricsTreeSettings.class);
        this.projectMetricsTreeSettings = project.getComponent(ProjectMetricsTreeSettings.class);
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "Jacoquev";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return null;
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        if (panel == null) {
            panel = new SettingsPanel(project);
        }
        return panel.getRootPane();
    }

    @Override
    public boolean isModified() {
        return panel != null && (panel.isModified(metricsAllowableValueRanges)
                || panel.isModified(classMetricsTreeSettings)
                || panel.isModified(projectMetricsTreeSettings));
    }

    @Override
    public void apply() {
        if (panel != null) {
            panel.save(metricsAllowableValueRanges);
            panel.save(classMetricsTreeSettings);
            panel.save(projectMetricsTreeSettings);
            onSave();
        }
    }

    private void onSave() {}

    @Override
    public void reset() {}

    @Override
    public void disposeUIResources() {}
}
