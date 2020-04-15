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

import org.b333vv.metric.model.code.*;
import org.b333vv.metric.ui.tree.node.ClassNode;
import org.b333vv.metric.ui.tree.node.FileNode;

import javax.swing.tree.DefaultTreeModel;

public class ClassMetricTreeBuilder extends MetricTreeBuilder {

    public ClassMetricTreeBuilder(JavaCode javaCode) {
        super(javaCode);
    }

    public DefaultTreeModel createMetricTreeModel() {
        JavaFile javaFile = (JavaFile) javaCode;
        if (javaFile.getClasses().count() > 1) {
            FileNode rootFileNode = new FileNode(javaFile);
            model = new DefaultTreeModel(rootFileNode);
            model.setRoot(rootFileNode);
            javaFile.getClasses()
                    .forEach(c -> {
                ClassNode classNode = new ClassNode(c);
                rootFileNode.add(classNode);
                addSubClasses(classNode);
                addTypeMetricsAndMethodNodes(classNode);
            });
        } else if (javaFile.getClasses().findFirst().isPresent()) {
            JavaClass rootJavaClass = javaFile.getClasses().findFirst().get();
            ClassNode rootClassNode = new ClassNode(rootJavaClass);
            model = new DefaultTreeModel(rootClassNode);
            model.setRoot(rootClassNode);
            addSubClasses(rootClassNode);
            addTypeMetricsAndMethodNodes(rootClassNode);
        }
        return model;
    }
}
