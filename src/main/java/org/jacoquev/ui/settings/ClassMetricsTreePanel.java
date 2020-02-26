package org.jacoquev.ui.settings;

import com.intellij.openapi.project.Project;
import org.jacoquev.util.ClassMetricsTreeSettings;

import javax.swing.*;
import java.util.List;

public class ClassMetricsTreePanel implements ConfigurationPanel<ClassMetricsTreeSettings> {
    private static final String EMPTY_LABEL = "No metrics configured";
    private final Project project;
    private JPanel panel;
    private ClassMetricsTreeTable classMetricsTreeTable;

    public ClassMetricsTreePanel(Project project) {
        this.project = project;
        createUIComponents();
    }

    public JPanel getComponent() {
        return panel;
    }

    @Override
    public boolean isModified(ClassMetricsTreeSettings settings) {
        List<ClassMetricsTreeSettings.ClassMetricsTreeStub> rows = settings.getMetricsList();
        return !rows.equals(classMetricsTreeTable.get());
    }

    @Override
    public void save(ClassMetricsTreeSettings settings) {
        settings.setClassTreeMetrics(classMetricsTreeTable.get());
    }

    @Override
    public void load(ClassMetricsTreeSettings settings) {
        classMetricsTreeTable.set(settings.getMetricsList());
    }

    private void createUIComponents() {
        classMetricsTreeTable = new ClassMetricsTreeTable(EMPTY_LABEL, project);
        panel = classMetricsTreeTable.getComponent();
    }
}
