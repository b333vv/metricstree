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

package org.b333vv.metric.ui.treemap.presentation;

import org.b333vv.metric.model.code.JavaClass;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public class TreeMapMouseController<N> implements MouseListener, MouseMotionListener {

    protected final MetricTreeMap<N> treemap;

    public TreeMapMouseController(final MetricTreeMap<N> aMetricTreeMap) {
        treemap = aMetricTreeMap;

        treemap.addMouseMotionListener(this);
        treemap.addMouseListener(this);
    }

    @Override
    public void mouseReleased(final MouseEvent mouseevent) {
        if (treemap.model != null && treemap.currentRoot != null) {
            if (mouseevent.getButton() == MouseEvent.BUTTON1) {
                if (treemap.selected != null) {
                    N cell = treemap.selected.getNode();
                    if (cell instanceof JavaClass) {
                        treemap.getClickedAction().accept((JavaClass) cell);
                    }
                }
            }
        }
    }

    @Override
    public void mouseMoved(final MouseEvent event) {
        final boolean notBuilding;
        synchronized (this) {
            notBuilding = treemap.buildControl == null;
        }
        if (notBuilding) {
            if (treemap.selectRectangle(event.getX(), event.getY())) {
                treemap.repaint();
            }
        }
    }

    @Override
    public void mouseDragged(final MouseEvent event) {
    }

    @Override
    public void mouseClicked(final MouseEvent mouseevent) {
    }

    @Override
    public void mouseEntered(final MouseEvent mouseevent) {
    }

    @Override
    public void mouseExited(final MouseEvent mouseevent) {
    }

    @Override
    public void mousePressed(final MouseEvent mouseevent) {
    }
}
