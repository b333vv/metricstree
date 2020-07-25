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

package org.b333vv.metric.ui.treemap.model;

import java.util.*;

public class RectangleModelImpl<N> implements TreeModel<Rectangle<N>> {

    private static final RectangleModelImpl<?> EMPTY = new RectangleModelImpl<>() {
        void addChild(final Rectangle<Object> parent, final Rectangle<Object> child) {}
    };

    protected final Map<N, List<Rectangle<N>>> children;
    protected final Map<N, Rectangle<N>> childToParent;

    @SuppressWarnings("unchecked")
    protected static <T> RectangleModelImpl<T> emptyModel() {
        return (RectangleModelImpl<T>) EMPTY;
    }

    public RectangleModelImpl() {
        children = new HashMap<>(64, 1);
        childToParent = new HashMap<>(64, 1);
    }

    public RectangleModelImpl(final Rectangle<N> root) {
        this();
        addChild(null, root);
    }

    void addChild(final Rectangle<N> parent, final Rectangle<N> child) {
        if (parent != null) {
            childToParent.put(child.getNode(), parent);
            final N key = parent.getNode();
            List<Rectangle<N>> list = children.computeIfAbsent(key, k -> new ArrayList<>(5));
            list.add(child);
        } else {
            childToParent.put(null, child);
        }
    }

    @Override
    public Iterator<Rectangle<N>> getChildren(final Rectangle<N> node) {
        final List<Rectangle<N>> result = children.get(node.getNode());
        return result != null ? result.iterator(): Collections.emptyIterator();
    }

    @Override
    public Rectangle<N> getParent(final Rectangle<N> node) {
        return childToParent.get(node.getNode());
    }

    @Override
    public Rectangle<N> getRoot() {
        return childToParent.get(null);
    }

    @Override
    public boolean hasChildren(final Rectangle<N> node) {
        return children.containsKey(node.getNode());
    }

    public List<Rectangle<N>> toList() {
        final List<Rectangle<N>> list = new ArrayList<>(16);
        final List<Rectangle<N>> stack = new LinkedList<>();
        stack.add(getRoot());
        while (!stack.isEmpty()) {
            final Rectangle<N> node = stack.remove(0);
            list.add(node);
            if (hasChildren(node)) {
                for (final Iterator<Rectangle<N>> i = getChildren(node); i.hasNext(); ) {
                    stack.add(i.next());
                }
            }
        }
        return list;
    }

    public String toString() {
        return toList().toString();
    }

}
