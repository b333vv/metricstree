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

public class GenericTreeModel<N> implements WeightedTreeModel<N> {

	protected final Map<N, List<N>> children;
	protected final Map<N, N> parents;
	protected final Map<N, Weight> weights;

	public GenericTreeModel() {
		this(new HashMap<>(32, 0.9f),
				new HashMap<>(32, 0.9f), new HashMap<>(32, 0.9f));
	}

	public GenericTreeModel(final Map<N, List<N>> childMap, final Map<N, N> parentMap, final Map<N, Weight> weightMap) {
		children = childMap;
		parents = parentMap;
		weights = weightMap;
	}

	public void add(final N node, final long weight, final N parent) {
		add(node, weight, parent, true);
	}

	public void add(final N node, final long weight, final N parent, final boolean propagateWeights) {
		if (parent != null) {
			parents.put(node, parent);
			List<N> list = children.computeIfAbsent(parent, k -> new ArrayList<N>());
			list.add(node);
			if (propagateWeights) {
				N runner = getParent(node);
				while (runner != null) {
					weights.get(runner).add(weight);
					runner = getParent(runner);
				}
			}
		} else {
			parents.put(null, node);
		}
		weights.put(node, new Weight(weight));
	}

	@Override
	public long getWeight(final N node) {
		final Weight result = weights.get(node);
		if (result != null) {
			return result.get();
		} else {
			return 0;
		}
	}

	@Override
	public Iterator<N> getChildren(final N node) {
		final List<N> result = children.get(node);
		if (result != null && !result.isEmpty()) {
			return new NodeIterator<N>(result);
		} else {
			return Collections.emptyIterator();
		}
	}

	@Override
	public N getParent(final N node) {
		return parents.get(node);
	}

	@Override
	public N getRoot() {
		return parents.get(null);
	}

	@Override
	public boolean hasChildren(final N node) {
		final List<N> result = children.get(node);
		if (result != null) {
			return !result.isEmpty();
		} else {
			return false;
		}
	}

	protected static class Weight {

		private long weight;

		public Weight() {
			weight = 0;
		}

		public Weight(final long initialWeight) {
			weight = initialWeight;
		}

		public void add(final long value) {
			weight += value;
		}

		public long get() {
			return weight;
		}

	}

	private static class NodeIterator<N> implements IteratorSize<N> {

		protected final List<N> nodes;
		protected int pos;

		protected NodeIterator(final List<N> nodeList) {
			nodes = nodeList;
		}

		@Override
		public int size() {
			return nodes.size();
		}

		@Override
		public boolean hasNext() {
			return pos < nodes.size();
		}

		@Override
		public N next() {
			return nodes.get(pos++);
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

	}

}
