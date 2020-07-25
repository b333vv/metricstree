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

import com.intellij.ui.JBColor;
import org.b333vv.metric.ui.treemap.model.ColorProvider;
import org.b333vv.metric.ui.treemap.model.Rectangle;
import org.b333vv.metric.ui.treemap.model.TreeModel;

import java.awt.*;
import java.io.Serializable;

public class DefaultColorProvider<N> implements ColorProvider<N, Color>, Serializable {

    private static final long serialVersionUID = 1L;

    protected static final Color[] COLORS = new JBColor[] {
            new JBColor(new Color(0xA7FFF6), new Color(0xA7FFF6)),
            new JBColor(new Color(0xA4F9C8), new Color(0xA4F9C8)),
            new JBColor(new Color(0x95D9C3), new Color(0x95D9C3)),
            new JBColor(new Color(0x8AA39B), new Color(0x8AA39B)),
            new JBColor(new Color(0x5C6F68), new Color(0x5C6F68))};

    protected final Color[] colors;

    public DefaultColorProvider() {
        colors = COLORS;
    }

    @Override
    public Color getColor(final TreeModel<Rectangle<N>> model, final Rectangle<N> rectangle) {
        return colors[Math.abs(rectangle.getNode().hashCode() % colors.length)];
    }

}
