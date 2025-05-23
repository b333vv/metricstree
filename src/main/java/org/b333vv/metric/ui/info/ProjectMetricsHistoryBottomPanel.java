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

package org.b333vv.metric.ui.info;

import com.intellij.openapi.project.Project;
import com.intellij.util.ui.JBInsets;
import com.intellij.util.ui.JBUI;
import org.b333vv.metric.event.ButtonsEventListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static java.awt.GridBagConstraints.*;

public class ProjectMetricsHistoryBottomPanel {
    private final JPanel panel;
    JButton plusButton = new JButton("+");
    JButton minusButton = new JButton("-");
    private final Project project;

    public ProjectMetricsHistoryBottomPanel(Project project) {
        this.project = project;
        panel = new JPanel(new GridBagLayout());


        plusButton.setActionCommand("+");
        plusButton.addActionListener(new ButtonListener());


        minusButton.setActionCommand("-");
        minusButton.addActionListener(new ButtonListener());

        JBInsets insets = JBUI.insets(2);

        panel.add(plusButton, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                CENTER, NONE, insets, 0, 0));
        panel.add(minusButton, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                CENTER, NONE, insets, 0, 0));
    }

    public JComponent getPanel() {
        return panel;
    }

    public class ButtonListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if ("+".equals(e.getActionCommand())) {
                ProjectMetricsHistoryBottomPanel.this.project.getMessageBus()
                        .syncPublisher(ButtonsEventListener.BUTTONS_EVENT_LISTENER_TOPIC).plusButtonPressed(plusButton, minusButton);
            }
            if ("-".equals(e.getActionCommand())) {
                ProjectMetricsHistoryBottomPanel.this.project.getMessageBus()
                        .syncPublisher(ButtonsEventListener.BUTTONS_EVENT_LISTENER_TOPIC).minusButtonPressed(plusButton, minusButton);
            }
        }
    }
}
