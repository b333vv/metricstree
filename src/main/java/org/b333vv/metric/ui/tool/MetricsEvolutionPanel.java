package org.b333vv.metric.ui.tool;

import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.project.Project;
import javax.swing.*;
import java.awt.*;

public class MetricsEvolutionPanel extends JPanel implements DataProvider {
    private final Project project;

    public MetricsEvolutionPanel(Project project) {
        this.project = project;
        setLayout(new BorderLayout());
        JLabel placeholder = new JLabel("Metrics Evolution chart will be here", SwingConstants.CENTER);
        placeholder.setFont(new Font("Arial", Font.ITALIC, 18));
        add(placeholder, BorderLayout.CENTER);
    }

    @Override
    public Object getData(String dataId) {
        // Возвращайте здесь необходимые данные, если требуется интеграция с DataProvider
        return null;
    }
}
