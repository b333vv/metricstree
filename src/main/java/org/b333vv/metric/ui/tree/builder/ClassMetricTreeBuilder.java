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

package org.b333vv.metric.ui.tree.builder;

import org.b333vv.metric.model.code.JavaClass;
import org.b333vv.metric.model.code.JavaPackage;
import org.b333vv.metric.model.code.JavaProject;
import org.b333vv.metric.ui.tree.node.ClassNode;

import javax.swing.tree.DefaultTreeModel;

public class ClassMetricTreeBuilder extends MetricTreeBuilder {

    public ClassMetricTreeBuilder(JavaProject javaProject) {
        super(javaProject);
    }

    public DefaultTreeModel createMetricTreeModel() {
        if (getMetricsTreeFilter().isClassMetricsVisible()
            || getMetricsTreeFilter().isMethodMetricsVisible()) {
            if (!javaProject.getPackages().findFirst().isPresent()) {
                return null;
            }
            JavaPackage javaPackage = javaProject.getPackages().findFirst().get();
            if (!javaPackage.getClasses().findFirst().isPresent()) {
                return null;
            }
            JavaClass rootJavaClass = javaPackage.getClasses().findFirst().get();
            ClassNode rootClassNode = new ClassNode(rootJavaClass);
            model = new DefaultTreeModel(rootClassNode);
            model.setRoot(rootClassNode);
            addSubClasses(rootClassNode);
            addTypeMetricsAndMethodNodes(rootClassNode);
            return model;
        } else {
            return null;
        }
    }
}
