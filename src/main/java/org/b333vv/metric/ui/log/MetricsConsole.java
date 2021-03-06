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

package org.b333vv.metric.ui.log;

import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.openapi.project.impl.ProjectLifecycleListener;
import com.intellij.openapi.util.Disposer;
import org.b333vv.metric.event.MetricsEventListener;
import org.jetbrains.annotations.NotNull;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalTime;

public class MetricsConsole implements ProjectLifecycleListener {

    private final ConsoleView consoleView;
    private final Project project;

    public MetricsConsole(Project project) {
        this.project = project;
        consoleView = TextConsoleBuilderFactory.getInstance().createBuilder(project).getConsole();
        ProjectCloseListener projectCloseListener = new ProjectCloseListener();
        project.getMessageBus().connect(project).subscribe(ProjectManager.TOPIC, projectCloseListener);
        MetricsEventListener metricsEventListener = new MetricsConsoleEventListener();
        project.getMessageBus().connect(project).subscribe(MetricsEventListener.TOPIC, metricsEventListener);
    }

    private class ProjectCloseListener implements ProjectManagerListener {
        @Override
        public void projectClosing(@NotNull Project closingProject) {
            if (project == closingProject) {
                Disposer.dispose(consoleView);
            }
        }
    }

    public void debug(String message) {
        getConsoleView().print(LocalTime.now() + ": " + message + "\n", ConsoleViewContentType.NORMAL_OUTPUT);
    }

    public void info(String message) {
        getConsoleView().print(LocalTime.now() + ": " + message + "\n", ConsoleViewContentType.NORMAL_OUTPUT);
    }

    public void firstPart(String message) {
        getConsoleView().print(LocalTime.now() + ": " + message, ConsoleViewContentType.NORMAL_OUTPUT);
    }

    public void lastPart(String message) {
        getConsoleView().print(message + "\n", ConsoleViewContentType.NORMAL_OUTPUT);
    }

    public void error(String message) {
        getConsoleView().print(LocalTime.now() + ": " + message + "\n", ConsoleViewContentType.ERROR_OUTPUT);
    }

    public void error(String message, Throwable t) {
        error(message);
        StringWriter errors = new StringWriter();
        t.printStackTrace(new PrintWriter(errors));
        error(errors.toString());
    }

    public void clear() {
        getConsoleView().clear();
    }

    public ConsoleView getConsoleView() {
        return this.consoleView;
    }

    private class MetricsConsoleEventListener implements MetricsEventListener {

        @Override
        public void printInfo(String info) {
            info(info);
        }
    }
}
