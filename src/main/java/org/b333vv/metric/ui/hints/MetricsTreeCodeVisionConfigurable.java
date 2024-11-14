package org.b333vv.metric.ui.hints;

import com.intellij.openapi.options.Configurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class MetricsTreeCodeVisionConfigurable implements Configurable {

    private final MetricsTreeCodeVisionSettings settings;
    private JCheckBox checkBox;


    public MetricsTreeCodeVisionConfigurable() {
        this.settings = MetricsTreeCodeVisionSettings.getInstance();
    }

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "Metrics Tree Code Vision";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        JPanel panel = new JPanel();
        checkBox = new JCheckBox("Enable Metrics Tree Code Vision");
        checkBox.setSelected(settings.isEnabled());
        checkBox.addActionListener(e -> settings.setEnabled(checkBox.isSelected()));
        panel.add(checkBox);
        return panel;
    }

    @Override
    public boolean isModified() {
        return checkBox.isSelected() != settings.isEnabled();
    }

    @Override
    public void apply() {
        settings.setEnabled(checkBox.isSelected());
    }

    @Override
    public void reset() {
        checkBox.setSelected(settings.isEnabled());
    }
}
