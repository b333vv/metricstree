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
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import org.jetbrains.annotations.NotNull;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class MetricsConsole implements ProjectComponent {

  private final ConsoleView consoleView;
  private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

  public MetricsConsole(Project project) {
    consoleView = TextConsoleBuilderFactory.getInstance().createBuilder(project).getConsole();
  }

  public static synchronized MetricsConsole get(@NotNull Project p) {
    return p.getComponent(MetricsConsole.class);
  }

  @Override
  public void projectClosed() {
    Disposer.dispose(consoleView);
  }

  public void debug(String msg) {
    getConsoleView().print(LocalTime.now().format(dateTimeFormatter) + ": " + msg + "\n", ConsoleViewContentType.NORMAL_OUTPUT);
  }

  public void info(String msg) {
    getConsoleView().print(LocalTime.now().format(dateTimeFormatter) + ": " + msg + "\n", ConsoleViewContentType.NORMAL_OUTPUT);
  }

  public void error(String msg) {
    getConsoleView().print(LocalTime.now().format(dateTimeFormatter) + ": " + msg + "\n", ConsoleViewContentType.ERROR_OUTPUT);
  }

  public void error(String msg, Throwable t) {
    error(msg);
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
}