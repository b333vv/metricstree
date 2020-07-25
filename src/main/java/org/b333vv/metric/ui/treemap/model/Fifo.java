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

public class Fifo<T> {

    protected Entry<T> head;
    protected Entry<T> tail;

    public T pull() {
        if (head != null) {
            final Entry<T> current = head;
            head = head.next;
            if (head == null) {
                tail = null;
            }
            return current.value;
        } else {
            return null;
        }
    }

    public void push(final T value) {
        if (tail != null) {
            final Entry<T> current = new Entry<T>(value);
            tail.next = current;
            tail = current;
        } else {
            final Entry<T> current = new Entry<T>(value);
            head = tail = current;
        }
    }

    public boolean notEmpty() {
        return head != null;
    }

    private static final class Entry<T> {
        protected final T value;
        protected Entry<T> next;

        public Entry(final T aValue) {
            value = aValue;
        }
    }

}
