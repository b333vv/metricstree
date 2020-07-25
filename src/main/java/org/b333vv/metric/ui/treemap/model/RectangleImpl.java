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

public class RectangleImpl<N> implements Rectangle<N> {

	protected final int x, y, w, h;
	protected final N node;

	public RectangleImpl(final N aNode, final int x, final int y, final int width, final int height) {
		node = aNode;
		this.x = x;
		this.y = y;
		w = width;
		h = height;
	}

	protected int area() {
		return w*h;
	}

	@Override
	public int getHeight() {
		return h;
	}

	@Override
	public int getWidth() {
		return w;
	}

	@Override
	public int getX() {
		return x;
	}

	@Override
	public int getY() {
		return y;
	}

	@Override
	public N getNode() {
		return node;
	}

	@Override
	public boolean contains(final int px, final int py) {
		final int wi = px - x;
		if (wi >= 0 && wi < w) {
			final int he = py - y;
			return he >= 0 && he < h;
		} else {
			return false;
		}
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof RectangleImpl<?>) {
			final RectangleImpl<?> other = (RectangleImpl<?>) obj;
			return w == other.w && h == other.h && node.equals(other.node);
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return node.hashCode() ^ (w + h);
	}

	@SuppressWarnings("unchecked")
	RectangleImpl<N>[] split(final double proportion) {
		if (proportion <= 0 || proportion >=1) {
			throw new IllegalArgumentException("cannot split at "+proportion);
		}
		final RectangleImpl<N>[] result = new RectangleImpl[2];
		if (w < h) {
			final int nh = (int) (h*proportion);
			result[0] = new RectangleImpl<>(node, x, y, w, nh);
			result[1] = new RectangleImpl<>(node, x, y + nh, w, h - nh);
		} else {
			final int nw = (int) (w*proportion);
			result[0] = new RectangleImpl<>(node, x, y, nw, h);
			result[1] = new RectangleImpl<>(node, x + nw, y, w - nw, h);
		}
		return result;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder(32);
		sb.append('(').append(node).append(',')
		  .append(x).append(',').append(y).append(',')
		  .append(w).append(',').append(h).append(')');
		return sb.toString();
	}
}
