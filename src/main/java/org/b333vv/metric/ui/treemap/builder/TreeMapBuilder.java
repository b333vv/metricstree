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

package org.b333vv.metric.ui.treemap.builder;

import org.b333vv.metric.event.MetricsEventListener;
import org.b333vv.metric.model.code.JavaClass;
import org.b333vv.metric.model.code.JavaCode;
import org.b333vv.metric.model.code.JavaPackage;
import org.b333vv.metric.model.code.JavaProject;
import org.b333vv.metric.ui.treemap.model.*;
import org.b333vv.metric.ui.treemap.model.Rectangle;
import org.b333vv.metric.ui.treemap.presentation.CushionRectangleRendererEx;
import org.b333vv.metric.ui.treemap.presentation.MetricTreeMap;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class TreeMapBuilder implements SelectionChangeListener<JavaCode>, LabelProvider<JavaCode> {

    private final GenericTreeModel<JavaCode> model = new GenericTreeModel<>();
    private final MetricTreeMap<JavaCode> treeMap = new MetricTreeMap<>();

    public TreeMapBuilder(@NotNull JavaProject javaProject) {
        treeMap.setLabelProvider(this);
        treeMap.setRectangleRenderer(new CushionRectangleRendererEx<>(160));
        treeMap.addSelectionChangeListener(this);
        treeMap.setTreeMapLayout(new SquarifiedLayout<>(64));

//        treeMap.setColorProvider(new MetricTypeColorProvider(MetricType.NCSS));
//        treeMap.setColorProvider(colorProvider);

        treeMap.setTreeModel(TreeMapModel.createTreeModel(javaProject));
    }

//    public void setColorProvider(@NotNull ColorProvider<JavaCode, Color> colorProvider) {
//        treeMap.setColorProvider(colorProvider);
//    }

    public MetricTreeMap<JavaCode> getTreeMap() {
        return treeMap;
    }

    @Override
    public void selectionChanged(TreeModel<Rectangle<JavaCode>> model, Rectangle<JavaCode> rectangle, String label) {
        if (label != null) {
            final JavaCode node = rectangle.getNode();
            if (node instanceof JavaClass) {
                String name = ((JavaClass) node).getPsiClass().getQualifiedName();
                Consumer<String> selectionAction = treeMap.getSelectionChangedAction();
                if (selectionAction != null) {
                    selectionAction.accept("Class: " + name);
                }
            }
        }
    }

    @Override
    public String getLabel(final TreeModel<Rectangle<JavaCode>> model, final Rectangle<JavaCode> rectangle) {
        if (rectangle.getNode() instanceof JavaPackage) {
            return rectangle.getNode().getName();
        }
        return "";
    }
}
